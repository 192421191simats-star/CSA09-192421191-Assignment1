package parking.service;

import parking.model.ParkingTicket;

/** public service class; its helper is private. */
public class BillingService {
    public double fee(ParkingTicket t) {
        return billableHours(t.getDurationMinutes()) * (double) t.getVehicle().hourlyRate();
    }
    private int billableHours(int minutes) { return (minutes + 59) / 60; }
}
