import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Task 1 - Understand the Problem and Identify the Objects.
 *
 * Reads the vehicles that entered the campus from input.txt and stores them
 * in TWO ways so the designs can be compared:
 *   (a) object design   : ParkedVehicle[]  (one object per vehicle)
 *   (b) parallel arrays : regNo[], type[], entryTime[]
 * It then searches both by registration number and times a large search.
 *
 * Compile: javac ObjectsVsParallelArrays.java
 * Run    : java ObjectsVsParallelArrays input.txt
 */
public class ObjectsVsParallelArrays {

    /** Candidate class "Vehicle" in its simplest form: data + behaviour together. */
    static class ParkedVehicle {
        private final String regNo;
        private final String type;
        private final String entryTime;
        // Adding a new attribute = ONE new field here; every array element gets it.
        private String ownerCategory = "VISITOR";

        ParkedVehicle(String regNo, String type, String entryTime) {
            this.regNo = regNo;
            this.type = type;
            this.entryTime = entryTime;
        }
        String getRegNo() { return regNo; }
        @Override public String toString() {
            return regNo + " (" + type + ", in " + entryTime + ", " + ownerCategory + ")";
        }
    }

    // ---------- design (a): array of objects ----------
    static int findObject(ParkedVehicle[] vehicles, int count, String regNo) {
        for (int i = 0; i < count; i++) {
            if (vehicles[i].getRegNo().equals(regNo)) return i;
        }
        return -1;
    }

    // ---------- design (b): parallel arrays ----------
    static int findParallel(String[] regNos, int count, String regNo) {
        for (int i = 0; i < count; i++) {
            if (regNos[i].equals(regNo)) return i;
        }
        return -1;
    }

    public static void main(String[] args) throws IOException {
        Path file = Path.of(args.length > 0 ? args[0] : "input.txt");
        List<String> lines = Files.readAllLines(file).stream()
                .filter(l -> !l.isBlank() && !l.startsWith("#")).toList();

        int n = lines.size();
        ParkedVehicle[] vehicles = new ParkedVehicle[n];
        String[] regNo = new String[n], type = new String[n], entryTime = new String[n];

        for (int i = 0; i < n; i++) {
            String[] p = lines.get(i).split(",");
            vehicles[i] = new ParkedVehicle(p[0].trim(), p[1].trim(), p[2].trim());
            regNo[i] = p[0].trim();
            type[i] = p[1].trim();
            entryTime[i] = p[2].trim();
        }

        System.out.println("=== Vehicles read from " + file + " : " + n + " ===");
        for (ParkedVehicle v : vehicles) System.out.println("  " + v);

        String[] queries = {"TN09KL7777", "TN01AB1111", "KA05ZZ0000"};
        System.out.println("\n=== Search by registration number ===");
        for (String q : queries) {
            int a = findObject(vehicles, n, q);
            int b = findParallel(regNo, n, q);
            System.out.printf("  %-11s object design -> %s | parallel arrays -> %s%n", q,
                    a >= 0 ? vehicles[a].toString() : "not found",
                    b >= 0 ? regNo[b] + " " + type[b] + " " + entryTime[b] : "not found");
        }

        // ----- Adding a new attribute -----
        System.out.println("\n=== Adding a new attribute (owner category) ===");
        System.out.println("  Object design  : 1 new field in ParkedVehicle; constructor/methods unchanged.");
        String[] ownerCategory = new String[n];       // parallel-array design needs a NEW array
        for (int i = 0; i < n; i++) ownerCategory[i] = "VISITOR";
        System.out.println("  Parallel arrays: new String[" + n + "] created; every add/remove/sort");
        System.out.println("                   routine must now update " + 4 + " arrays in step.");

        // ----- Timing a large linear search (worst case: key not present) -----
        int big = 200_000;
        ParkedVehicle[] bigObj = new ParkedVehicle[big];
        String[] bigReg = new String[big];
        for (int i = 0; i < big; i++) {
            String r = String.format("TN%02dXX%04d", i % 100, i);
            bigObj[i] = new ParkedVehicle(r, "CAR", "08:00");
            bigReg[i] = r;
        }
        for (int w = 0; w < 30; w++) {               // JIT warm-up
            findObject(bigObj, big, "NONE"); findParallel(bigReg, big, "NONE");
        }
        int reps = 50;
        long t0 = System.nanoTime();
        for (int r = 0; r < reps; r++) findObject(bigObj, big, "NONE");
        long t1 = System.nanoTime();
        for (int r = 0; r < reps; r++) findParallel(bigReg, big, "NONE");
        long t2 = System.nanoTime();
        System.out.println("\n=== Measured worst-case search, n = " + big + " (avg of " + reps + ") ===");
        System.out.printf("  Object array    : %.3f ms  -> O(n)%n", (t1 - t0) / 1e6 / reps);
        System.out.printf("  Parallel arrays : %.3f ms  -> O(n)%n", (t2 - t1) / 1e6 / reps);
        System.out.println("  Both are linear; the object design costs one extra reference hop");
        System.out.println("  per comparison (array slot -> object -> regNo String).");
    }
}
