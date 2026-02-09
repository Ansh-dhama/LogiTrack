package LogiTrack.Dto;

import LogiTrack.Enums.Role;
import LogiTrack.Enums.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrackingEventDto {

    private Status status;          // can be null for pure LOCATION_UPDATE
    private LocalDateTime atTime;

    private Role byRole;
    private Long byUserId;

    private Double lat;
    private Double lng;

    private String remark;          // e.g. "STATUS_CHANGED", "LOCATION_UPDATE"
    private String reason;          // optional
}
