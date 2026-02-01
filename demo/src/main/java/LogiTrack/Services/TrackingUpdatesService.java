package LogiTrack.Services;

import LogiTrack.Dto.TrackingUpdateDto;
import LogiTrack.Entity.TrackingUpdate;
import LogiTrack.Repository.ShipmentRepository;
import LogiTrack.Repository.TrackingRepository;
import LogiTrack.MapStructs.ShipmentMapper;
import org.springframework.stereotype.Service;

@Service
public class TrackingUpdatesService {

    private final TrackingRepository trackingRepository;


    public TrackingUpdatesService(TrackingRepository trackingRepository) {
        this.trackingRepository = trackingRepository;

    }

    public TrackingUpdateDto findByTrackingNumber(String trackingNumber) {
        TrackingUpdate trackingUpdate = trackingRepository.findBytrackingNumber(trackingNumber);

        if (trackingUpdate == null) {
            return null; // Or throw a ShipmentNotFoundException
        }

        TrackingUpdateDto trackingUpdateDto = new TrackingUpdateDto();
        trackingUpdateDto.setTrackingNumber(trackingUpdate.getTrackingNumber());
        trackingUpdateDto.setLastUpdate(trackingUpdate.getLastUpdate());
        trackingUpdateDto.setStatus(trackingUpdate.getStatus());
        trackingUpdateDto.setShipment(trackingUpdate.getShipment());

        return trackingUpdateDto;
    }
}