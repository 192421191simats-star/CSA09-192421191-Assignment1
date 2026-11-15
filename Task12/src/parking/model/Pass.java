package parking.model;

import java.time.LocalDate;

/** NEW (Task 12): any kind of parking pass. Users HAVE-A pass instead of a boolean flag. */
public interface Pass {
    /** True if parking on the given day is covered by this pass. */
    boolean coversParking(LocalDate day);

    /** Price paid for the pass in rupees (counted in the day's collection when sold). */
    double price();

    String label();
}
