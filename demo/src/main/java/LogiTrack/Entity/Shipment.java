package LogiTrack.Entity;

import LogiTrack.Enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(unique = true)
    private String trackingNumber;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdTimeDate;

    @UpdateTimestamp
    private LocalDateTime lastModifiedDate;

    @Column(nullable = false)
    private String senderAddress;

    @Column(nullable = false)
    private String receiverAddress;

    @Column(nullable = false)
    private int weight;

    @Column(name = "delivery_attempts")
    private int deliveryAttempts = 0;

    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;
    private Double currentLatitude;
    private Double currentLongitude;

    // Destination Location (Fixed when shipment is created)
    private Double destinationLatitude;
    private Double destinationLongitude;

    private String estimatedTimeArrival;
}