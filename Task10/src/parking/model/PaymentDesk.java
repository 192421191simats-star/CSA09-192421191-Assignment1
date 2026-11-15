package parking.model;

/** Lives in parking.model, so it may call the package-private ParkingTicket.markPaid(). */
public class PaymentDesk {
    public void settle(ParkingTicket t) { t.markPaid(); }
}
