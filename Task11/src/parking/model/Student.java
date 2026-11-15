package parking.model;

public class Student extends User {
    private final boolean monthlyPass;
    public Student(String id, boolean monthlyPass) { super(id); this.monthlyPass = monthlyPass; }
    @Override public boolean hasFreeParking() { return monthlyPass; }
}
