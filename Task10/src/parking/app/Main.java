package parking.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import parking.model.Car;
import parking.model.ParkingTicket;
import parking.model.PaymentDesk;
import parking.model.PublicFieldTicket;
import parking.model.TwoWheeler;
import parking.model.Vehicle;
import parking.service.BillingService;

/**
 * Task 10 - packages, access modifiers, validated setters, wrapper-class pitfalls.
 *
 * Compile (from Task10): javac -d out $(find src -name "*.java")
 * Run                  : java -cp out parking.app.Main input.txt
 */
public class Main {
    public static void main(String[] args) throws IOException {
        BillingService billing = new BillingService();
        PaymentDesk desk = new PaymentDesk();

        System.out.println("=== Updates from input file (validated by setters) ===");
        for (String line : Files.readAllLines(Path.of(args.length > 0 ? args[0] : "input.txt"))) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] p = line.split(",");
            try {
                Vehicle v = p[1].trim().equals("TWO_WHEELER") ? new TwoWheeler(p[0].trim()) : new Car(p[0].trim());
                ParkingTicket t = new ParkingTicket(v, Integer.parseInt(p[2].trim()), Integer.parseInt(p[3].trim()));
                t.setDurationMinutes(Integer.parseInt(p[4].trim()));
                double fee = billing.fee(t);
                desk.settle(t);
                System.out.printf("  OK       %-45s fee Rs %.1f%n", t, fee);
            } catch (IllegalArgumentException e) {
                System.out.println("  REJECTED " + line.trim() + "  -> " + e.getMessage());
            }
        }
        // t.markPaid();          // would NOT compile here: markPaid() is package-private in parking.model
        // t.durationMinutes = -5; // would NOT compile: field is private

        System.out.println("\n=== Alternative: public fields, no validation ===");
        PublicFieldTicket bad = new PublicFieldTicket();
        bad.regNo = "TN01AB1111";
        bad.floor = 9;                 // facility has 5 floors
        bad.slot = -3;
        bad.durationMinutes = -120;    // negative stay
        System.out.println("  accepted floor=" + bad.floor + " slot=" + bad.slot
                + " duration=" + bad.durationMinutes + " -> fee would be Rs "
                + ((bad.durationMinutes + 59) / 60 * 30) + "  (silent corruption)");

        System.out.println("\n=== Wrapper classes and autoboxing ===");
        Integer a = 100, b = 100, c = 500, d = 500;           // autoboxing int -> Integer
        System.out.println("  Integer 100 == 100   : " + (a == b) + "   (cached -128..127, same object)");
        System.out.println("  Integer 500 == 500   : " + (c == d) + "  (two different objects!)");
        System.out.println("  c.equals(d)          : " + c.equals(d) + "   (compare values with equals)");
        Integer missing = null;                               // e.g. slot lookup that found nothing
        try {
            int slot = missing;                               // auto-unboxing null
            System.out.println("  slot " + slot);
        } catch (NullPointerException e) {
            System.out.println("  int slot = (Integer)null -> NullPointerException on unboxing");
        }
    }
}
