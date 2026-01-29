package LogiTrack.Entity;

import LogiTrack.Enums.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackingUpdate {

    @Id
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreationTimestamp
    @Column(nullable = false,  updatable = false)
    private LocalDateTime creationTime;
    // This handles the "time" field from your JSON

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime lastUpdate;


    // Link back to the Shipment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    @JsonBackReference // Prevents infinite recursion loop in JSON
    private Shipment shipment;
}