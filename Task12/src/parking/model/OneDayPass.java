package parking.model;

import java.time.LocalDate;

/** NEW (Task 12): one-day pass that visitors may buy at the gate. */
public final class OneDayPass implements Pass {
    public static final double PRICE = 100.0;
    private final LocalDate day;

    public OneDayPass(LocalDate day) {
        if (day == null) throw new IllegalArgumentException("Day required");
        this.day = day;
    }

    @Override public boolean coversParking(LocalDate d) { return day.equals(d); }
    @Override public double price() { return PRICE; }
    @Override public String label() { return "DAYPASS"; }
}
