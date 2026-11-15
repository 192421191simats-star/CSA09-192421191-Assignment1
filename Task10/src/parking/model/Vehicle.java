package parking.model;

/** public abstract: visible to service and app packages; cannot be instantiated. */
public abstract class Vehicle {
    private final String regNo;                         // private: only this class touches it

    protected Vehicle(String regNo) {                   // protected: only subclasses construct it
        if (regNo == null || !regNo.matches("[A-Z]{2}\\d{2}[A-Z]{1,2}\\d{1,4}"))
            throw new IllegalArgumentException("Invalid registration number: " + regNo);
        this.regNo = regNo;
    }

    public final String getRegNo() { return regNo; }    // read-only access, no setter

    protected abstract int ratePerHour();               // protected: subclasses supply it

    public int hourlyRate() { return ratePerHour(); }   // public read of the rate

    @Override public String toString() { return getClass().getSimpleName() + "[" + regNo + "]"; }
}
