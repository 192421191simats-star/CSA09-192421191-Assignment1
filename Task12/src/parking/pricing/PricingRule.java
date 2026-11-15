package parking.pricing;

import java.time.LocalDateTime;

/** NEW (Task 12): a pricing rule is an object, so new rules are new classes - not edits. */
@FunctionalInterface
public interface PricingRule {
    double fee(LocalDateTime in, LocalDateTime out);

    static long minutes(LocalDateTime in, LocalDateTime out) {
        if (out.isBefore(in)) throw new IllegalArgumentException("Exit time before entry time");
        return java.time.Duration.between(in, out).toMinutes();
    }
}
