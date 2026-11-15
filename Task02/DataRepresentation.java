import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Task 2 - Choose the Data Representation.
 *
 * Input (Scanner, read from standard input - see input.txt):
 *   line 1 : number of floors F
 *   line 2 : F integers, slots on each floor (may differ -> jagged array)
 *   line 3 : number of parking durations D
 *   line 4 : D durations in minutes
 *
 * Compile: javac DataRepresentation.java
 * Run    : java DataRepresentation < input.txt
 */
public class DataRepresentation {

    static final int RATE_PER_HOUR = 30;          // car rate, whole rupees

    // ---------- WRONG ways to bill a stay (casting / integer-division traps) ----------
    static int wrongIntDivision(int minutes) {
        return (minutes / 60) * RATE_PER_HOUR;             // 130/60 = 2 -> drops 10 minutes
    }
    static double wrongCeilAfterIntDivision(int minutes) {
        return Math.ceil(minutes / 60) * RATE_PER_HOUR;    // division happens in int first!
    }

    // ---------- CORRECT way used in the design ----------
    /** Every started hour is billed in full; integer arithmetic only, no precision loss. */
    static int billableHours(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("negative duration");
        return (minutes + 59) / 60;                        // integer ceiling
    }
    static double correctFee(int minutes) {
        return billableHours(minutes) * (double) RATE_PER_HOUR;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int floors = in.nextInt();
        int[] slotsPerFloor = new int[floors];
        for (int f = 0; f < floors; f++) slotsPerFloor[f] = in.nextInt();
        int d = in.nextInt();
        int[] durations = new int[d];
        for (int i = 0; i < d; i++) durations[i] = in.nextInt();

        // ----- Jagged array: one row per floor, row length = slots on that floor -----
        String[][] slots = new String[floors][];
        int total = 0;
        for (int f = 0; f < floors; f++) {
            slots[f] = new String[slotsPerFloor[f]];
            total += slotsPerFloor[f];
        }
        System.out.println("=== Facility as a jagged array String[floor][slot] ===");
        for (int f = 0; f < floors; f++)
            System.out.println("  floor " + f + " -> slots.length = " + slots[f].length);
        System.out.println("  total slots = " + total);

        // ----- Field types -----
        System.out.println("\n=== Chosen data types ===");
        System.out.println("  floor, slot number : int     (small, whole, used as array index)");
        System.out.println("  vehicle number     : String  (alphanumeric, e.g. TN01AB1111)");
        System.out.println("  entry/exit time    : java.time.LocalDateTime (no manual hh:mm maths)");
        System.out.println("  duration           : int minutes (whole minutes, no rounding error)");
        System.out.println("  fee                : double rupees computed from int hours x int rate");
        System.out.println("  ticket number      : long    (never reused, large range)");

        // ----- Casting risk demonstration -----
        System.out.println("\n=== Casting / integer-division risk (rate Rs " + RATE_PER_HOUR + "/h) ===");
        System.out.printf("  %-8s %-14s %-18s %-14s%n", "minutes", "(m/60)*rate", "ceil(m/60)*rate", "(m+59)/60*rate");
        for (int m : durations) {
            System.out.printf("  %-8d %-14d %-18.1f %-14.1f%s%n", m, wrongIntDivision(m),
                    wrongCeilAfterIntDivision(m), correctFee(m),
                    wrongIntDivision(m) != correctFee(m) ? "  <-- wrong fee" : "");
        }
        double hoursAsDouble = 130 / 60.0;                  // 2.1666...
        System.out.println("  130 / 60.0 = " + hoursAsDouble + " ; (int) of it = " + (int) hoursAsDouble
                + "  -> narrowing cast silently truncates");
        System.out.println("  0.1 + 0.2 = " + (0.1 + 0.2) + "  -> never add fractional rupees as double;");
        System.out.println("  fees are built from int hours x int rate, so they stay exact.");

        // ----- Alternative representations: locate (floor, slot) -----
        System.out.println("\n=== Alternative representations: locate floor 2, slot 7 ===");
        int[] offset = new int[floors];                     // prefix sums for 1-D layout
        for (int f = 1; f < floors; f++) offset[f] = offset[f - 1] + slotsPerFloor[f - 1];
        String[] flat = new String[total];
        List<String[]> list = new ArrayList<>();
        for (int f = 0; f < floors; f++) list.add(new String[slotsPerFloor[f]]);

        int floor = Math.min(2, floors - 1), slot = 7;
        slots[floor][slot] = "TN07EV3333";
        flat[offset[floor] + slot] = "TN07EV3333";
        list.get(floor)[slot] = "TN07EV3333";
        int naive = floor * slotsPerFloor[0] + slot;        // WRONG when floors differ
        System.out.println("  jagged     slots[" + floor + "][" + slot + "] = " + slots[floor][slot]);
        System.out.println("  1-D        flat[offset[f]+s] = flat[" + (offset[floor] + slot) + "] = "
                + flat[offset[floor] + slot]);
        System.out.println("  1-D naive  flat[f*S+s]       = flat[" + naive + "] = " + flat[naive]
                + "  <-- wrong when floors have different sizes");
        System.out.println("  ArrayList  list.get(" + floor + ")[" + slot + "] = " + list.get(floor)[slot]);
    }
}
