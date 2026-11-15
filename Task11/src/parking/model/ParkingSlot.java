package parking.model;

/** One numbered slot on one floor. A slot holds at most one vehicle. */
public class ParkingSlot {
    private final int floor;
    private final int number;
    private final boolean chargingPoint;
    private Vehicle occupant;

    public ParkingSlot(int floor, int number) { this(floor, number, false); }

    public ParkingSlot(int floor, int number, boolean chargingPoint) {
        if (floor < 0 || number < 0) throw new IllegalArgumentException("Invalid slot position");
        this.floor = floor;
        this.number = number;
        this.chargingPoint = chargingPoint;
    }

    public boolean isFree() { return occupant == null; }
    public boolean hasChargingPoint() { return chargingPoint; }
    public int getFloor() { return floor; }
    public int getNumber() { return number; }
    public Vehicle getOccupant() { return occupant; }

    /** Business rule: a slot cannot hold two vehicles. */
    public void park(Vehicle v) {
        if (!isFree()) throw new IllegalStateException("Slot " + label() + " already occupied");
        occupant = v;
    }

    public Vehicle vacate() {
        Vehicle v = occupant;
        occupant = null;
        return v;
    }

    public String label() { return "F" + floor + "-S" + number + (chargingPoint ? "(EV)" : ""); }

    @Override public String toString() { return label() + (isFree() ? " free" : " " + occupant); }
}
