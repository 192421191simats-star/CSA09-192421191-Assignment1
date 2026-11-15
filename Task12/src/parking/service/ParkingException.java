package parking.service;

/** Checked exception for rejected business operations (full, duplicate, unknown type ...). */
public class ParkingException extends Exception {
    public ParkingException(String message) { super(message); }
}
