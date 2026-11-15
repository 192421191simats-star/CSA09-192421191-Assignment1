package parking.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import parking.pricing.PeakHourRule;
import parking.pricing.PricingRule;

/** NEW (Task 12): double the car rate for hours starting between 09:00 and 17:00. */
public class DeliveryVan extends Vehicle {
    public static final int CAR_RATE = 30;
    private static final PricingRule RULE =
            new PeakHourRule(CAR_RATE, 2, LocalTime.of(9, 0), LocalTime.of(17, 0));

    public DeliveryVan(String regNo) { super(regNo); }

    /** Used only when no clock times are known: charges the peak rate (safe upper bound). */
    @Override protected int ratePerHour() { return CAR_RATE * 2; }

    /** The time-of-day rule needs real entry/exit times, so this overload is the one that matters. */
    @Override public double calculateFee(LocalDateTime in, LocalDateTime out) {
        return RULE.fee(in, out);
    }
}
