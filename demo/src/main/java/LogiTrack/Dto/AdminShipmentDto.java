package LogiTrack.Dto;

import LogiTrack.Enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminShipmentDto {
    private Long id;
    private String trackingNumber;
    private Status status;
    private String senderAddress;
    private String receiverAddress;
    private int weight;
    private int deliveryAttempts;
    private LocalDateTime createdTimeDate;
    private LocalDateTime lastModifiedDate;

    private Long userId;
    private String username;
    private String customerEmail;

    private Long driverId;
    private String driverName;
    private String driverEmail;

    private Double currentLatitude;
    private Double currentLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    private String estimatedTimeArrival;
}
