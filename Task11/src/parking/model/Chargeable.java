package parking.model;

/** Capability of vehicles that can use a charging point. Charged separately from parking. */
public interface Chargeable {
    double RATE_PER_KWH = 12.0;

    double chargingFee(int kWh);

    /** Shared validation for every implementer. */
    static void checkKWh(int kWh) {
        if (kWh < 0 || kWh > 150) throw new IllegalArgumentException("Invalid kWh: " + kWh);
    }
}
