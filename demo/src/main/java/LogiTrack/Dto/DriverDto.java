package LogiTrack.Dto;

import LogiTrack.Enums.Role;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DriverDto {
    private Long id;
    private String driverName;
    private String email;
    private String password;
    private boolean isAvailable; // Crucial for auto-assignment logic
    private Role role;

    // Initialized to prevent NullPointerExceptions during mapping
    private List<ShipmentDto> shipmentDtoList = new ArrayList<>();
}