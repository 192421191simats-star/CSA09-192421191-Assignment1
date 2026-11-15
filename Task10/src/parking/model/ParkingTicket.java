package parking.model;

/**
 * Encapsulated ticket: private fields, validating setters, no way to put the
 * object into an invalid state from outside the class.
 */
public class ParkingTicket {
    public static final int MAX_FLOORS = 5;              // public constant: safe to share
    public static final int MAX_SLOTS_PER_FLOOR = 100;

    private static long counter = 1000;                  // private static: nobody can reset it
    private final long ticketNo;
    private final Vehicle vehicle;
    private int floor;
    private int slot;
    private int durationMinutes;                         // set on exit
    private boolean paid;

    public ParkingTicket(Vehicle vehicle, int floor, int slot) {
        if (vehicle == null) throw new IllegalArgumentException("vehicle required");
        this.ticketNo = ++counter;
        this.vehicle = vehicle;
        setPosition(floor, slot);                        // re-use the same validation
    }

    public long getTicketNo() { return ticketNo; }
    public Vehicle getVehicle() { return vehicle; }
    public int getFloor() { return floor; }
    public int getSlot() { return slot; }
    public int getDurationMinutes() { return durationMinutes; }
    public boolean isPaid() { return paid; }

    /** Validating setter: rejects an invalid slot number. */
    public void setPosition(int floor, int slot) {
        if (floor < 0 || floor >= MAX_FLOORS)
            throw new IllegalArgumentException("Invalid floor " + floor + " (0.." + (MAX_FLOORS - 1) + ")");
        if (slot < 0 || slot >= MAX_SLOTS_PER_FLOOR)
            throw new IllegalArgumentException("Invalid slot " + slot + " (0.." + (MAX_SLOTS_PER_FLOOR - 1) + ")");
        this.floor = floor;
        this.slot = slot;
    }

    /** Validating setter: rejects a negative duration. */
    public void setDurationMinutes(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Duration cannot be negative: " + minutes);
        this.durationMinutes = minutes;
    }

    /** Package-private (default): only parking.model classes may call it. */
    void markPaid() { paid = true; }

    @Override public String toString() {
        return "Ticket#" + ticketNo + " " + vehicle + " F" + floor + "-S" + slot
                + " " + durationMinutes + "min" + (paid ? " PAID" : "");
    }
}
