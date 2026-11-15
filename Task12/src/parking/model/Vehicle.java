package parking.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Root of the vehicle hierarchy. Holds identity (registration number) and the
 * common fee algorithm; subclasses supply the rate and any specialisation.
 *
 * Fee rules: 0 min = Rs 0; every started hour billed in full;
 * at most 8 x hourly rate per 24-hour block (daily maximum).
 */
public abstract class Vehicle {
    public static final int MINUTES_PER_DAY = 24 * 60;
    public static final int DAILY_CAP_HOURS = 8;

    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || !regNo.matches("[A-Z]{2}\\d{2}[A-Z]{1,3}\\d{1,4}"))
            throw new IllegalArgumentException("Invalid registration number: " + regNo);
        this.regNo = regNo;
    }

    public final String getRegNo() { return regNo; }

    /** Hourly parking rate in rupees. */
    protected abstract int ratePerHour();

    /** Grace period in minutes that is free of charge (none by default). */
    protected int graceMinutes() { return 0; }

    /** True if this vehicle should be placed in a charging slot when one is free. */
    public boolean needsChargingSlot() { return false; }

    /** Parking fee for a stay of the given length (compile-time overload #1). */
    public double calculateFee(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Negative duration: " + minutes);
        if (minutes == 0 || minutes <= graceMinutes()) return 0;
        int rate = ratePerHour();
        int dailyMax = DAILY_CAP_HOURS * rate;
        int fullDays = minutes / MINUTES_PER_DAY;
        int rest = minutes % MINUTES_PER_DAY;
        int hours = (rest + 59) / 60;                      // integer ceiling, no precision loss
        int restFee = Math.min(hours * rate, dailyMax);
        return fullDays * dailyMax + restFee;
    }

    /** Parking fee between two instants (compile-time overload #2). */
    public double calculateFee(LocalDateTime in, LocalDateTime out) {
        if (out.isBefore(in)) throw new IllegalArgumentException("Exit time before entry time");
        return calculateFee((int) Duration.between(in, out).toMinutes());
    }

    public String typeName() { return getClass().getSimpleName(); }

    @Override public String toString() { return typeName() + "[" + regNo + "]"; }

    /** Same registration number = same physical vehicle, whatever the subclass. */
    @Override public final boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Vehicle other && regNo.equals(other.regNo);
    }

    @Override public final int hashCode() { return regNo.hashCode(); }
}
