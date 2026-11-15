import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Task 7 - Override and specialise behaviour; final; toString/equals/hashCode.
 *
 * Compile: javac OverridingDemo.java
 * Run    : java OverridingDemo input.txt
 */
public class OverridingDemo {

    static abstract class Vehicle {
        public static final int MINUTES_PER_HOUR = 60;        // final constant
        private final String regNo;                           // final field: identity never changes

        protected Vehicle(String regNo) { this.regNo = Objects.requireNonNull(regNo); }

        public final String getRegNo() { return regNo; }      // final method: no subclass may change identity

        protected abstract int ratePerHour();

        /** Template used by most vehicles: every started hour billed in full. */
        public double calculateFee(int minutes) {
            int hours = (minutes + MINUTES_PER_HOUR - 1) / MINUTES_PER_HOUR;
            return hours * (double) ratePerHour();
        }

        @Override public String toString() {
            return getClass().getSimpleName() + "[" + regNo + "]";
        }
        /** Two vehicles are equal when their registration numbers are equal (any subclass). */
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Vehicle other)) return false;
            return regNo.equals(other.regNo);
        }
        @Override public int hashCode() { return regNo.hashCode(); }   // consistent with equals
    }

    static class TwoWheeler extends Vehicle {
        TwoWheeler(String r) { super(r); }
        @Override protected int ratePerHour() { return 10; }
        /** Specialised: two-wheelers get the first 30 minutes free. */
        @Override public double calculateFee(int minutes) {
            return minutes <= 30 ? 0 : super.calculateFee(minutes);
        }
    }

    static class Car extends Vehicle {
        Car(String r) { super(r); }
        @Override protected int ratePerHour() { return 30; }    // same signature; access widened is allowed
    }

    static final class ElectricCar extends Car {              // final class: no further subclassing
        private final int kWh;
        ElectricCar(String r, int kWh) { super(r); this.kWh = kWh; }
        /** Specialised: parking fee (inherited rule) + charging at Rs 12 per kWh. */
        @Override public double calculateFee(int minutes) {
            return super.calculateFee(minutes) + 12.0 * kWh;
        }
        // @Override protected int ratePerHour() { return 30L; }   // would NOT compile: return type long != int
        // @Override int ratePerHour() { return 30; }              // would NOT compile: weaker access than protected
    }

    // ---------------- Alternative: one method with a switch on a type field ----------------
    static double switchFee(String type, int minutes, int kWh) {
        int hours = (minutes + 59) / 60;
        switch (type) {
            case "TWO_WHEELER":  return minutes <= 30 ? 0 : hours * 10.0;
            case "CAR":          return hours * 30.0;
            case "ELECTRIC_CAR": return hours * 30.0 + 12.0 * kWh;
            default: throw new IllegalArgumentException("unknown type " + type);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== Overridden calculateFee() from input file ===");
        System.out.printf("  %-26s %6s %10s %12s%n", "vehicle", "min", "override", "switch-alt");
        for (String line : Files.readAllLines(Path.of(args.length > 0 ? args[0] : "input.txt"))) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split(",");
            String reg = p[0].trim(), type = p[1].trim();
            int min = Integer.parseInt(p[2].trim()), kwh = Integer.parseInt(p[3].trim());
            Vehicle v = switch (type) {
                case "TWO_WHEELER" -> new TwoWheeler(reg);
                case "CAR" -> new Car(reg);
                default -> new ElectricCar(reg, kwh);
            };
            System.out.printf("  %-26s %6d %10.1f %12.1f%n", v, min, v.calculateFee(min), switchFee(type, min, kwh));
        }

        System.out.println("\n=== equals() / hashCode() / toString() ===");
        Vehicle a = new Car("TN02B2");
        Vehicle b = new Car("TN02B2");
        Vehicle c = new ElectricCar("TN02B2", 5);
        System.out.println("  a == b        : " + (a == b) + "  (different objects)");
        System.out.println("  a.equals(b)   : " + a.equals(b) + "  (same registration)");
        System.out.println("  a.equals(c)   : " + a.equals(c) + "  (Car vs ElectricCar, same registration)");
        System.out.println("  hash a,b,c    : " + a.hashCode() + ", " + b.hashCode() + ", " + c.hashCode());
        Set<Vehicle> inside = new HashSet<>();
        System.out.println("  add a -> " + inside.add(a) + ", add b -> " + inside.add(b)
                + ", add c -> " + inside.add(c) + "  => set size " + inside.size()
                + " (duplicate registration blocked)");

        // ---------------- timing: override vs switch ----------------
        Vehicle[] fleet = new Vehicle[3_000_000];
        String[] types = new String[fleet.length];
        for (int i = 0; i < fleet.length; i++) {
            switch (i % 3) {
                case 0 -> { fleet[i] = new TwoWheeler("R" + i); types[i] = "TWO_WHEELER"; }
                case 1 -> { fleet[i] = new Car("R" + i); types[i] = "CAR"; }
                default -> { fleet[i] = new ElectricCar("R" + i, 10); types[i] = "ELECTRIC_CAR"; }
            }
        }
        double sink = 0;
        for (int w = 0; w < 5; w++) {
            for (Vehicle v : fleet) sink += v.calculateFee(130);
            for (String t : types) sink += switchFee(t, 130, 10);
        }
        long t0 = System.nanoTime();
        for (Vehicle v : fleet) sink += v.calculateFee(130);
        long t1 = System.nanoTime();
        for (String t : types) sink += switchFee(t, 130, 10);
        long t2 = System.nanoTime();
        System.out.println("\n=== Time per fee calculation, " + fleet.length + " calls, k = 3 types ===");
        System.out.printf("  overriding (virtual call) : %.2f ns/call%n", (t1 - t0) / (double) fleet.length);
        System.out.printf("  switch on String type     : %.2f ns/call%n", (t2 - t1) / (double) fleet.length);
        System.out.println("  (checksum " + (long) sink + ")");
    }
}
