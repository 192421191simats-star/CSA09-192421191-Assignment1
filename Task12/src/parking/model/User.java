package parking.model;

import java.time.LocalDate;

/**
 * A person parking on campus.
 * MODIFIED (Task 12): free parking is now decided by an optional Pass object
 * (composition) checked against a date, instead of a boolean in each subclass.
 */
public abstract class User {
    private final String id;
    private Pass pass;                                       // null = no pass

    protected User(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("User id required");
        this.id = id;
    }

    public String getId() { return id; }
    public Pass getPass() { return pass; }

    /** Each user type decides which passes it may hold. */
    protected abstract boolean mayHold(Pass p);

    public void setPass(Pass p) {
        if (p != null && !mayHold(p))
            throw new IllegalArgumentException(category() + " cannot hold a " + p.label());
        this.pass = p;
    }

    /** True if parking on this day is free for this user. */
    public boolean hasFreeParking(LocalDate day) { return pass != null && pass.coversParking(day); }

    public String category() { return getClass().getSimpleName().toUpperCase(); }

    @Override public String toString() { return category() + (pass != null ? "(" + pass.label().toLowerCase() + ")" : ""); }
}
