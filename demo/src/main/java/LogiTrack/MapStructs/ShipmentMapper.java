package LogiTrack.MapStructs;

import LogiTrack.Dto.ShipmentDto;
import LogiTrack.Entity.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// 'componentModel = "spring"' allows you to @Autowired this mapper!
@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    // --- Entity to DTO ---
    @Mapping(source = "user.id", target = "userId") 
    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "driver.driverName", target = "driverName")
    @Mapping(source = "createdTimeDate", target = "createdDate")
    ShipmentDto toDto(Shipment shipment);

    // --- DTO to Entity ---
    // We ignore these because we usually load them from DB manually in the Service
    @Mapping(target = "user", ignore = true) 
    @Mapping(target = "driver", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingUpdates", ignore = true)
    Shipment toEntity(ShipmentDto dto);
}