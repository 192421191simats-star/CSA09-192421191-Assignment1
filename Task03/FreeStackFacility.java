import java.util.Arrays;

/**
 * Task 3 - Alternative concept: free-slot counter + stack of free slot numbers per floor.
 * Compares allocate()/release() cost against the linear scan of ParkingFacility.
 *
 * Compile: javac ParkingFacility.java FreeStackFacility.java
 * Run    : java FreeStackFacility
 */
public class FreeStackFacility {
    private final String[][] slots;
    private final int[] freeCount;      // free slots per floor
    private final int[][] freeStack;    // freeStack[f][0..freeCount[f]-1] = free slot numbers

    public FreeStackFacility(int[] slotsPerFloor) {
        int floors = slotsPerFloor.length;
        slots = new String[floors][];
        freeCount = new int[floors];
        freeStack = new int[floors][];
        for (int f = 0; f < floors; f++) {
            int n = slotsPerFloor[f];
            slots[f] = new String[n];
            freeStack[f] = new int[n];
            for (int s = 0; s < n; s++) freeStack[f][s] = n - 1 - s;  // top = slot 0
            freeCount[f] = n;
        }
    }

    /** O(F) worst case to find a floor with freeCount > 0, then O(1) pop. */
    public int[] allocate(String vehicleNo) {
        for (int f = 0; f < slots.length; f++) {
            if (freeCount[f] == 0) continue;              // whole floor skipped in O(1)
            int s = freeStack[f][--freeCount[f]];         // pop
            slots[f][s] = vehicleNo;
            return new int[]{f, s};
        }
        return null;
    }

    /** O(1): validate, clear, push. */
    public boolean release(int floor, int slot) {
        if (floor < 0 || floor >= slots.length) return false;
        if (slot < 0 || slot >= slots[floor].length) return false;
        if (slots[floor][slot] == null) return false;
        slots[floor][slot] = null;
        freeStack[floor][freeCount[floor]++] = slot;      // push
        return true;
    }

    public static void main(String[] args) {
        // Same scenario as ParkingFacility.main - output must be identical
        FreeStackFacility p = new FreeStackFacility(new int[]{2, 1});
        System.out.println("Same scenario as ParkingFacility:");
        System.out.println(Arrays.toString(p.allocate("TN01AB1111")));
        System.out.println(Arrays.toString(p.allocate("TN02CD2222")));
        System.out.println(Arrays.toString(p.allocate("TN03EF3333")));
        System.out.println(Arrays.toString(p.allocate("TN04GH4444")));
        System.out.println(p.release(0, 1) + " " + p.release(0, 1) + " " + p.release(5, 0));
        System.out.println(Arrays.toString(p.allocate("TN04GH4444")));

        // Benchmark: facility nearly full, only the LAST slot of the LAST floor is free.
        System.out.println("\nWorst-case allocate()+release() cycle, only last slot free:");
        System.out.printf("%-14s %-16s %-16s%n", "F x S", "scan (ns/op)", "stack (ns/op)");
        int[][] sizes = {{1, 20}, {3, 50}, {5, 100}, {20, 1000}};
        for (int[] sz : sizes) {
            int[] per = new int[sz[0]];
            Arrays.fill(per, sz[1]);
            ParkingFacility scan = new ParkingFacility(per);
            FreeStackFacility stack = new FreeStackFacility(per);
            int total = sz[0] * sz[1];
            for (int i = 0; i < total; i++) { scan.allocate("V" + i); stack.allocate("V" + i); }
            int lf = sz[0] - 1, ls = sz[1] - 1;
            scan.release(lf, ls);
            stack.release(lf, ls);
            int reps = 200_000;
            for (int w = 0; w < reps; w++) {                     // warm-up
                scan.allocate("X"); scan.release(lf, ls);
                stack.allocate("X"); stack.release(lf, ls);
            }
            long t0 = System.nanoTime();
            for (int r = 0; r < reps; r++) { scan.allocate("X"); scan.release(lf, ls); }
            long t1 = System.nanoTime();
            for (int r = 0; r < reps; r++) { stack.allocate("X"); stack.release(lf, ls); }
            long t2 = System.nanoTime();
            System.out.printf("%-14s %-16.1f %-16.1f%n", sz[0] + " x " + sz[1],
                    (t1 - t0) / (double) reps, (t2 - t1) / (double) reps);
        }
        System.out.println("scan  : allocate O(F*S), release O(1), extra space O(1)");
        System.out.println("stack : allocate O(F),   release O(1), extra space O(F*S) ints + O(F) counters");
    }
}
