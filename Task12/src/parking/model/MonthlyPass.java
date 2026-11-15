package parking.model;

import java.time.LocalDate;
import java.time.YearMonth;

/** NEW (Task 12): monthly pass for students and staff (replaces the old boolean flag). */
public final class MonthlyPass implements Pass {
    private final YearMonth month;

    public MonthlyPass(YearMonth month) {
        if (month == null) throw new IllegalArgumentException("Month required");
        this.month = month;
    }

    @Override public boolean coversParking(LocalDate day) { return YearMonth.from(day).equals(month); }
    @Override public double price() { return 0; }          // sold separately by the admin office
    @Override public String label() { return "PASS"; }
}
