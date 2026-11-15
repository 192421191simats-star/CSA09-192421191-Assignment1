package parking.model;

/** Immutable result of an exit. */
public record Receipt(long ticketNo, String regNo, String vehicleType, String userCategory,
                      String slot, long minutes, double parkingFee, double chargingFee) {
    public double total() { return parkingFee + chargingFee; }

    @Override public String toString() {
        return String.format("Receipt#%d %-11s %-11s %-12s %-9s %4d min  parking Rs %6.1f  charging Rs %6.1f  TOTAL Rs %6.1f",
                ticketNo, regNo, vehicleType, userCategory, slot, minutes, parkingFee, chargingFee, total());
    }
}
