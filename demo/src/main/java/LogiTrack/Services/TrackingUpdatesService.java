package LogiTrack.Services;

import LogiTrack.Dto.TrackingUpdateDto;
<<<<<<< HEAD
import LogiTrack.Entity.TrackingUpdate;
import LogiTrack.Repository.TrackingRepository;
import org.springframework.stereotype.Service;

@Service
public class TrackingUpdatesService {
    private TrackingRepository  trackingRepository;
    public TrackingUpdatesService(TrackingRepository trackingRepository) {
=======
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.TrackingUpdate;
import LogiTrack.Enums.Role;
import LogiTrack.Enums.Status;
import LogiTrack.Exceptions.ShipmentNotFoundException;
import LogiTrack.MapStructs.ShipmentMapper;
import LogiTrack.Repository.ShipmentRepository;
import LogiTrack.Repository.TrackingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrackingUpdatesService {
    private TrackingRepository  trackingRepository;


    public TrackingUpdatesService(TrackingRepository trackingRepository, StatusTransitionValidator statusTransitionValidator, ShipmentRepository shipmentRepository, ShipmentMapper shipmentMapper) {
>>>>>>> c8ec02f (initial commit for LogiTrack)
        this.trackingRepository = trackingRepository;
    }
    public TrackingUpdateDto findByTrackingNumber(String trackingNumber) {
        TrackingUpdate trackingUpdate = trackingRepository.findBytrackingNumber(trackingNumber);
<<<<<<< HEAD
=======
        if (trackingUpdate == null) {
            return null;
        }
>>>>>>> c8ec02f (initial commit for LogiTrack)
        TrackingUpdateDto trackingUpdateDto = new TrackingUpdateDto();
        trackingUpdateDto.setTrackingNumber(trackingUpdate.getTrackingNumber());
        trackingUpdateDto.setLastUpdate(trackingUpdate.getLastUpdate());
        trackingUpdateDto.setStatus(trackingUpdate.getStatus());
        trackingUpdateDto.setShipment(trackingUpdate.getShipment());
<<<<<<< HEAD
        return trackingUpdateDto;
     }
=======
        
        return trackingUpdateDto;
     }
    // Inside TrackingUpdatesService.java

>>>>>>> c8ec02f (initial commit for LogiTrack)
}
