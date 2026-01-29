package LogiTrack.Exceptions;

public class ShipmentNotFoundException extends RuntimeException{
    public ShipmentNotFoundException(String message) {
        super(message);
    }
}
