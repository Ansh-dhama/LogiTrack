package LogiTrack.Repository;

import LogiTrack.Entity.Shipment;
import LogiTrack.Enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment,Long> {
    Optional<Shipment> findByTrackingNumber(String t);

    List<Shipment> findByDriverIsNullAndStatus(Status status);

    long countByStatus(Status status);

    // ✅ FIX 2: You MUST have this @Query line above the method
    @Query("SELECT COUNT(s) FROM Shipment s WHERE DATE(s.createdTimeDate) = CURRENT_DATE")
    long countShipmentsToday();
}
