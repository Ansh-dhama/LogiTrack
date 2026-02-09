package LogiTrack.Dto;

import LogiTrack.Enums.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TrackingTimelineDto {
    private String trackingNumber;

    private Status lastStatus;
    private LocalDateTime lastUpdateTime;

    private Long shipmentId; // optional (if you want)
    private List<TrackingEventDto> timeline; // last 20 events
}
