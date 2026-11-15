package parking.model;

/** MODIFIED (Task 12): pass handling moved to User; Staff only states which pass it may hold. */
public class Staff extends User {
    public Staff(String id) { super(id); }
    public Staff(String id, Pass pass) { super(id); setPass(pass); }
    @Override protected boolean mayHold(Pass p) { return p instanceof MonthlyPass; }
}
