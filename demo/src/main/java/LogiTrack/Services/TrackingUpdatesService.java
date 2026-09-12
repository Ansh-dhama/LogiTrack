package LogiTrack.Services;

import LogiTrack.Dto.TrackingUpdateDto;
import LogiTrack.Entity.TrackingUpdate;
import LogiTrack.Exceptions.ShipmentNotFoundException;
import LogiTrack.Repository.TrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackingUpdatesService {

    private final TrackingRepository trackingRepository;

    public TrackingUpdatesService(TrackingRepository trackingRepository) {
        this.trackingRepository = trackingRepository;
    }

    @Transactional(readOnly = true)
    public TrackingUpdateDto findByTrackingNumber(String trackingNumber) {
        TrackingUpdate trackingUpdate = trackingRepository.findBytrackingNumber(trackingNumber);
        if (trackingUpdate == null) {
            throw new ShipmentNotFoundException("Tracking info not found for: " + trackingNumber);
        }

        TrackingUpdateDto dto = new TrackingUpdateDto();
        dto.setTrackingNumber(trackingUpdate.getTrackingNumber());
        dto.setLastUpdate(trackingUpdate.getLastUpdate());
        dto.setCreationTime(trackingUpdate.getCreationTime());
        dto.setStatus(trackingUpdate.getStatus());
        dto.setUpdates(trackingUpdate.getUpdates());

        if (trackingUpdate.getShipment() != null) {
            dto.setShipmentId(trackingUpdate.getShipment().getId());
        }
        return dto;
    }
}
