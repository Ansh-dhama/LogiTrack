package LogiTrack.Enums;

public enum Status {
    PENDING,
    ASSIGNED,// Shipment created, waiting for driver
<<<<<<< HEAD
    IN_TRANSIT,     // Driver picked up, on the way
    DELIVERED,      // Successfully reached destination
    CANCELLED,      // Shipment cancelled
=======
    IN_TRANSIT,
    CREATED,// Driver picked up, on the way
    DELIVERED,
    DELIVERY_ATTEMPTED,
    CANCELLED,
>>>>>>> c8ec02f (initial commit for LogiTrack)
    RETURNED        // Delivery failed, returned to sender
    ;

    public Object toLowerCase(Status s) {
       return s.toLowerCase(this);
    }
}