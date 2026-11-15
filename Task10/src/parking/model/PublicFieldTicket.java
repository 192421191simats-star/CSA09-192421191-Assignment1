package parking.model;

/** Alternative concept: public fields, validation left to the callers. */
public class PublicFieldTicket {
    public long ticketNo;
    public String regNo;
    public int floor;
    public int slot;
    public int durationMinutes;
}
