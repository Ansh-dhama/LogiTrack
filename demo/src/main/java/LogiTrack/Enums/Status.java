package LogiTrack.Enums;

public enum Status {
    PENDING,            // User just created the order
    ASSIGNED,           // Admin or System linked a driver
    IN_TRANSIT,         // Driver has picked up the item
    DELIVERY_ATTEMPTED, // Driver tried to deliver but failed
    DELIVERED,          // Successfully reached destination
    CANCELLED,          // Shipment stopped
    RETURNED,           // Delivery failed 3 times, going back to sender
    CREATED             // Initial state for some workflows
    ;

    public String toLowerCase() {
        return this.name().toLowerCase();
    }
}