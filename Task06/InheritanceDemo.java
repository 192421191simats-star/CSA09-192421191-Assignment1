import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Task 6 - Inheritance hierarchy (single + multilevel + hierarchical) with a
 * traced constructor execution order, and a composition-based alternative.
 *
 *            Vehicle (abstract)                 User (abstract)
 *           /                \                 /      |       \
 *     TwoWheeler             Car           Student  Staff   Visitor
 *                             |
 *                        ElectricCar
 *
 * Compile: javac InheritanceDemo.java
 * Run    : java InheritanceDemo input.txt
 */
public class InheritanceDemo {

    static boolean trace = false;
    static void log(String s) { if (trace) System.out.println("    " + s); }

    // ------------------------------ vehicles ------------------------------
    static abstract class Vehicle {
        protected final String regNo;                       // inherited by every subclass
        { log("1. Vehicle instance initialiser block"); }
        protected Vehicle(String regNo) {
            log("2. Vehicle(String) constructor body, regNo=" + regNo);
            this.regNo = regNo;
        }
        abstract int ratePerHour();
        String describe() { return getClass().getSimpleName() + " " + regNo + " @Rs" + ratePerHour() + "/h"; }
    }

    static class TwoWheeler extends Vehicle {               // hierarchical: sibling of Car
        TwoWheeler(String regNo) { super(regNo); }
        @Override int ratePerHour() { return 10; }
    }

    static class Car extends Vehicle {
        protected final int seats;                          // ADDED by Car
        { log("3. Car instance initialiser block"); }
        Car(String regNo) { this(regNo, 5); }
        Car(String regNo, int seats) {
            super(regNo);                                   // super(...) must be the first statement
            log("4. Car(String,int) constructor body, seats=" + seats);
            this.seats = seats;
        }
        @Override int ratePerHour() { return 30; }
        @Override String describe() { return super.describe() + ", seats=" + seats; }
    }

    static class ElectricCar extends Car {                  // multilevel: Vehicle -> Car -> ElectricCar
        private final int batteryKWh;                       // ADDED by ElectricCar
        { log("5. ElectricCar instance initialiser block"); }
        ElectricCar(String regNo, int batteryKWh) {
            super(regNo, 5);                                // chains up to Car, then Vehicle
            log("6. ElectricCar(String,int) constructor body, battery=" + batteryKWh);
            this.batteryKWh = batteryKWh;
        }
        @Override String describe() {
            // super.describe() -> Car.describe() -> Vehicle.describe(); super.regNo reads inherited field
            return super.describe() + ", battery=" + batteryKWh + "kWh (reg via super: " + super.regNo + ")";
        }
    }

    // ------------------------------ users ------------------------------
    static abstract class User {
        protected final String id;
        User(String id) { this.id = id; }
        abstract boolean parksFree();
        public String toString() { return getClass().getSimpleName() + "(" + id + ") free=" + parksFree(); }
    }
    static class Student extends User {
        private final boolean monthlyPass;
        Student(String id, boolean pass) { super(id); monthlyPass = pass; }
        boolean parksFree() { return monthlyPass; }
    }
    static class Staff extends User {
        private final boolean monthlyPass;
        Staff(String id, boolean pass) { super(id); monthlyPass = pass; }
        boolean parksFree() { return monthlyPass; }
    }
    static class Visitor extends User {
        Visitor(String id) { super(id); }
        boolean parksFree() { return false; }
    }

    // ------------------------------ composition alternative ------------------------------
    static final class VehicleType {                       // a new type = a new OBJECT, not a class
        final String name; final int ratePerHour;
        VehicleType(String name, int rate) { this.name = name; this.ratePerHour = rate; }
    }
    static final class ComposedVehicle {
        final String regNo; final VehicleType type;        // has-a
        ComposedVehicle(String regNo, VehicleType type) { this.regNo = regNo; this.type = type; }
        int fee(int minutes) { return (minutes + 59) / 60 * type.ratePerHour; }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== Constructor execution order: new ElectricCar(\"TN07EV3333\", 40) ===");
        trace = true;
        ElectricCar ev = new ElectricCar("TN07EV3333", 40);
        trace = false;
        System.out.println("  -> " + ev.describe());

        System.out.println("\n=== Vehicles from input file ===");
        for (String line : Files.readAllLines(Path.of(args.length > 0 ? args[0] : "input.txt"))) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split(",");
            Vehicle v = switch (p[1].trim()) {
                case "TWO_WHEELER" -> new TwoWheeler(p[0].trim());
                case "CAR" -> new Car(p[0].trim());
                case "ELECTRIC_CAR" -> new ElectricCar(p[0].trim(), 40);
                default -> null;
            };
            System.out.println("  " + (v == null ? p[0].trim() + " rejected: unknown type " + p[1].trim()
                    : v.describe() + " | is-a Vehicle? " + (v instanceof Vehicle)
                      + " | is-a Car? " + (v instanceof Car)));
        }

        System.out.println("\n=== User hierarchy ===");
        User[] users = { new Student("S101", true), new Staff("E202", true),
                         new Student("S102", false), new Visitor("V303") };
        for (User u : users) System.out.println("  " + u);

        System.out.println("\n=== Composition alternative: Vehicle has-a VehicleType ===");
        VehicleType car = new VehicleType("CAR", 30);
        VehicleType ebike = new VehicleType("E_BIKE", 5);    // added at run time - no new class
        ComposedVehicle a = new ComposedVehicle("TN02B2", car);
        ComposedVehicle b = new ComposedVehicle("TN08EB8", ebike);
        System.out.println("  " + a.regNo + " " + a.type.name + " 130 min -> Rs " + a.fee(130));
        System.out.println("  " + b.regNo + " " + b.type.name + " 130 min -> Rs " + b.fee(130));
        System.out.println("  Many vehicles share ONE VehicleType object (flyweight): extra space = 1 reference/vehicle.");
    }
}
