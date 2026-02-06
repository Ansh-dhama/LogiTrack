package LogiTrack.Services;

import LogiTrack.Dto.TrackingEventDto;
import LogiTrack.Dto.TrackingTimelineDto;
import LogiTrack.Entity.TrackingEvent;
import LogiTrack.Repository.TrackingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrackingUpdatesService {

    private final TrackingEventRepository trackingEventRepository;

    public TrackingUpdatesService(TrackingEventRepository trackingEventRepository) {
        this.trackingEventRepository = trackingEventRepository;
    }

    @Transactional(readOnly = true)
    public TrackingTimelineDto getTimeline(String trackingNumber) {

        // ✅ Existence check (at least one event)
        boolean exists = trackingEventRepository.existsByTrackingNumber(trackingNumber);
        if (!exists) {
            throw new RuntimeException("No tracking events found for trackingNumber: " + trackingNumber);
        }

        List<TrackingEvent> last20 = trackingEventRepository
                .findTop20ByTrackingNumberOrderByAtTimeDesc(trackingNumber);

        var lastStatusEventOpt = trackingEventRepository
                .findTop1ByTrackingNumberAndStatusIsNotNullOrderByAtTimeDesc(trackingNumber);

        TrackingTimelineDto dto = new TrackingTimelineDto();
        dto.setTrackingNumber(trackingNumber);

        lastStatusEventOpt.ifPresent(last -> {
            dto.setLastStatus(last.getStatus());
            dto.setLastUpdateTime(last.getAtTime());
        });

        // timeline: currently DESC (latest first). If you want oldest->latest, reverse later.
        List<TrackingEventDto> timeline = last20.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        dto.setTimeline(timeline);
        return dto;
    }

    private TrackingEventDto toDto(TrackingEvent e) {
        TrackingEventDto dto = new TrackingEventDto();
        dto.setStatus(e.getStatus());
        dto.setAtTime(e.getAtTime());
        dto.setByRole(e.getByRole());
        dto.setByUserId(e.getByUserId());
        dto.setLat(e.getLat());
        dto.setLng(e.getLng());
        dto.setRemark(e.getRemark());
        dto.setReason(e.getReason());
        return dto;
    }

    // ✅ If you still want a "quick" endpoint for latest status only
    @Transactional(readOnly = true)
    public TrackingEventDto getLatestStatus(String trackingNumber) {
        var lastStatusEventOpt = trackingEventRepository
                .findTop1ByTrackingNumberAndStatusIsNotNullOrderByAtTimeDesc(trackingNumber);

        TrackingEvent last = lastStatusEventOpt
                .orElseThrow(() -> new RuntimeException("No status events found for trackingNumber: " + trackingNumber));

        return toDto(last);
    }

    public TrackingEventDto findByTrackingNumber(String trackingNumber) {
        return trackingEventRepository.findBytrackingNumber(trackingNumber);
    }
}
