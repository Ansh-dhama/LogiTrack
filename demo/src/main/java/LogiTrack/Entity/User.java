        package LogiTrack.Entity;

        import LogiTrack.Enums.Role;
        import com.fasterxml.jackson.annotation.JsonIgnore;
        import jakarta.persistence.*;
        import lombok.*;

        import java.util.List;


        @Entity
        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        @Table(name = "users")
        public class User {

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;

            @Column(nullable = false)
            private String name;
            @Column(nullable = false, unique = true)
            private String email;
            @Column(nullable = false)
            private String password;

            @Enumerated(EnumType.STRING)
            @Column(nullable = false)
            private Role role;

            @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
            @JsonIgnore
            private List<Shipment> shipments;

        }
