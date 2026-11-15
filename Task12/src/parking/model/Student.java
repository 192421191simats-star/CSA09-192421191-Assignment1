package parking.model;

/** MODIFIED (Task 12): pass handling moved to User; Student only states which pass it may hold. */
public class Student extends User {
    public Student(String id) { super(id); }
    public Student(String id, Pass pass) { super(id); setPass(pass); }
    @Override protected boolean mayHold(Pass p) { return p instanceof MonthlyPass; }
}
