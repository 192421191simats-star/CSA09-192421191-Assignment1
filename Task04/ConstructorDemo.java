import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Task 4 - Classes, Objects and Constructors.
 *
 * Compile: javac *.java
 * Run    : java ConstructorDemo input.txt
 */
public class ConstructorDemo {
    public static void main(String[] args) throws IOException {
        LocalDate day = LocalDate.of(2026, 11, 16);

        System.out.println("=== Slots: default, overloaded and full constructors (chained) ===");
        ParkingSlot s0 = new ParkingSlot();
        ParkingSlot s1 = new ParkingSlot(1, 4);
        ParkingSlot s2 = new ParkingSlot(2, 9, true);
        s1.occupy("TN01AB1111");
        System.out.println("  " + s0 + " | " + s1 + " | " + s2);
        System.out.println("  second occupy on F1-S4 allowed? " + s1.occupy("TN99ZZ9999"));

        System.out.println("\n=== Tickets issued from " + (args.length > 0 ? args[0] : "input.txt") + " ===");
        ParkingTicket first = null;
        for (String line : Files.readAllLines(Path.of(args.length > 0 ? args[0] : "input.txt"))) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split(",");
            ParkingTicket t = new ParkingTicket(p[0].trim(), Integer.parseInt(p[1].trim()),
                    Integer.parseInt(p[2].trim()), LocalDateTime.of(day, LocalTime.parse(p[3].trim())));
            if (first == null) first = t;
            System.out.println("  " + t);
        }

        System.out.println("\n=== Copy constructor: driver lost ticket, duplicate re-issued ===");
        ParkingTicket dup = new ParkingTicket(first);
        System.out.println("  original : " + first);
        System.out.println("  duplicate: " + dup);
        System.out.println("  same number? " + (first.getTicketNo() == dup.getTicketNo())
                + " | same object? " + (first == dup));

        System.out.println("\n=== References: stack variable -> heap object ===");
        ParkingTicket alias = first;                  // copies the REFERENCE, not the object
        System.out.println("  alias == first : " + (alias == first) + " (one heap object, two references)");
        System.out.println("  dup   == first : " + (dup == first) + " (copy constructor made a new heap object)");

        System.out.println("\n=== Invalid objects ===");
        try {
            new ParkingTicket("", 0, 1, LocalDateTime.of(day, LocalTime.NOON));
        } catch (IllegalArgumentException e) {
            System.out.println("  constructor rejected blank regNo: " + e.getMessage());
        }
        SetterTicket half = new SetterTicket();
        half.setRegNo("TN05MN5555");                  // forgot floor, slot, time, number
        System.out.println("  setter style created: " + half + " -> valid? " + half.isValid());

        // ----- Benchmark: creating n tickets three ways -----
        int n = 1_000_000;
        LocalDateTime at = LocalDateTime.of(day, LocalTime.of(9, 0));
        for (int w = 0; w < 3; w++) { build(n, at, 0); build(n, at, 1); build(n, at, 2); }
        System.out.println("\n=== Time to create n = " + n + " tickets (ms) ===");
        String[] names = {"constructor chain", "default + setters", "static factory"};
        for (int k = 0; k < 3; k++) {
            long t0 = System.nanoTime();
            build(n, at, k);
            System.out.printf("  %-18s : %.1f ms%n", names[k], (System.nanoTime() - t0) / 1e6);
        }
        System.out.println("  All three are O(n) time and O(n) space; setters add 5 extra calls/object.");
    }

    static Object build(int n, LocalDateTime at, int kind) {
        Object[] keep = new Object[n];
        for (int i = 0; i < n; i++) {
            switch (kind) {
                case 0 -> keep[i] = new ParkingTicket("TN01AB1111", 0, i % 100, at);
                case 1 -> {
                    SetterTicket t = new SetterTicket();
                    t.setTicketNo(i + 1); t.setRegNo("TN01AB1111"); t.setFloor(0);
                    t.setSlot(i % 100); t.setEntryTime(at);
                    keep[i] = t;
                }
                default -> keep[i] = ParkingTicket.issue("TN01AB1111", 0, i % 100, at);
            }
        }
        return keep;
    }
}
