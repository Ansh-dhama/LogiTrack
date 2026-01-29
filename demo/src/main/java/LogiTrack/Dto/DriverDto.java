package LogiTrack.Dto;

import LogiTrack.Enums.Role;
<<<<<<< HEAD
=======
import LogiTrack.Enums.Status;
>>>>>>> c8ec02f (initial commit for LogiTrack)
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DriverDto {
    private Long id;
    private String driverName;
    private String email;
    private String password;
<<<<<<< HEAD
=======
    private boolean isAvailable;
>>>>>>> c8ec02f (initial commit for LogiTrack)

    private Role role;
    // In LogiTrack/Dto/DriverDto.java
    private List<ShipmentDto> shipmentDtoList = new ArrayList<>(); // Initialize this!
}
