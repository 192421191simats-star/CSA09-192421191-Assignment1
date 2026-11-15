package parking.model;

import parking.pricing.FlatFeeRule;

/** NEW (Task 12): e-bikes pay a flat Rs 20 per visit. Added by extension only. */
public class EBike extends Vehicle {
    private static final FlatFeeRule RULE = new FlatFeeRule(20);

    public EBike(String regNo) { super(regNo); }

    @Override protected int ratePerHour() { return 0; }       // hourly rate not used by a flat rule

    /** Vehicle.calculateFee(in, out) converts to minutes and lands here. */
    @Override public double calculateFee(int minutes) { return RULE.feeFor(minutes); }
}
