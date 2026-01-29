package LogiTrack.MapStructs;

import LogiTrack.Dto.DriverDto;
import LogiTrack.Dto.ShipmentDto;
import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DriverMapper {

    // 1. Main Mapping: Driver -> DriverDto
    @Mapping(target = "password", ignore = true) // Security: Never copy password to DTO
    @Mapping(source = "shipments", target = "shipmentDtoList") // Fixes name mismatch (shipments -> shipmentDtoList)
    DriverDto toDto(Driver driver);

    // 2. Helper Mapping: Shipment -> ShipmentDto
    // MapStruct will use this automatically for every item in the list above
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "driverId", ignore = true) // Optimization: We already know the driver
    @Mapping(target = "driverName", ignore = true)
    ShipmentDto mapShipment(Shipment shipment);
}