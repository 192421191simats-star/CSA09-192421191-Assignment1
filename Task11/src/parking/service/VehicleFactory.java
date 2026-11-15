package parking.service;

import parking.model.Car;
import parking.model.ElectricCar;
import parking.model.Staff;
import parking.model.Student;
import parking.model.TwoWheeler;
import parking.model.User;
import parking.model.Vehicle;
import parking.model.Visitor;

/** Converts text input into objects; the ONLY place that knows the type names. */
public final class VehicleFactory {
    private VehicleFactory() { }

    public static Vehicle create(String type, String regNo) throws ParkingException {
        try {
            switch (type.toUpperCase()) {
                case "TWO_WHEELER":  return new TwoWheeler(regNo);
                case "CAR":          return new Car(regNo);
                case "ELECTRIC_CAR": return new ElectricCar(regNo);
                default: throw new ParkingException("Unknown vehicle type: " + type);
            }
        } catch (IllegalArgumentException e) {
            throw new ParkingException(e.getMessage());
        }
    }

    public static User createUser(String category, String id, boolean pass) throws ParkingException {
        switch (category.toUpperCase()) {
            case "STUDENT": return new Student(id, pass);
            case "STAFF":   return new Staff(id, pass);
            case "VISITOR":
                if (pass) throw new ParkingException("Visitors cannot hold a monthly pass");
                return new Visitor(id);
            default: throw new ParkingException("Unknown user category: " + category);
        }
    }
}
