package LogiTrack.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverStatus {
    @NotNull(message = "Driver status is required")
    private Boolean status;

    // Kept for backwards-compatible responses. The server never trusts this
    // value when changing availability; authenticated identity is used instead.
    private String email;
}
