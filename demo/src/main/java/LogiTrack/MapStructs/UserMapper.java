package LogiTrack.MapStructs;

import LogiTrack.Dto.AuthDto;
import LogiTrack.Dto.ShipmentDto;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // 1. Map User -> AuthDto
    @Mapping(source = "name", target = "username") // Fixes name mismatch
    @Mapping(source = "shipments", target = "shipments") // Uses method below automatically
    AuthDto toAuthDto(User user);

    // 2. Map Shipment -> ShipmentDto (Helper)
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "driver.driverName", target = "driverName")
    ShipmentDto toShipmentDto(Shipment shipment);
}