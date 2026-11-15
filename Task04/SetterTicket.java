import java.time.LocalDateTime;

/** Alternative concept: JavaBean style - default constructor + setters. */
public class SetterTicket {
    private long ticketNo;
    private String regNo;
    private int floor = -1, slot = -1;
    private LocalDateTime entryTime;

    public SetterTicket() { }                       // object exists BEFORE it is valid

    public void setTicketNo(long t) { ticketNo = t; }
    public void setRegNo(String r) { regNo = r; }
    public void setFloor(int f) { floor = f; }
    public void setSlot(int s) { slot = s; }
    public void setEntryTime(LocalDateTime t) { entryTime = t; }

    /** Callers must remember to check this - nothing forces them to. */
    public boolean isValid() {
        return ticketNo > 0 && regNo != null && floor >= 0 && slot >= 0 && entryTime != null;
    }
    @Override public String toString() {
        return "SetterTicket#" + ticketNo + " " + regNo + " F" + floor + "-S" + slot + " in " + entryTime;
    }
}
