package LogiTrack.Repository;

import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.TrackingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TrackingRepository extends JpaRepository<TrackingUpdate,String> {
    TrackingUpdate findBytrackingNumber(String trackingNumber);

}
