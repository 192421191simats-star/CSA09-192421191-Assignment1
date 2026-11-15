import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Task 3 - Fee computation control logic (if-else + switch, break/continue/return).
 *
 * Fee slabs (same rules are used by the full system in Task 11):
 *   - 0 minutes                       : Rs 0 (vehicle entered and left at once)
 *   - TWO_WHEELER up to 30 minutes     : Rs 0 (grace period)
 *   - first started hour               : 1 x hourly rate (minimum charge)
 *   - each additional started hour     : + 1 x hourly rate
 *   - daily maximum per 24 h block     : 8 x hourly rate
 *   Hourly rate: TWO_WHEELER 10, CAR 30, ELECTRIC_CAR 30.
 *
 * Compile: javac FeeSlabCalculator.java
 * Run    : java FeeSlabCalculator input.txt
 */
public class FeeSlabCalculator {

    static final int MINUTES_PER_DAY = 24 * 60;
    static final int CAP_HOURS = 8;

    /** Returns the hourly rate, or -1 for an unknown type (caller must reject). */
    static int rateFor(String type) {
        switch (type) {
            case "TWO_WHEELER":  return 10;
            case "CAR":
            case "ELECTRIC_CAR": return 30;
            default:             return -1;
        }
    }

    static int fee(String type, int minutes) {
        int rate = rateFor(type);
        if (rate < 0) throw new IllegalArgumentException("Unknown vehicle type: " + type);
        if (minutes < 0) throw new IllegalArgumentException("Negative duration: " + minutes);
        if (minutes == 0) return 0;                              // slab 0
        if (type.equals("TWO_WHEELER") && minutes <= 30) return 0; // grace slab

        int dailyMax = CAP_HOURS * rate;
        int fullDays = minutes / MINUTES_PER_DAY;                 // complete 24 h blocks
        int rest = minutes % MINUTES_PER_DAY;
        int hours = (rest + 59) / 60;                             // started hours (int ceiling)

        int restFee;
        if (hours == 0) {
            restFee = 0;                                          // exactly n whole days
        } else if (hours == 1) {
            restFee = rate;                                       // first hour slab
        } else {
            restFee = rate + (hours - 1) * rate;                  // first + additional hours
            if (restFee > dailyMax) restFee = dailyMax;           // daily maximum slab
        }
        return fullDays * dailyMax + restFee;
    }

    public static void main(String[] args) throws IOException {
        Path file = Path.of(args.length > 0 ? args[0] : "input.txt");
        System.out.printf("%-14s %8s %8s%n", "TYPE", "MINUTES", "FEE(Rs)");
        for (String line : Files.readAllLines(file)) {
            if (line.isBlank() || line.startsWith("#")) continue;        // continue: skip noise
            String[] p = line.trim().split("\\s+");
            try {
                System.out.printf("%-14s %8s %8d%n", p[0], p[1], fee(p[0], Integer.parseInt(p[1])));
            } catch (IllegalArgumentException e) {
                System.out.printf("%-14s %8s   REJECTED: %s%n", p[0], p[1], e.getMessage());
            }
        }
    }
}
