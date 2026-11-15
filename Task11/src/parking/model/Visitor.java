package parking.model;

public class Visitor extends User {
    public Visitor(String id) { super(id); }
    /** Visitors always pay the full fee. */
    @Override public boolean hasFreeParking() { return false; }
}
