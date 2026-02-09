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
        private List<Shipment> shipments;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Role role;


        private Boolean isAvailable = false; // default true

        // Current Driver Location (Updated frequently)
        private Double currentLatitude;
        private Double currentLongitude;

    }