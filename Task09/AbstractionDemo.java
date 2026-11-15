import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Task 9 - Abstraction with abstract classes and interfaces.
 *
 * Compile: javac AbstractionDemo.java
 * Run    : java AbstractionDemo input.txt
 */
public class AbstractionDemo {

    // ===== Capabilities as INTERFACES (can be mixed into unrelated classes) =====
    interface Chargeable {
        double RATE_PER_KWH = 12.0;                             // implicitly public static final
        double chargingFee(int kWh);
        /** default method: shared behaviour added without breaking existing implementers. */
        default int estimatedChargingMinutes(int kWh, int chargerKW) {
            return (int) Math.ceil(kWh * 60.0 / chargerKW);
        }
        /** static method: utility that belongs to the capability, not to an object. */
        static boolean isValidKWh(int kWh) { return kWh >= 0 && kWh <= 150; }
    }

    interface Payable {
        double payableAmount(double grossFee);
    }

    interface Reservable {
        boolean reserveSlot(int floor, int slot);
    }

    // ===== Common identity + state as an ABSTRACT CLASS =====
    static abstract class Vehicle {
        private final String regNo;                             // state - interfaces cannot hold it
        protected Vehicle(String regNo) {                       // constructor validation - interfaces cannot have it
            if (regNo == null || !regNo.matches("[A-Z]{2}\\d{2}[A-Z0-9]{1,6}"))
                throw new IllegalArgumentException("bad registration " + regNo);
            this.regNo = regNo;
        }
        public String getRegNo() { return regNo; }
        protected abstract int ratePerHour();                   // WHAT: each type must say its rate
        public double calculateFee(int minutes) {               // HOW: concrete shared algorithm
            return (minutes + 59) / 60 * (double) ratePerHour();
        }
    }

    static class Car extends Vehicle {
        Car(String r) { super(r); }
        @Override protected int ratePerHour() { return 30; }
    }

    /** Multiple behaviours: IS-A Car, and CAN charge, and CAN reserve. */
    static class ElectricCar extends Car implements Chargeable, Reservable {
        ElectricCar(String r) { super(r); }
        @Override public double chargingFee(int kWh) {
            if (!Chargeable.isValidKWh(kWh)) throw new IllegalArgumentException("kWh " + kWh);
            return RATE_PER_KWH * kWh;
        }
        @Override public boolean reserveSlot(int floor, int slot) { return floor >= 0 && slot >= 0; }
    }

    // ===== Users: Payable applies only to fee-paying users =====
    static abstract class User { final String id; User(String id) { this.id = id; } }
    static class PassHolder extends User { PassHolder(String id) { super(id); } }   // NOT Payable
    static class Visitor extends User implements Payable {
        Visitor(String id) { super(id); }
        public double payableAmount(double grossFee) { return grossFee; }
    }

    // ===== Alternative concept: Vehicle as interface, Chargeable as abstract class =====
    interface IVehicle {
        String regNo();                                          // no field - every class must store it
        int ratePerHour();
        default double calculateFee(int minutes) { return (minutes + 59) / 60 * (double) ratePerHour(); }
    }
    static abstract class AbstractChargeable {
        protected int sessions;                                  // state allowed here
        double chargingFee(int kWh) { sessions++; return 12.0 * kWh; }
    }
    /** Can extend only ONE class, so it cannot also be a Car - Car's rules must be re-written. */
    static class AltElectricCar extends AbstractChargeable implements IVehicle {
        private final String reg;
        AltElectricCar(String reg) { this.reg = reg; }           // no shared validation inherited
        public String regNo() { return reg; }
        public int ratePerHour() { return 30; }                  // duplicated from "car"
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== Abstract class cannot be instantiated ===");
        System.out.println("  'new Vehicle(\"TN01AB1111\")' -> compile error: Vehicle is abstract");

        System.out.println("\n=== Vehicles and users from input ===");
        for (String line : Files.readAllLines(Path.of(args.length > 0 ? args[0] : "input.txt"))) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split(",");
            String reg = p[0].trim(), type = p[1].trim(), user = p[2].trim();
            int min = Integer.parseInt(p[3].trim()), kwh = Integer.parseInt(p[4].trim());
            try {
                Vehicle v = type.equals("ELECTRIC_CAR") ? new ElectricCar(reg) : new Car(reg);
                User u = user.equals("VISITOR") ? new Visitor("U-" + reg) : new PassHolder("U-" + reg);
                double parking = v.calculateFee(min);
                double charging = 0;
                if (v instanceof Chargeable c) charging = c.chargingFee(kwh);
                double payParking = (u instanceof Payable pay) ? pay.payableAmount(parking) : 0;
                System.out.printf("  %-11s %-12s %-10s %4d min %3d kWh -> parking Rs %.1f, charging Rs %.1f, total Rs %.1f%s%n",
                        reg, type, user, min, kwh, payParking, charging, payParking + charging,
                        v instanceof Chargeable c ? "  (~" + c.estimatedChargingMinutes(kwh, 22) + " min on 22 kW)" : "");
            } catch (IllegalArgumentException e) {
                System.out.println("  " + reg + " rejected: " + e.getMessage());
            }
        }

        System.out.println("\n=== Alternative (Vehicle=interface, Chargeable=abstract class) ===");
        AltElectricCar alt = new AltElectricCar("bad reg!");     // nothing stops an invalid object
        System.out.println("  AltElectricCar(\"bad reg!\") accepted -> " + alt.regNo()
                + " ; fee(130) = " + alt.calculateFee(130) + " ; charging(10) = " + alt.chargingFee(10));
        System.out.println("  instanceof Car? impossible - it already used its one superclass on AbstractChargeable");
    }
}
