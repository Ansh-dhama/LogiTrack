package LogiTrack.Controller;

import LogiTrack.Dto.*;
import LogiTrack.Entity.Shipment;
import LogiTrack.Enums.Status;
import LogiTrack.MapStructs.ShipmentMapper;
import LogiTrack.Services.CustomUserDetails;
import LogiTrack.Services.ShipmentService;
import LogiTrack.Services.TrackingUpdatesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/shipment")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentMapper shipmentMapper;
    private final TrackingUpdatesService trackingUpdatesService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ShipmentDto>> createShipment(@Valid @RequestBody ShipmentDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Shipment createdShipment = shipmentService.createShipment(dto, email);
        return new ResponseEntity<>(
                ApiResponse.success("Shipment created successfully", shipmentMapper.toDto(createdShipment)),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShipmentDto>>> getYourShipment() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ShipmentDto> dtos = shipmentService.getShipmentsByUser(email)
                .stream()
                .map(shipmentMapper::toDto)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Shipments fetched successfully", dtos));
    }

    @GetMapping("/{trackingNumber}/updates")
    public ResponseEntity<ApiResponse<Map<LocalDateTime, Status>>> getUpdates(
            @PathVariable("trackingNumber") String trackingNumber
    ) {
        TrackingUpdateDto tracking = trackingUpdatesService.findByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Your updates are", tracking.getUpdates()));
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<String>> updateShipmentStatus(
            @RequestBody StatusDto statusDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return new ResponseEntity<>(ApiResponse.error("User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        shipmentService.updateStatus(
                statusDto.getTrackingNumber(),
                statusDto.getStatus(),
                userDetails.getId(),
                userDetails.getRole()
        );
        return ResponseEntity.ok(ApiResponse.success("Shipment status updated successfully.", null));
    }

    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @PostMapping("/{id}/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody DriverLocationUpdateDto locationDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return new ResponseEntity<>(ApiResponse.error("User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        shipmentService.updateDriverLocation(
                id,
                locationDto,
                userDetails.getId(),
                userDetails.getRole()
        );
        return ResponseEntity.ok(ApiResponse.success("Location and ETA updated", null));
    }
}
