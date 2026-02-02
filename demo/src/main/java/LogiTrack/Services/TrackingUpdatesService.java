package LogiTrack.Services;

import LogiTrack.Dto.TrackingUpdateDto;
import LogiTrack.Entity.TrackingUpdate;
import LogiTrack.Repository.TrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import this!

@Service
public class TrackingUpdatesService {

    private final TrackingRepository trackingRepository;

    public TrackingUpdatesService(TrackingRepository trackingRepository) {
        this.trackingRepository = trackingRepository;
    }

    // Add @Transactional to ensure the Map loads correctly from the database
    @Transactional(readOnly = true)
    public TrackingUpdateDto findByTrackingNumber(String trackingNumber) {
        TrackingUpdate trackingUpdate = trackingRepository.findBytrackingNumber(trackingNumber);

        if (trackingUpdate == null) {
            // It is better to return an empty object or throw exception than return null
            throw new RuntimeException("Tracking info not found for: " + trackingNumber);
        }

        TrackingUpdateDto trackingUpdateDto = new TrackingUpdateDto();
        trackingUpdateDto.setTrackingNumber(trackingUpdate.getTrackingNumber());
        trackingUpdateDto.setLastUpdate(trackingUpdate.getLastUpdate());
        trackingUpdateDto.setStatus(trackingUpdate.getStatus());

        if (trackingUpdate.getShipment() != null) {
            trackingUpdateDto.setShipmentId(trackingUpdate.getShipment().getId());
        }

        // 👇 THIS WAS MISSING. YOU MUST COPY THE MAP!
        trackingUpdateDto.setUpdates(trackingUpdate.getUpdates());

        return trackingUpdateDto;
    }
}