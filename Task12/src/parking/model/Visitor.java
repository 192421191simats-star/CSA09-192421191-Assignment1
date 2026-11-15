package parking.model;

/** MODIFIED (Task 12): a visitor may now buy a one-day pass (but never a monthly pass). */
public class Visitor extends User {
    public Visitor(String id) { super(id); }
    public Visitor(String id, Pass pass) { super(id); setPass(pass); }
    @Override protected boolean mayHold(Pass p) { return p instanceof OneDayPass; }
}
