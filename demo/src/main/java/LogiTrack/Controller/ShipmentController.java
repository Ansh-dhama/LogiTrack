package LogiTrack.Controller;

import LogiTrack.Dto.ApiResponse;
import LogiTrack.Dto.ShipmentDto;
import LogiTrack.Dto.StatusDto;
import LogiTrack.Entity.Shipment;
import LogiTrack.MapStructs.ShipmentMapper;
import LogiTrack.Services.CustomUserDetails;
import LogiTrack.Services.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/shipment")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentMapper shipmentMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<ShipmentDto>> createShipment(@Valid @RequestBody ShipmentDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Shipment createdShipment = shipmentService.createShipment(dto, username);
        ShipmentDto responseDto = shipmentMapper.toDto(createdShipment);
        return new ResponseEntity<>(
                ApiResponse.success("Shipment created successfully", responseDto),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShipmentDto>>> getYourShipment() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Shipment> shipments = shipmentService.getShipmentsByUser(username);
        List<ShipmentDto> dtos = shipments.stream()
                .map(shipmentMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Shipments fetched successfully", dtos));
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<String>> updateShipmentStatus(
            @RequestBody StatusDto statusDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return new ResponseEntity<>(ApiResponse.error("User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        // Using the trusted ID and Role from the security context
        shipmentService.updateStatus(
                statusDto.getTrackingNumber(),
                statusDto.getStatus(),
                userDetails.getId(),
                userDetails.getRole()
        );

        return ResponseEntity.ok(ApiResponse.success("Shipment status updated successfully.", null));
    }
}