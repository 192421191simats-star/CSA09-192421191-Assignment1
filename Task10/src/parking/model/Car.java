package parking.model;

public class Car extends Vehicle {
    public Car(String regNo) { super(regNo); }
    @Override protected int ratePerHour() { return 30; }
}
