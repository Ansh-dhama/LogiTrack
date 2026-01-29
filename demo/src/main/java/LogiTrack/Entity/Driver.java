package LogiTrack.Entity;

import LogiTrack.Enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String driverName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    @JsonIgnore
<<<<<<< HEAD
    private List<Shipment> shipments;
=======
        private List<Shipment> shipments;
>>>>>>> c8ec02f (initial commit for LogiTrack)

    @Column(nullable = false)
    private Role role;

    private boolean isAvailable = false;

    public void setAvailable(boolean newStatus) {
        isAvailable = newStatus;
    }
<<<<<<< HEAD
=======

>>>>>>> c8ec02f (initial commit for LogiTrack)
}
