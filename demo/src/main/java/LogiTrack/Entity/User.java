        package LogiTrack.Entity;

        import LogiTrack.Enums.Role;
        import com.fasterxml.jackson.annotation.JsonIgnore;
        import jakarta.persistence.*;
        import lombok.*;

        import java.time.LocalDateTime;
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
            @JsonIgnore
            @Column(nullable = false)
            private String password;

            @Enumerated(EnumType.STRING)
            @Column(nullable = false)
            private Role role;

            @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
            @JsonIgnore
            private List<Shipment> shipments;

            // Login OTP is stored as a BCrypt hash and expires quickly.
            @JsonIgnore
            @Column(length = 100)
            private String otp;

            @JsonIgnore
            private LocalDateTime otpExpirationTime;

        }
