package LogiTrack.Entity;

import LogiTrack.Enums.Role;
import LogiTrack.Enums.Status;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Table(name = "tracking_event", indexes = {
        @Index(name="idx_track_event_tracking", columnList="trackingNumber,atTime")
})
@Data
public class TrackingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime atTime;

    @Enumerated(EnumType.STRING)
    private Role byRole;

    private Long byUserId;

    private Double lat;
    private Double lng;
    private String remark;
    private String reason;

    // ✅ add this
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;
}
