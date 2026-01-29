package LogiTrack.Entity;

import LogiTrack.Enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.sql.Update;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
<<<<<<< HEAD
=======
import java.util.Optional;
>>>>>>> c8ec02f (initial commit for LogiTrack)

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
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
    @Column(nullable = false )
    private int weight;
    @ManyToOne
    @JoinColumn(name = "users_id")
    private User user;
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL)
    private List<TrackingUpdate> trackingUpdates = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;
<<<<<<< HEAD
=======
    // Inside Shipment.java Entity
    @Column(name = "delivery_attempts")
    private int deliveryAttempts = 0;


    // Getter and Setter
    public int getDeliveryAttempts() { return deliveryAttempts; }
    public void setDeliveryAttempts(int deliveryAttempts) { this.deliveryAttempts = deliveryAttempts; }
>>>>>>> c8ec02f (initial commit for LogiTrack)
}
