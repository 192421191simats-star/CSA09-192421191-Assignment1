package parking.pricing;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * NEW (Task 12): every started hour is billed; an hour that STARTS inside the
 * peak window [peakStart, peakEnd) costs baseRate x multiplier, otherwise baseRate.
 */
public final class PeakHourRule implements PricingRule {
    private final int baseRate;
    private final int multiplier;
    private final LocalTime peakStart;
    private final LocalTime peakEnd;

    public PeakHourRule(int baseRate, int multiplier, LocalTime peakStart, LocalTime peakEnd) {
        if (baseRate < 0 || multiplier < 1 || !peakStart.isBefore(peakEnd))
            throw new IllegalArgumentException("Invalid peak rule");
        this.baseRate = baseRate;
        this.multiplier = multiplier;
        this.peakStart = peakStart;
        this.peakEnd = peakEnd;
    }

    @Override public double fee(LocalDateTime in, LocalDateTime out) {
        long minutes = PricingRule.minutes(in, out);
        long hours = (minutes + 59) / 60;
        double total = 0;
        for (long h = 0; h < hours; h++) {                     // O(hours) - at most 24 per day
            LocalTime start = in.plusHours(h).toLocalTime();
            boolean peak = !start.isBefore(peakStart) && start.isBefore(peakEnd);
            total += peak ? baseRate * multiplier : baseRate;
        }
        return total;
    }
}
