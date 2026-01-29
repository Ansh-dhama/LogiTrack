package LogiTrack.Repository;

import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.TrackingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface TrackingRepository extends JpaRepository<TrackingUpdate,Long> {
    TrackingUpdate findBytrackingNumber(String trackingNumber);
}
