package parking.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import parking.model.ParkingTicket;
import parking.model.Receipt;
import parking.model.User;
import parking.model.Vehicle;
import parking.service.ParkingException;
import parking.service.ParkingLot;
import parking.service.VehicleFactory;

/**
 * Replays one day of gate events from a text file and prints the day's collection.
 *
 * File format:
 *   DATE yyyy-mm-dd
 *   FLOORS n1 n2 ...            slots on each floor
 *   EV_SLOTS k                  last k slots of each floor have chargers
 *   ENTER hh:mm REG TYPE CATEGORY [PASS|DAYPASS]
 *   EXIT  hh:mm REG [kWh]
 *   RELEASE floor slot
 *   SHOW                        print the occupancy map
 *
 * Compile (from Task11): javac -d out $(find src -name "*.java")
 * Run                  : java -cp out parking.app.ParkingApp input/day_scenario_v2.txt
 */
public class ParkingApp {
    public static void main(String[] args) throws IOException {
        Path file = Path.of(args.length > 0 ? args[0] : "input/day_scenario_v2.txt");
        LocalDate date = LocalDate.now();
        int[] floors = {20};
        int ev = 0;
        ParkingLot lot = null;

        for (String raw : Files.readAllLines(file)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\\s+");
            switch (p[0]) {
                case "DATE" -> date = LocalDate.parse(p[1]);
                case "FLOORS" -> floors = Arrays.stream(p).skip(1).mapToInt(Integer::parseInt).toArray();
                case "EV_SLOTS" -> ev = Integer.parseInt(p[1]);
                default -> {
                    if (lot == null) {
                        lot = new ParkingLot("MAIN", floors, ev);
                        System.out.println("Facility " + Arrays.toString(floors) + " slots/floor, "
                                + ev + " EV slot(s) per floor, capacity " + lot.capacity() + ", date " + date);
                        System.out.println("--------------------------------------------------------------------------------------------------------");
                    }
                    handle(lot, date, p);
                }
            }
        }
        if (lot == null) return;
        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.println("Occupancy at close:");
        System.out.print(lot.occupancyMap());
        System.out.printf("Vehicles served: %d | still inside: %d | next ticket no: %d%n",
                lot.getVehiclesServed(), lot.occupied(), ParkingTicket.peekNextNumber());
        System.out.printf("One-day pass sales: Rs %.1f%n", lot.getPassSales());
        System.out.printf("TOTAL COLLECTION FOR %s : Rs %.1f%n", date, lot.getTotalCollection());
    }

    private static void handle(ParkingLot lot, LocalDate date, String[] p) {
        String cmd = p[0];
        try {
            switch (cmd) {
                case "ENTER" -> {
                    LocalDateTime at = LocalDateTime.of(date, LocalTime.parse(p[1]));
                    Vehicle v = VehicleFactory.create(p[3], p[2]);
                    String pass = p.length > 5 ? p[5] : "";
                    User u = VehicleFactory.createUser(p[4], "U-" + p[2], pass, date);
                    ParkingTicket t = lot.enter(v, u, at);
                    System.out.printf("%s ENTER   %s%n", p[1], t);
                }
                case "EXIT" -> {
                    LocalDateTime at = LocalDateTime.of(date, LocalTime.parse(p[1]));
                    int kWh = p.length > 3 ? Integer.parseInt(p[3]) : 0;
                    Receipt r = lot.exit(p[2], at, kWh);
                    System.out.printf("%s EXIT    %s%n", p[1], r);
                }
                case "RELEASE" -> {
                    boolean ok = lot.release(Integer.parseInt(p[1]), Integer.parseInt(p[2]));
                    System.out.printf("      RELEASE floor %s slot %s -> %s%n", p[1], p[2],
                            ok ? "released" : "REJECTED (invalid or already free)");
                }
                case "SHOW" -> System.out.print("      Occupancy now (" + lot.occupied() + "/" + lot.capacity() + "):\n" + lot.occupancyMap());
                default -> System.out.println("      UNKNOWN COMMAND " + String.join(" ", p));
            }
        } catch (ParkingException e) {
            System.out.printf("%s %-7s REJECTED: %s%n", p.length > 1 ? p[1] : "", cmd, e.getMessage());
        }
    }
}
