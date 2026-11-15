package parking.pricing;

import java.time.LocalDateTime;

/** NEW (Task 12): fixed amount per visit, whatever the duration (zero-minute stay is free). */
public final class FlatFeeRule implements PricingRule {
    private final double amount;

    public FlatFeeRule(double amount) {
        if (amount < 0) throw new IllegalArgumentException("Negative flat fee");
        this.amount = amount;
    }

    @Override public double fee(LocalDateTime in, LocalDateTime out) {
        return feeFor(PricingRule.minutes(in, out));
    }

    public double feeFor(long minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Negative duration: " + minutes);
        return minutes == 0 ? 0 : amount;
    }
}
