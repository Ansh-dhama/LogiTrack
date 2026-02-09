package LogiTrack.Controller;

import LogiTrack.Dto.*;
import LogiTrack.Entity.Shipment;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/shipment")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ShipmentMapper shipmentMapper;
    private final TrackingUpdatesService trackingUpdatesService;

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

    /**
     * ✅ TrackingEvent-based timeline:
     * - lastStatus
     * - lastUpdateTime
     * - last 20 events
     */
    @GetMapping("/{trackingNumber}/timeline")
    public ResponseEntity<ApiResponse<TrackingTimelineDto>> getTimeline(@PathVariable String trackingNumber) {
        try {
            TrackingTimelineDto timelineDto = trackingUpdatesService.getTimeline(trackingNumber);
            return ResponseEntity.ok(ApiResponse.success("Shipment timeline fetched", timelineDto));
        } catch (RuntimeException ex) {
            log.error("Timeline not found for trackingNumber={}", trackingNumber, ex);
            return new ResponseEntity<>(
                    ApiResponse.error(ex.getMessage(), null),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    /**
     * ✅ Optional: Latest status only (small payload)
     */
    @GetMapping("/{trackingNumber}/latest-status")
    public ResponseEntity<ApiResponse<TrackingEventDto>> getLatestStatus(@PathVariable String trackingNumber) {
        try {
            TrackingEventDto latest = trackingUpdatesService.getLatestStatus(trackingNumber);
            return ResponseEntity.ok(ApiResponse.success("Latest status fetched", latest));
        } catch (RuntimeException ex) {
            log.error("Latest status not found for trackingNumber={}", trackingNumber, ex);
            return new ResponseEntity<>(
                    ApiResponse.error(ex.getMessage(), null),
                    HttpStatus.NOT_FOUND
            );
        }
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<String>> updateShipmentStatus(
            @RequestBody StatusDto statusDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

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

    /**
     * ✅ Secure: only DRIVER should update location
     * ✅ Service must verify "assigned driver only"
     */
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{id}/location")
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @PathVariable Long id,
            @RequestBody DriverLocationUpdateDto locationDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return new ResponseEntity<>(ApiResponse.error("User not authenticated"), HttpStatus.UNAUTHORIZED);
        }

        shipmentService.updateDriverLocation(id, locationDto);

        return ResponseEntity.ok(ApiResponse.success("Location and ETA updated", null));
    }
}
