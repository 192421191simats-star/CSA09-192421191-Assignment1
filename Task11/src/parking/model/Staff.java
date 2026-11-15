package parking.model;

public class Staff extends User {
    private final boolean monthlyPass;
    public Staff(String id, boolean monthlyPass) { super(id); this.monthlyPass = monthlyPass; }
    @Override public boolean hasFreeParking() { return monthlyPass; }
}
