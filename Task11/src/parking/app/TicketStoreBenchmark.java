package parking.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import parking.model.Car;
import parking.model.ParkingSlot;
import parking.model.ParkingTicket;
import parking.model.Visitor;

/**
 * Task 11 alternative concept: time to find a ticket on exit when active tickets
 * are stored in an array, an ArrayList, or a HashMap keyed by registration number.
 *
 * Run: java -cp out parking.app.TicketStoreBenchmark
 */
public class TicketStoreBenchmark {
    public static void main(String[] args) {
        int[] sizes = {100, 500, 1_000, 10_000, 100_000};
        LocalDateTime t = LocalDateTime.of(2026, 11, 16, 8, 0);
        System.out.printf("%-8s %-16s %-16s %-16s%n", "n", "array (ns/find)", "ArrayList (ns)", "HashMap (ns)");
        for (int n : sizes) {
            ParkingTicket[] arr = new ParkingTicket[n];
            List<ParkingTicket> list = new ArrayList<>(n);
            Map<String, ParkingTicket> map = new HashMap<>(n * 2);
            String[] keys = new String[n];
            for (int i = 0; i < n; i++) {
                keys[i] = String.format("TN%02dAB%04d", i / 10_000, i % 10_000);
                ParkingTicket tk = new ParkingTicket(new Car(keys[i]), new Visitor("v"), new ParkingSlot(0, i % 100), t);
                arr[i] = tk; list.add(tk); map.put(keys[i], tk);
            }
            int lookups = Math.max(2_000, 2_000_000 / n);
            java.util.Random rnd = new java.util.Random(42);
            int[] idx = new int[lookups];
            for (int i = 0; i < lookups; i++) idx[i] = rnd.nextInt(n);
            long sink = 0;
            for (int w = 0; w < 3; w++) { sink += runArr(arr, keys, idx); sink += runList(list, keys, idx); sink += runMap(map, keys, idx); }
            long a0 = System.nanoTime(); sink += runArr(arr, keys, idx);
            long a1 = System.nanoTime(); sink += runList(list, keys, idx);
            long a2 = System.nanoTime(); sink += runMap(map, keys, idx);
            long a3 = System.nanoTime();
            System.out.printf("%-8d %-16.1f %-16.1f %-16.1f%n", n,
                    (a1 - a0) / (double) lookups, (a2 - a1) / (double) lookups, (a3 - a2) / (double) lookups);
            if (sink == 42) System.out.println();
        }
        System.out.println("array / ArrayList : O(n) linear search on regNo; HashMap : O(1) average");
        System.out.println("space: array n refs (fixed capacity); ArrayList n refs + up to 50% slack;");
        System.out.println("       HashMap n entries (~32-48 bytes each) + bucket table -> roughly 3-4x the array");
    }
    static long runArr(ParkingTicket[] a, String[] k, int[] idx) {
        long s = 0;
        for (int i : idx) { String key = k[i];
            for (ParkingTicket t : a) if (t.getVehicle().getRegNo().equals(key)) { s += t.getTicketNo(); break; } }
        return s;
    }
    static long runList(List<ParkingTicket> l, String[] k, int[] idx) {
        long s = 0;
        for (int i : idx) { String key = k[i];
            for (ParkingTicket t : l) if (t.getVehicle().getRegNo().equals(key)) { s += t.getTicketNo(); break; } }
        return s;
    }
    static long runMap(Map<String, ParkingTicket> m, String[] k, int[] idx) {
        long s = 0;
        for (int i : idx) s += m.get(k[i]).getTicketNo();
        return s;
    }
}
