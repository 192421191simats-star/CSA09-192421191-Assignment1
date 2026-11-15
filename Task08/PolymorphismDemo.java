import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Task 8 - Compile-time (overloading) and runtime (dynamic dispatch) polymorphism.
 * Re-uses the completed Vehicle hierarchy from FeeDemo.java (same folder).
 *
 * Compile: javac FeeDemo.java PolymorphismDemo.java
 * Run    : java PolymorphismDemo input.txt
 */
public class PolymorphismDemo {

    // ---------------- compile-time polymorphism: overloaded calculateFee ----------------
    static double calculateFee(Vehicle v, int minutes) {
        System.out.println("    [compiler chose calculateFee(Vehicle,int)]");
        return v.calculateFee(minutes);
    }
    static double calculateFee(Vehicle v, LocalTime in, LocalTime out) {
        System.out.println("    [compiler chose calculateFee(Vehicle,LocalTime,LocalTime)] -> converts to minutes and delegates:");
        return calculateFee(v, (int) Duration.between(in, out).toMinutes());
    }
    static double calculateFee(Vehicle v, LocalDateTime in, LocalDateTime out) {
        System.out.println("    [compiler chose calculateFee(Vehicle,LocalDateTime,LocalDateTime)] -> converts to minutes and delegates:");
        return calculateFee(v, (int) Duration.between(in, out).toMinutes());
    }

    // ---------------- alternative: instanceof + cast chain ----------------
    static double feeByInstanceof(Vehicle v, int minutes) {
        int hours = (minutes + 59) / 60;
        if (v instanceof ElectricCar) return hours * 30.0;      // must test most-specific FIRST
        else if (v instanceof Car) return hours * 30.0;
        else if (v instanceof TwoWheeler) return hours * 10.0;
        throw new IllegalArgumentException("unhandled type " + v.getClass());
    }

    /** Finds which class actually declares the method that will run (for the trace). */
    static String declaringClass(Class<?> c, String name, Class<?>... params) {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            try { Method m = k.getDeclaredMethod(name, params); return k.getSimpleName() + "." + m.getName(); }
            catch (NoSuchMethodException ignored) { }
        }
        return "?";
    }

    public static void main(String[] args) throws IOException {
        List<Vehicle> list = new ArrayList<>();
        for (String line : Files.readAllLines(Path.of(args.length > 0 ? args[0] : "input.txt"))) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split(",");
            Vehicle v = switch (p[1].trim()) {                  // UPCASTING: subclass -> Vehicle
                case "TWO_WHEELER" -> new TwoWheeler(p[0].trim());
                case "CAR" -> new Car(p[0].trim());
                default -> new ElectricCar(p[0].trim());
            };
            list.add(v);
        }
        Vehicle[] parked = list.toArray(new Vehicle[0]);        // ONE array, many actual types

        System.out.println("=== Runtime polymorphism: dynamic method dispatch trace ===");
        System.out.printf("  %-10s %-9s %-12s %-24s %-22s %s%n", "reg", "declared", "actual",
                "calculateFee() runs", "ratePerHour() runs", "fee(130)");
        for (Vehicle v : parked) {
            System.out.printf("  %-10s %-9s %-12s %-24s %-22s %.1f%n", v.getRegNo(), "Vehicle",
                    v.getClass().getSimpleName(), declaringClass(v.getClass(), "calculateFee", int.class),
                    declaringClass(v.getClass(), "ratePerHour"), v.calculateFee(130));
        }

        System.out.println("\n=== Compile-time polymorphism: overload chosen by argument types ===");
        Vehicle ev = parked[parked.length - 1];
        System.out.println("  call: calculateFee(ev, 130)");
        System.out.println("    = Rs " + calculateFee(ev, 130));
        System.out.println("  call: calculateFee(ev, LocalTime 09:00, LocalTime 11:10)");
        System.out.println("    = Rs " + calculateFee(ev, LocalTime.of(9, 0), LocalTime.of(11, 10)));
        System.out.println("  call: calculateFee(ev, 16-Nov 22:00, 17-Nov 00:10)  (crosses midnight)");
        System.out.println("    = Rs " + calculateFee(ev, LocalDateTime.of(2026, 11, 16, 22, 0),
                LocalDateTime.of(2026, 11, 17, 0, 10)));

        // ---------------- timing: dispatch vs instanceof chain ----------------
        int n = 3_000_000;
        Vehicle[] fleet = new Vehicle[n];
        for (int i = 0; i < n; i++)
            fleet[i] = i % 3 == 0 ? new TwoWheeler("R" + i) : i % 3 == 1 ? new Car("R" + i) : new ElectricCar("R" + i);
        double sink = 0;
        for (int w = 0; w < 5; w++) for (Vehicle v : fleet) { sink += v.calculateFee(130); sink += feeByInstanceof(v, 130); }
        long t0 = System.nanoTime();
        for (Vehicle v : fleet) sink += v.calculateFee(130);
        long t1 = System.nanoTime();
        for (Vehicle v : fleet) sink += feeByInstanceof(v, 130);
        long t2 = System.nanoTime();
        System.out.println("\n=== Time per call over " + n + " mixed vehicles ===");
        System.out.printf("  dynamic dispatch   : %.2f ns/call  (O(1) vtable lookup)%n", (t1 - t0) / (double) n);
        System.out.printf("  instanceof chain   : %.2f ns/call  (up to k type tests)%n", (t2 - t1) / (double) n);
        System.out.println("  (checksum " + (long) sink + ")");
    }
}
