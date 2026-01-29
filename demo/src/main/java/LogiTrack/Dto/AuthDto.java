package LogiTrack.Dto;

import LogiTrack.Enums.Role;
import jakarta.validation.constraints.Email; // Import for email validation
import jakarta.validation.constraints.NotBlank; // Import for checking empty strings
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AuthDto {
    private Long id;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotNull(message = "Role is required")
    private Role role;


    private List<ShipmentDto> shipments;
}