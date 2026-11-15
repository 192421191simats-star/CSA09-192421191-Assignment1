package parking.model;

import java.time.LocalDateTime;

/** Issued on entry; closed on exit. Ticket numbers come from a static counter and are never reused. */
public class ParkingTicket {
    private static long nextNumber = 1001;

    private final long ticketNo;
    private final Vehicle vehicle;
    private final User user;
    private final ParkingSlot slot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;

    public ParkingTicket(Vehicle vehicle, User user, ParkingSlot slot, LocalDateTime entryTime) {
        if (vehicle == null || user == null || slot == null || entryTime == null)
            throw new IllegalArgumentException("Ticket needs vehicle, user, slot and entry time");
        this.ticketNo = nextNumber++;
        this.vehicle = vehicle;
        this.user = user;
        this.slot = slot;
        this.entryTime = entryTime;
    }

    public long getTicketNo() { return ticketNo; }
    public Vehicle getVehicle() { return vehicle; }
    public User getUser() { return user; }
    public ParkingSlot getSlot() { return slot; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public boolean isClosed() { return exitTime != null; }

    public void close(LocalDateTime exit) {
        if (isClosed()) throw new IllegalStateException("Ticket " + ticketNo + " already closed");
        if (exit == null || exit.isBefore(entryTime))
            throw new IllegalArgumentException("Exit time before entry time");
        exitTime = exit;
    }

    public static long peekNextNumber() { return nextNumber; }

    @Override public String toString() {
        return "Ticket#" + ticketNo + " " + vehicle + " " + user + " " + slot.label()
                + " in " + entryTime.toLocalTime();
    }
}
