package parking.service;

import java.time.Duration;
import java.time.LocalDateTime;
import parking.model.Chargeable;
import parking.model.ParkingSlot;
import parking.model.ParkingTicket;
import parking.model.Receipt;
import parking.model.User;
import parking.model.Vehicle;

/**
 * The parking facility. Floors are a jagged array of ParkingSlot objects so each
 * floor may have a different number of slots. Active tickets are kept in an array.
 *
 * MODIFIED (Task 12): (1) free parking is checked with the user's Pass for the
 * entry date; (2) one-day pass sales are added to the day's collection.
 * The jagged floor array needed NO change for 5 floors of different sizes.
 */
public class ParkingLot {
    private final String name;
    private final ParkingSlot[][] floors;          // floors[f][s]
    private final ParkingTicket[] active;          // open tickets, compacted 0..activeCount-1
    private int activeCount;
    private double totalCollection;
    private double passSales;
    private int vehiclesServed;

    /**
     * @param slotsPerFloor     number of slots on each floor (may differ)
     * @param evSlotsPerFloor   the last k slots of every floor have a charging point
     */
    public ParkingLot(String name, int[] slotsPerFloor, int evSlotsPerFloor) {
        if (slotsPerFloor == null || slotsPerFloor.length == 0)
            throw new IllegalArgumentException("At least one floor is required");
        this.name = name;
        floors = new ParkingSlot[slotsPerFloor.length][];
        int total = 0;
        for (int f = 0; f < slotsPerFloor.length; f++) {
            if (slotsPerFloor[f] <= 0) throw new IllegalArgumentException("Floor " + f + " needs slots");
            floors[f] = new ParkingSlot[slotsPerFloor[f]];
            for (int s = 0; s < slotsPerFloor[f]; s++)
                floors[f][s] = new ParkingSlot(f, s, s >= slotsPerFloor[f] - evSlotsPerFloor);
            total += slotsPerFloor[f];
        }
        active = new ParkingTicket[total];
    }

    // ------------------------------------------------------------------ entry
    public ParkingTicket enter(Vehicle v, User u, LocalDateTime at) throws ParkingException {
        if (findActive(v.getRegNo()) >= 0)
            throw new ParkingException("Duplicate registration " + v.getRegNo() + " is already inside");
        ParkingSlot slot = allocate(v);
        if (slot == null) throw new ParkingException("No suitable free slot for " + v);
        slot.park(v);
        ParkingTicket t = new ParkingTicket(v, u, slot, at);
        active[activeCount++] = t;
        if (u.getPass() != null && u.getPass().price() > 0) {   // pass bought at the gate
            passSales += u.getPass().price();
            totalCollection += u.getPass().price();
        }
        return t;
    }

    /**
     * EVs: charging slot first, then a normal slot.
     * Other vehicles: normal slots only (charging slots are reserved for EVs).
     */
    private ParkingSlot allocate(Vehicle v) {
        if (v.needsChargingSlot()) {
            ParkingSlot s = firstFree(true);
            if (s != null) return s;
        }
        return firstFree(false);
    }

    private ParkingSlot firstFree(boolean charging) {
        for (ParkingSlot[] floor : floors) {
            for (ParkingSlot s : floor) {
                if (s.hasChargingPoint() != charging) continue;   // wrong kind, skip
                if (s.isFree()) return s;                          // first match ends the search
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ exit
    public Receipt exit(String regNo, LocalDateTime at, int kWh) throws ParkingException {
        int i = findActive(regNo);
        if (i < 0) throw new ParkingException("Vehicle " + regNo + " is not parked here");
        ParkingTicket t = active[i];
        Vehicle v = t.getVehicle();
        User u = t.getUser();
        double parking, charging = 0;
        try {                                              // validate everything BEFORE changing state
            boolean free = u.hasFreeParking(t.getEntryTime().toLocalDate());
            parking = free ? 0 : v.calculateFee(t.getEntryTime(), at);
            if (kWh > 0) {
                if (!(v instanceof Chargeable c))
                    throw new ParkingException(v + " cannot be charged");
                charging = c.chargingFee(kWh);            // pass holders still pay for energy
            }
            t.close(at);
        } catch (IllegalArgumentException e) {
            throw new ParkingException(e.getMessage());
        }
        t.getSlot().vacate();
        active[i] = active[--activeCount];                 // O(1) removal: move last into the hole
        active[activeCount] = null;                        // drop reference -> ticket can be GC'd
        totalCollection += parking + charging;
        vehiclesServed++;
        long minutes = Duration.between(t.getEntryTime(), at).toMinutes();
        String who = u.category() + (u.getPass() != null ? "+" + u.getPass().label() : "");
        return new Receipt(t.getTicketNo(), regNo, v.typeName(), who, t.getSlot().label(), minutes, parking, charging);
    }

    /** Frees a slot directly (attendant override). False if invalid position or already free. */
    public boolean release(int floor, int slot) {
        if (floor < 0 || floor >= floors.length) return false;
        if (slot < 0 || slot >= floors[floor].length) return false;
        ParkingSlot s = floors[floor][slot];
        if (s.isFree()) return false;
        int i = findActive(s.getOccupant().getRegNo());
        if (i >= 0) { active[i] = active[--activeCount]; active[activeCount] = null; }
        s.vacate();
        return true;
    }

    /** Linear search of the active-ticket array: O(n). */
    private int findActive(String regNo) {
        for (int i = 0; i < activeCount; i++)
            if (active[i].getVehicle().getRegNo().equals(regNo)) return i;
        return -1;
    }

    // ------------------------------------------------------------------ queries
    public int freeSlots() {
        int n = 0;
        for (ParkingSlot[] floor : floors) for (ParkingSlot s : floor) if (s.isFree()) n++;
        return n;
    }
    public int capacity() { return active.length; }
    public int occupied() { return activeCount; }
    public double getTotalCollection() { return totalCollection; }
    public double getPassSales() { return passSales; }
    public int getVehiclesServed() { return vehiclesServed; }
    public int floorCount() { return floors.length; }
    public int slotsOnFloor(int f) { return floors[f].length; }
    public String getName() { return name; }

    public String occupancyMap() {
        StringBuilder sb = new StringBuilder();
        for (int f = 0; f < floors.length; f++) {
            sb.append("    floor ").append(f).append(": ");
            for (ParkingSlot s : floors[f])
            {
                String cell;
                if (s.isFree()) cell = s.hasChargingPoint() ? "ev" : ".";
                else {
                    String r = s.getOccupant().getRegNo();
                    cell = r.substring(Math.max(0, r.length() - 4));
                }
                sb.append(String.format("[%-4s]", cell));
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
