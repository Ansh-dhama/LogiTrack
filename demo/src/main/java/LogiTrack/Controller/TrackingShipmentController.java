package LogiTrack.Controller;

import LogiTrack.Dto.ApiResponse;
import LogiTrack.Dto.TrackingEventDto;
import LogiTrack.Dto.TrackingTimelineDto;
import LogiTrack.Services.TrackingUpdatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("track")
public class TrackingShipmentController {

    private final TrackingUpdatesService trackingUpdatesService;

    public TrackingShipmentController(TrackingUpdatesService trackingUpdatesService) {
        this.trackingUpdatesService = trackingUpdatesService;
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<ApiResponse> trackShipment(@PathVariable String trackingNumber) {
        try {
            TrackingTimelineDto dto = trackingUpdatesService.getTimeline(trackingNumber);
            return new ResponseEntity<>(ApiResponse.success("Your shipment timeline", dto), HttpStatus.OK);
        } catch (RuntimeException ex) {
            return new ResponseEntity<>(ApiResponse.error(ex.getMessage(), trackingNumber), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/lastUpdate/{trackingNumber}")
    public ResponseEntity<ApiResponse> currentUpdate(@PathVariable String trackingNumber){
        TrackingEventDto dto = trackingUpdatesService.getLatestStatus(trackingNumber);
        return  new ResponseEntity<>(ApiResponse.success("Current status is : ", dto), HttpStatus.OK);
    }

}
