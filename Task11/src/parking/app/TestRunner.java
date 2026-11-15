package parking.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import parking.model.Car;
import parking.model.ElectricCar;
import parking.model.ParkingTicket;
import parking.model.Receipt;
import parking.model.Staff;
import parking.model.Student;
import parking.model.TwoWheeler;
import parking.model.Vehicle;
import parking.model.Visitor;
import parking.service.ParkingException;
import parking.service.ParkingLot;
import parking.service.VehicleFactory;

/**
 * Self-contained test harness (no external libraries). Prints a test table with
 * expected and actual results and writes it to test_results.csv.
 *
 * Run: java -cp out parking.app.TestRunner
 */
public class TestRunner {
    private static final List<String[]> rows = new ArrayList<>();
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 16, 8, 0);

    private static void check(String id, String kind, String what, Object expected, Object actual) {
        boolean pass = String.valueOf(expected).equals(String.valueOf(actual));
        rows.add(new String[]{id, kind, what, String.valueOf(expected), String.valueOf(actual), pass ? "PASS" : "FAIL"});
    }

    private static String attempt(ThrowingSupplier s) {
        try { return String.valueOf(s.get()); }
        catch (ParkingException e) { return "REJECTED: " + e.getMessage(); }
        catch (RuntimeException e) { return "REJECTED: " + e.getMessage(); }
    }
    @FunctionalInterface interface ThrowingSupplier { Object get() throws ParkingException; }

    private static double fee(ParkingLot lot, Vehicle v, parking.model.User u, int minutes, int kWh) throws ParkingException {
        lot.enter(v, u, T0);
        return lot.exit(v.getRegNo(), T0.plusMinutes(minutes), kWh).total();
    }

    public static void main(String[] args) throws Exception {
        // ---------------- normal operation ----------------
        ParkingLot lot = new ParkingLot("T", new int[]{10, 10}, 2);
        check("N1", "Normal", "Visitor car 130 min", 90.0, fee(lot, new Car("TN01AB1111"), new Visitor("v"), 130, 0));
        check("N2", "Normal", "Visitor two-wheeler 130 min", 30.0, fee(lot, new TwoWheeler("TN22CD2222"), new Visitor("v"), 130, 0));
        check("N3", "Normal", "Visitor EV 130 min + 10 kWh", 210.0, fee(lot, new ElectricCar("TN07EV3333"), new Visitor("v"), 130, 10));
        check("N4", "Normal", "Student with pass, car 130 min", 0.0, fee(lot, new Car("TN10GH4444"), new Student("s", true), 130, 0));
        check("N5", "Normal", "Student WITHOUT pass, car 130 min", 90.0, fee(lot, new Car("TN10GH4445"), new Student("s", false), 130, 0));
        check("N6", "Normal", "Staff pass EV 60 min + 5 kWh (energy still paid)", 60.0, fee(lot, new ElectricCar("TN05MN5555"), new Staff("e", true), 60, 5));
        check("N7", "Normal", "Lot empty again after all exits", 0, lot.occupied());

        // ---------------- boundary cases ----------------
        check("B1", "Boundary", "Zero-minute stay, car", 0.0, fee(lot, new Car("TN01BB0001"), new Visitor("v"), 0, 0));
        check("B2", "Boundary", "Exactly 60 min (slab edge), car", 30.0, fee(lot, new Car("TN01BB0002"), new Visitor("v"), 60, 0));
        check("B3", "Boundary", "61 min -> 2nd hour starts, car", 60.0, fee(lot, new Car("TN01BB0003"), new Visitor("v"), 61, 0));
        check("B4", "Boundary", "Two-wheeler exactly 30 min (grace edge)", 0.0, fee(lot, new TwoWheeler("TN01BB0004"), new Visitor("v"), 30, 0));
        check("B5", "Boundary", "Two-wheeler 31 min", 10.0, fee(lot, new TwoWheeler("TN01BB0005"), new Visitor("v"), 31, 0));
        check("B6", "Boundary", "Car 480 min = daily maximum", 240.0, fee(lot, new Car("TN01BB0006"), new Visitor("v"), 480, 0));
        check("B7", "Boundary", "Car 600 min capped at daily max", 240.0, fee(lot, new Car("TN01BB0007"), new Visitor("v"), 600, 0));
        check("B8", "Boundary", "Car 1500 min = 1 day + 1 h", 270.0, fee(lot, new Car("TN01BB0008"), new Visitor("v"), 1500, 0));

        ParkingLot small = new ParkingLot("S", new int[]{2, 1}, 0);     // 3 normal slots
        small.enter(new Car("TN01CC0001"), new Visitor("v"), T0);
        small.enter(new Car("TN01CC0002"), new Visitor("v"), T0);
        ParkingTicket last = small.enter(new Car("TN01CC0003"), new Visitor("v"), T0);
        check("B9", "Boundary", "Last free slot is allocated", "F1-S0", last.getSlot().label());
        check("B10", "Boundary", "Entry when full is rejected", "REJECTED: No suitable free slot for Car[TN01CC0004]",
                attempt(() -> small.enter(new Car("TN01CC0004"), new Visitor("v"), T0)));
        small.exit("TN01CC0002", T0.plusMinutes(10), 0);
        check("B11", "Boundary", "Freed slot is reused", "F0-S1",
                small.enter(new Car("TN01CC0005"), new Visitor("v"), T0).getSlot().label());

        ParkingLot evLot = new ParkingLot("E", new int[]{3}, 1);       // S0,S1 normal, S2 EV
        check("B12", "Boundary", "EV is placed in charging slot", "F0-S2(EV)",
                evLot.enter(new ElectricCar("TN07EV0001"), new Visitor("v"), T0).getSlot().label());
        check("B13", "Boundary", "Second EV falls back to normal slot", "F0-S0",
                evLot.enter(new ElectricCar("TN07EV0002"), new Visitor("v"), T0).getSlot().label());
        evLot.enter(new Car("TN01DD0001"), new Visitor("v"), T0);
        evLot.exit("TN07EV0001", T0.plusMinutes(5), 0);
        check("B14", "Boundary", "Car never takes a free charging slot", "REJECTED: No suitable free slot for Car[TN01DD0002]",
                attempt(() -> evLot.enter(new Car("TN01DD0002"), new Visitor("v"), T0)));

        // ---------------- invalid cases ----------------
        check("I1", "Invalid", "Unknown vehicle type BUS", "REJECTED: Unknown vehicle type: BUS",
                attempt(() -> VehicleFactory.create("BUS", "TN55UV5050")));
        lot.enter(new Car("TN09DUP001"), new Visitor("v"), T0);
        check("I2", "Invalid", "Duplicate registration on entry", "REJECTED: Duplicate registration TN09DUP001 is already inside",
                attempt(() -> lot.enter(new TwoWheeler("TN09DUP001"), new Visitor("v"), T0)));
        check("I3", "Invalid", "Release invalid floor 9", false, lot.release(9, 0));
        check("I4", "Invalid", "Release invalid slot -1", false, lot.release(0, -1));
        check("I5", "Invalid", "Release an already free slot", false, lot.release(1, 5));
        check("I6", "Invalid", "Exit of a vehicle not parked", "REJECTED: Vehicle TN99ZZ9999 is not parked here",
                attempt(() -> lot.exit("TN99ZZ9999", T0, 0)));
        check("I7", "Invalid", "Exit time before entry time", "REJECTED: Exit time before entry time",
                attempt(() -> lot.exit("TN09DUP001", T0.minusMinutes(1), 0)));
        check("I8", "Invalid", "Malformed registration number", "REJECTED: Invalid registration number: tn-01",
                attempt(() -> VehicleFactory.create("CAR", "tn-01")));
        check("I9", "Invalid", "Negative duration passed to calculateFee", "REJECTED: Negative duration: -5",
                attempt(() -> new Car("TN01EE0001").calculateFee(-5)));
        check("I10", "Invalid", "Charging requested for a petrol car", "REJECTED: Car[TN09DUP001] cannot be charged",
                attempt(() -> lot.exit("TN09DUP001", T0.plusMinutes(30), 5)));
        check("I11", "Invalid", "After a rejected exit the car can still leave", 30.0,
                lot.exit("TN09DUP001", T0.plusMinutes(30), 0).total());
        check("I12", "Invalid", "Visitor claiming a monthly pass", "REJECTED: Visitors cannot hold a monthly pass",
                attempt(() -> VehicleFactory.createUser("VISITOR", "v", true)));

        // ---------------- object rules ----------------
        Set<Long> numbers = new HashSet<>();
        ParkingLot many = new ParkingLot("M", new int[]{50}, 0);
        for (int i = 0; i < 200; i++) {
            String reg = String.format("TN01FF%04d", i);
            numbers.add(many.enter(new Car(reg), new Visitor("v"), T0).getTicketNo());
            many.exit(reg, T0.plusMinutes(1), 0);
        }
        check("O1", "Rule", "200 tickets in reused slot: all numbers unique", 200, numbers.size());
        check("O2", "Rule", "Car vs ElectricCar same reg are equal()", true, new Car("TN02BB0002").equals(new ElectricCar("TN02BB0002")));
        check("O3", "Rule", "equal vehicles have equal hashCode", true, new Car("TN02BB0002").hashCode() == new ElectricCar("TN02BB0002").hashCode());

        // ---------------- hand-calculated day scenario ----------------
        String dayOut = runScenario("input/day_scenario.txt");
        String totalLine = dayOut.lines().filter(l -> l.startsWith("TOTAL")).findFirst().orElse("?");
        check("D1", "Scenario", "Day total (hand calc 90+180+120+360+30+240)", "Rs 1020.0",
                totalLine.substring(totalLine.indexOf("Rs")));

        // ---------------- print table ----------------
        String fmt = "%-4s %-9s %-48s %-38s %-38s %s%n";
        System.out.printf(fmt, "ID", "Type", "Test case", "Expected", "Actual", "Result");
        System.out.println("-".repeat(146));
        int passed = 0;
        for (String[] r : rows) {
            System.out.printf(fmt, r[0], r[1], r[2], cut(r[3]), cut(r[4]), r[5]);
            if (r[5].equals("PASS")) passed++;
        }
        System.out.println("-".repeat(146));
        System.out.println(passed + " / " + rows.size() + " tests passed");

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(Path.of("output/test_results.csv")))) {
            w.println("ID,Type,Test case,Expected,Actual,Result");
            for (String[] r : rows) w.println(String.join(",", quote(r)));
        }
    }

    /** Console view only - the CSV keeps the full text. */
    private static String cut(String s) { return s.length() <= 38 ? s : s.substring(0, 35) + "..."; }

    private static String[] quote(String[] r) {
        String[] q = new String[r.length];
        for (int i = 0; i < r.length; i++) q[i] = "\"" + r[i].replace("\"", "'") + "\"";
        return q;
    }

    private static String runScenario(String file) throws IOException {
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        java.io.PrintStream old = System.out;
        System.setOut(new java.io.PrintStream(buf));
        try { ParkingApp.main(new String[]{file}); } finally { System.setOut(old); }
        return buf.toString();
    }
}
