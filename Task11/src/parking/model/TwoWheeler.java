package parking.model;

public class TwoWheeler extends Vehicle {
    public TwoWheeler(String regNo) { super(regNo); }
    @Override protected int ratePerHour() { return 10; }
    /** Two-wheelers park free for the first 30 minutes. */
    @Override protected int graceMinutes() { return 30; }
}
