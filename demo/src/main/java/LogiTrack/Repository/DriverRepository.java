package LogiTrack.Repository;

import LogiTrack.Entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {



    boolean existsByDriverName(String driverName);

    Driver findByEmail(String email);
    @Query("SELECT s.driver.id, COUNT(s) " +
            "FROM Shipment s " +
            "WHERE s.status NOT IN (LogiTrack.Enums.Status.PENDING ,LogiTrack.Enums.Status.IN_TRANSIT) " +
            "GROUP BY s.driver.id")
    List<Object[]> countActiveShipmentsPerDriver();
}
