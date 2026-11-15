import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Task 5 - static members and the object life cycle.
 *
 * Compile: javac StaticMembersDemo.java
 * Run    : java StaticMembersDemo input.txt
 */
public class StaticMembersDemo {

    // ===================== CORRECT: class-level counter =====================
    static class Ticket {
        private static long nextNumber;           // ONE copy for the whole program
        static final String PREFIX;

        static {                                  // static block: runs once, on class loading
            nextNumber = 1001;
            PREFIX = "CMP";
            System.out.println("  [static block] Ticket class loaded, counter starts at " + nextNumber);
        }

        private final long number;
        private final String regNo;
        private final String gate;

        Ticket(String regNo, String gate) {
            this.number = nextNumber++;
            this.regNo = regNo;
            this.gate = gate;
        }
        static long issuedSoFar() { return nextNumber - 1001; }   // static method: no object needed
        @Override public String toString() { return PREFIX + number + " " + regNo + " @" + gate; }
    }

    static class Facility {
        private static int campusOccupancy = 0;   // total across ALL facilities
        private final String name;
        private int occupancy = 0;                // per facility (instance is correct here)

        Facility(String name) { this.name = name; }

        /** Object returned from a method: caller receives a reference to the new heap object. */
        Ticket enter(String regNo) {
            occupancy++;
            campusOccupancy++;
            return new Ticket(regNo, name);
        }
        /** Object passed to a method: a copy of the REFERENCE is passed. */
        void exit(Ticket t) {
            occupancy--;
            campusOccupancy--;
            t = null;                             // only the local copy is cleared - caller unaffected
        }
        static int getCampusOccupancy() { return campusOccupancy; }
    }

    // ===================== WRONG: counter as an instance variable =====================
    static class FacilityWithOwnCounter {
        private long nextNumber = 1001;           // each facility object has its own copy!
        private final String name;
        FacilityWithOwnCounter(String name) { this.name = name; }
        String issue(String regNo) { return "CMP" + (nextNumber++) + " " + regNo + " @" + name; }
    }

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(args.length > 0 ? args[0] : "input.txt"))
                .stream().filter(l -> !l.isBlank() && !l.startsWith("#")).toList();

        System.out.println("=== Instance counter (WRONG) with two gates ===");
        FacilityWithOwnCounter northBad = new FacilityWithOwnCounter("NORTH");
        FacilityWithOwnCounter southBad = new FacilityWithOwnCounter("SOUTH");
        for (String l : lines) {
            String[] p = l.split(",");
            FacilityWithOwnCounter f = p[0].trim().equals("NORTH") ? northBad : southBad;
            System.out.println("  " + f.issue(p[1].trim()));
        }
        System.out.println("  -> CMP1001 issued twice: ticket numbers are REUSED across gates.");

        System.out.println("\n=== static counter (CORRECT) with two gates ===");
        System.out.println("  Ticket.issuedSoFar() before any object -> (triggers class loading)");
        System.out.println("  issued so far = " + Ticket.issuedSoFar());
        Facility north = new Facility("NORTH");
        Facility south = new Facility("SOUTH");
        Ticket keep = null;
        for (String l : lines) {
            String[] p = l.split(",");
            Facility f = p[0].trim().equals("NORTH") ? north : south;
            Ticket t = f.enter(p[1].trim());
            if (keep == null) keep = t;
            System.out.println("  " + t);
        }
        System.out.println("  north=" + north.occupancy + " south=" + south.occupancy
                + " campus(static)=" + Facility.getCampusOccupancy()
                + " issued=" + Ticket.issuedSoFar());

        System.out.println("\n=== Passing objects: reference is passed by value ===");
        north.exit(keep);
        System.out.println("  after exit(keep): caller's keep still -> " + keep);
        System.out.println("  campus occupancy now " + Facility.getCampusOccupancy());

        System.out.println("\n=== Object life cycle of a ticket after the vehicle leaves ===");
        WeakReference<Ticket> watch = new WeakReference<>(keep);
        keep = null;                              // last strong reference removed
        System.out.println("  strong reference set to null -> object is now ELIGIBLE for GC");
        for (int i = 0; i < 10 && watch.get() != null; i++) { System.gc(); Thread.yield(); }
        System.out.println("  after System.gc() request, weak ref sees: "
                + (watch.get() == null ? "null (collected)" : "still alive (GC not forced)"));
    }
}
