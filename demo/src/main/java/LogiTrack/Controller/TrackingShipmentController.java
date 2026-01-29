package LogiTrack.Controller;

import LogiTrack.Dto.ApiResponse;
import LogiTrack.Dto.TrackingUpdateDto;
import LogiTrack.Services.TrackingUpdatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController; // Import this

@Slf4j
@RestController
public class TrackingShipmentController {
    private final TrackingUpdatesService trackingUpdatesService;
    public TrackingShipmentController(TrackingUpdatesService trackingUpdatesService) {
        this.trackingUpdatesService = trackingUpdatesService;
    }
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse> trackShipment(@PathVariable String trackingNumber) {
        TrackingUpdateDto t1 = trackingUpdatesService.findByTrackingNumber(trackingNumber);
        if(t1 == null) {
            log.error("Shipment not found for trackingNumber {}", trackingNumber);
            return new ResponseEntity<>(ApiResponse.error("Shipment not found with this trackingNumber",trackingNumber),HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ApiResponse.success("Your shipment details",t1)
                , HttpStatus.OK);
    }
}