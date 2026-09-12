package LogiTrack.Controller;

import LogiTrack.Dto.ApiResponse;
import LogiTrack.Dto.TrackingUpdateDto;
import LogiTrack.Services.TrackingUpdatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TrackingShipmentController {

    private final TrackingUpdatesService trackingUpdatesService;

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse<TrackingUpdateDto>> trackShipment(@PathVariable String trackingNumber) {
        TrackingUpdateDto tracking = trackingUpdatesService.findByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Your shipment details", tracking));
    }
}
