package LogiTrack.Services;

import LogiTrack.Dto.AdminDashboardDto;
import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Enums.Status;
import LogiTrack.Repository.DriverRepository;
import LogiTrack.Repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ShipmentRepository shipmentRepository;
    private final DriverRepository driverRepository;

    // 1. Get All Shipments
    public List<Shipment> getAllShipments() {
        return shipmentRepository.findAll();
    }

    // 2. Manually Assign Driver
    public void assignDriverToShipment(Long shipmentId, Long driverId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (!driver.getAvailable()) {
            throw new RuntimeException("Driver is currently unavailable/inactive");
        }

        shipment.setDriver(driver);
        shipment.setStatus(Status.ASSIGNED);
        shipmentRepository.save(shipment);
    }

    // 3. Toggle Driver Availability (Active/Inactive)
    public String toggleDriverAvailability(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Flip the status (True -> False, or False -> True)
        boolean newStatus = !driver.getAvailable();
        driver.setAvailable(newStatus);
        driverRepository.save(driver);

        return newStatus ? "Driver is now Active" : "Driver is now Inactive";
    }

    // 4. Analytics Dashboard
    public AdminDashboardDto getDashboardStats() {
        AdminDashboardDto stats = new AdminDashboardDto();

        stats.setTotalShipments(shipmentRepository.count());
        stats.setPendingShipments(shipmentRepository.countByStatus(Status.PENDING));
        stats.setDeliveredShipments(shipmentRepository.countByStatus(Status.DELIVERED));
        stats.setShipmentsToday(shipmentRepository.countShipmentsToday());
        stats.setTotalDrivers(driverRepository.count());

        return stats;
    }
}