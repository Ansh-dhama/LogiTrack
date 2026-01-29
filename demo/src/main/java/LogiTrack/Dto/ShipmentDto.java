package LogiTrack.Dto;

import LogiTrack.Enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty; // Import this
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShipmentDto {
    @NotBlank(message = "Sender address is required")
    @JsonProperty("senderAddress")
    private String senderAddress;
    @NotBlank(message = "Receiver address is required")
    @JsonProperty("receiverAddress")
    private String receiverAddress;
    @Min(value = 1, message = "Weight must be at least 1kg")
    @JsonProperty("weight")
    private int weight;
    @JsonProperty("driverId")
    private Long driverId;
    private String trackingNumber;
    private Status status;
    private LocalDateTime updatedTime;
    private LocalDateTime createdDate;
    private String driverName;
    private Long userId;
}