import java.time.LocalDateTime;

/**
 * Parking ticket. Demonstrates parameterised + overloaded constructors,
 * constructor chaining with this(...), a copy constructor, and a static factory.
 */
public class ParkingTicket {
    private static long nextNumber = 1001;        // shared counter (detailed in Task 5)

    private final long ticketNo;
    private final String regNo;
    private final int floor;
    private final int slot;
    private final LocalDateTime entryTime;
    private final boolean duplicate;

    /** Overloaded: entry time defaults to "now" (normal gate operation). */
    public ParkingTicket(String regNo, int floor, int slot) {
        this(regNo, floor, slot, LocalDateTime.now());
    }

    /** Parameterised: explicit entry time (used by tests and replayed input files). */
    public ParkingTicket(String regNo, int floor, int slot, LocalDateTime entryTime) {
        this(nextNumber++, regNo, floor, slot, entryTime, false);
    }

    /** Copy constructor: re-issue a lost ticket. SAME ticket number, marked DUPLICATE. */
    public ParkingTicket(ParkingTicket original) {
        this(original.ticketNo, original.regNo, original.floor, original.slot,
             original.entryTime, true);
    }

    /** Private master constructor - all validation lives here only once. */
    private ParkingTicket(long ticketNo, String regNo, int floor, int slot,
                          LocalDateTime entryTime, boolean duplicate) {
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("regNo required");
        if (floor < 0 || slot < 0) throw new IllegalArgumentException("invalid position");
        if (entryTime == null) throw new IllegalArgumentException("entry time required");
        this.ticketNo = ticketNo;
        this.regNo = regNo;
        this.floor = floor;
        this.slot = slot;
        this.entryTime = entryTime;
        this.duplicate = duplicate;
    }

    /** Static factory alternative: a descriptive name, same validation path. */
    public static ParkingTicket issue(String regNo, int floor, int slot, LocalDateTime at) {
        return new ParkingTicket(regNo, floor, slot, at);
    }

    public long getTicketNo() { return ticketNo; }
    public String getRegNo() { return regNo; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public boolean isDuplicate() { return duplicate; }

    @Override public String toString() {
        return "Ticket#" + ticketNo + " " + regNo + " F" + floor + "-S" + slot
                + " in " + entryTime.toLocalTime() + (duplicate ? " (DUPLICATE)" : "");
    }
}
