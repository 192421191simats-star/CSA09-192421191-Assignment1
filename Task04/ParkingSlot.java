/** A numbered slot on a floor. Shows default + parameterised + chained constructors. */
public class ParkingSlot {
    private final int floor;
    private final int number;
    private final boolean chargingPoint;
    private String occupiedBy;            // null = free

    /** Default constructor: ground floor, slot 0, normal slot (used for quick tests). */
    public ParkingSlot() {
        this(0, 0);                       // chain -> 2-arg constructor
    }

    /** Overloaded: normal (non-charging) slot. */
    public ParkingSlot(int floor, int number) {
        this(floor, number, false);       // chain -> full constructor
    }

    /** Full constructor: the ONLY place where fields are validated and assigned. */
    public ParkingSlot(int floor, int number, boolean chargingPoint) {
        if (floor < 0 || number < 0) throw new IllegalArgumentException("negative floor/slot");
        this.floor = floor;               // 'this' separates the field from the parameter
        this.number = number;
        this.chargingPoint = chargingPoint;
    }

    public boolean isFree() { return occupiedBy == null; }
    public boolean occupy(String regNo) {
        if (!isFree()) return false;      // business rule: one vehicle per slot
        occupiedBy = regNo;
        return true;
    }
    public void vacate() { occupiedBy = null; }
    public int getFloor() { return floor; }
    public int getNumber() { return number; }
    public boolean hasChargingPoint() { return chargingPoint; }

    @Override public String toString() {
        return "F" + floor + "-S" + number + (chargingPoint ? "(EV)" : "")
                + (isFree() ? " free" : " [" + occupiedBy + "]");
    }
}
