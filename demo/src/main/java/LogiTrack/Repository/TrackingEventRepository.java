package LogiTrack.Repository;

import LogiTrack.Dto.TrackingEventDto;
import LogiTrack.Entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findTop20ByTrackingNumberOrderByAtTimeDesc(String trackingNumber);

    Optional<TrackingEvent> findTop1ByTrackingNumberAndStatusIsNotNullOrderByAtTimeDesc(String trackingNumber);

    TrackingEventDto findBytrackingNumber(String trackingNumber);

    boolean existsByTrackingNumber(String trackingNumber);
}
