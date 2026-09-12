package LogiTrack.Services;

import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.TrackingUpdate;
import LogiTrack.Enums.Status;
import LogiTrack.Repository.DriverRepository;
import LogiTrack.Repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssigningService {

    private final ShipmentRepository shipmentRepository;
    private final DriverRepository driverRepository;
    private final EmailService emailService;
    // ❌ REMOVED: private final UserRepository userRepository; (Not needed)

    @Scheduled(cron = "0 * * * * *") // Runs every minute
    @Transactional
    public void autoAssignShipments() {
        log.info("Starting scheduled auto-assignment task...");

        // 1. Fetch pending shipments
        List<Shipment> unassignedShipments = shipmentRepository.findByDriverIsNullAndStatus(Status.PENDING);

        // 2. Fetch all drivers (If you have thousands of drivers, filter this by 'Active' status in the query)
        List<Driver> drivers = driverRepository.findAll();

        if (unassignedShipments.isEmpty() || drivers.isEmpty()) return;

        int assignedCount = 0;

        for (Shipment shipment : unassignedShipments) {
            // ✅ OPTIMIZATION: Pass the user directly, don't query DB again
            Driver bestDriver = findBestDriver(drivers, shipment.getUser().getEmail());

            if (bestDriver != null) {
                assignDriverToShipment(shipment, bestDriver);

                // Update in-memory list for load balancing within this current loop
                if (bestDriver.getShipments() == null) {
                    bestDriver.setShipments(new ArrayList<>());
                }
                bestDriver.getShipments().add(shipment);

                assignedCount++;
            }
        }
        log.info("Auto-assignment complete. Assigned {} shipments.", assignedCount);
    }

    private Driver findBestDriver(List<Driver> drivers, String senderEmail) {
        return drivers.stream()
                .filter(d -> !d.getEmail().equals(senderEmail)) // Security check
                .filter(Driver::isAvailable)
                .min(Comparator.comparingInt(this::getActiveShipmentCount))
                .orElse(null);
    }

    private int getActiveShipmentCount(Driver driver) {
        if (driver.getShipments() == null) return 0;

        return (int) driver.getShipments().stream()
                .filter(s -> s.getStatus() != Status.DELIVERED
                        && s.getStatus() != Status.CANCELLED
                        && s.getStatus() != Status.RETURNED) // Also exclude returned
                .count();
    }

    private void assignDriverToShipment(Shipment shipment, Driver driver) {
        shipment.setDriver(driver);
        shipment.setStatus(Status.ASSIGNED);

        TrackingUpdate update = new TrackingUpdate();
        update.setShipment(shipment);
        update.setStatus(Status.ASSIGNED);
        update.setCreationTime(LocalDateTime.now());
        update.setTrackingNumber(shipment.getTrackingNumber());
        update.getUpdates().put(LocalDateTime.now(),Status.ASSIGNED);
        if (shipment.getTrackingUpdates() == null) {
            shipment.setTrackingUpdates(new ArrayList<>());
        }
        shipment.getTrackingUpdates().add(update);

        shipmentRepository.save(shipment);
        notifyParties(shipment, driver);
    }

    private void notifyParties(Shipment shipment, Driver driver) {
        try {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Assigned",
                    "Your shipment " + shipment.getTrackingNumber() + " is assigned to: " + driver.getDriverName());

            emailService.sendEmail(driver.getEmail(), "New Task",
                    "You have a new shipment to pick up. Tracking: " + shipment.getTrackingNumber());
        } catch (Exception e) {
            log.error("Notification failed for shipment {}", shipment.getTrackingNumber(), e);
        }
    }
}
