package parking.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import parking.model.Car;
import parking.model.DeliveryVan;
import parking.model.EBike;
import parking.model.ElectricCar;
import parking.model.MonthlyPass;
import parking.model.OneDayPass;
import parking.model.Pass;
import parking.model.Staff;
import parking.model.Student;
import parking.model.TwoWheeler;
import parking.model.User;
import parking.model.Vehicle;
import parking.model.Visitor;

/**
 * MODIFIED (Task 12): the switch was replaced by a registry, so a FUTURE vehicle
 * type is added with one register(...) call instead of editing this class again.
 */
public final class VehicleFactory {
    private static final Map<String, Function<String, Vehicle>> REGISTRY = new LinkedHashMap<>();

    static {
        register("TWO_WHEELER", TwoWheeler::new);
        register("CAR", Car::new);
        register("ELECTRIC_CAR", ElectricCar::new);
        register("E_BIKE", EBike::new);                // new category
        register("DELIVERY_VAN", DeliveryVan::new);    // new category
    }

    private VehicleFactory() { }

    public static void register(String type, Function<String, Vehicle> constructor) {
        REGISTRY.put(type.toUpperCase(), constructor);
    }

    public static Vehicle create(String type, String regNo) throws ParkingException {
        Function<String, Vehicle> ctor = REGISTRY.get(type.toUpperCase());
        if (ctor == null) throw new ParkingException("Unknown vehicle type: " + type);
        try {
            return ctor.apply(regNo);
        } catch (IllegalArgumentException e) {
            throw new ParkingException(e.getMessage());
        }
    }

    /**
     * @param passCode "" (none), "PASS" (monthly) or "DAYPASS" (one-day, visitors)
     */
    public static User createUser(String category, String id, String passCode, LocalDate day)
            throws ParkingException {
        User u = switch (category.toUpperCase()) {
            case "STUDENT" -> new Student(id);
            case "STAFF" -> new Staff(id);
            case "VISITOR" -> new Visitor(id);
            default -> throw new ParkingException("Unknown user category: " + category);
        };
        Pass pass = switch (passCode.toUpperCase()) {
            case "" -> null;
            case "PASS" -> new MonthlyPass(YearMonth.from(day));
            case "DAYPASS" -> new OneDayPass(day);
            default -> throw new ParkingException("Unknown pass: " + passCode);
        };
        try {
            u.setPass(pass);
        } catch (IllegalArgumentException e) {
            throw new ParkingException(e.getMessage());
        }
        return u;
    }
}
