    package LogiTrack.Controller;
    
    import LogiTrack.Dto.ApiResponse;
    import LogiTrack.Dto.ShipmentDto;
    import LogiTrack.Dto.StatusDto;
    import LogiTrack.Entity.Shipment;
    import LogiTrack.MapStructs.ShipmentMapper;
<<<<<<< HEAD
=======
    import LogiTrack.Services.CustomUserDetails;
>>>>>>> c8ec02f (initial commit for LogiTrack)
    import LogiTrack.Services.ShipmentService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
=======
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
>>>>>>> c8ec02f (initial commit for LogiTrack)
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
            ShipmentDto responseDto = shipmentMapper.toDto(createdShipment); // Assuming you have this mapper
            // Return Unifor Response
            return new ResponseEntity<>(
                    ApiResponse.success("Shipment created successfully", responseDto),
                    HttpStatus.CREATED
            );
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<ShipmentDto>>> getYourShipment() {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Shipment> shipments = shipmentService.getShipmentsByUser(username);

            // Convert to DTOs
            List<ShipmentDto> dtos = shipments.stream()
                    .map(shipment -> shipmentMapper.toDto(shipment))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    ApiResponse.success("Shipments fetched successfully", dtos)
            );
        }

<<<<<<< HEAD
        @PutMapping("/status")
        public ResponseEntity<ApiResponse<String>> updateShipmentStatus(@RequestBody StatusDto statusDto) {
            shipmentService.updateStatus(statusDto);
            // Even simple messages get wrapped
=======
        @PatchMapping("/status") // Use Patch for updates
        public ResponseEntity<ApiResponse<String>> updateShipmentStatus(
                @RequestBody StatusDto statusDto,
                @AuthenticationPrincipal CustomUserDetails userDetails) { // <--- Inject here

            // 1. Log for debugging
            if (userDetails == null) {
                throw new RuntimeException("User is not authenticated");
            }

            // 2. Pass the TRUSTED id from token, NOT the untrusted id from DTO
            shipmentService.updateStatus(
                    statusDto.getTrackingNumber(),
                    statusDto.getStatus(),
                    userDetails.getId(),   // <--- Safe ID
                    userDetails.getRole()  // <--- Safe Role
            );

>>>>>>> c8ec02f (initial commit for LogiTrack)
            return ResponseEntity.ok(
                    ApiResponse.success("Shipment status updated successfully.", null)
            );
        }
    }