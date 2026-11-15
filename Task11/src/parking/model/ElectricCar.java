package parking.model;

/** Multilevel: Vehicle -> Car -> ElectricCar, plus the Chargeable capability. */
public class ElectricCar extends Car implements Chargeable {
    public ElectricCar(String regNo) { super(regNo); }

    @Override public boolean needsChargingSlot() { return true; }

    @Override public double chargingFee(int kWh) {
        Chargeable.checkKWh(kWh);
        return RATE_PER_KWH * kWh;
    }
}
