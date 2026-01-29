package LogiTrack.Dto;

import LogiTrack.Entity.Shipment;
import LogiTrack.Enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackingUpdateDto {
    private String trackingNumber;
    private Status status;
    private LocalDateTime lastUpdate;
    private LocalDateTime creationTime;
    private Shipment shipment;
}
