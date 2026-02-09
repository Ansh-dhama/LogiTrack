package LogiTrack.Services;

import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.TrackingEvent;
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
    private final TrackingEventService trackingEventService; // ✅ use service

    @Scheduled(cron = "0 * * * * *") // every minute
    @Transactional
    public void autoAssignShipments() {
        log.info("Starting scheduled auto-assignment task...");

        // ✅ fetch only CREATED/PENDING and limit
        List<Shipment> unassignedShipments = shipmentRepository.findTop50ByDriverIsNullAndStatusInOrderByCreatedTimeDateAsc(
                List.of(Status.CREATED, Status.PENDING)
        );


        // ✅ only available drivers
        List<Driver> drivers = driverRepository.findByIsAvailableTrue();

        if (unassignedShipments.isEmpty() || drivers.isEmpty()) {
            log.info("Auto-assign skipped. UnassignedShipments={}, AvailableDrivers={}",
                    unassignedShipments.size(), drivers.size());
            return;
        }

        int assignedCount = 0;

        for (Shipment shipment : unassignedShipments) {
            Driver bestDriver = findBestDriver(drivers, shipment.getUser().getEmail());
            if (bestDriver == null) continue;

            boolean assigned = assignDriverToShipment(shipment, bestDriver);
            if (assigned) {
                assignedCount++;
            }
        }

        log.info("Auto-assignment complete. Assigned {} shipments.", assignedCount);
    }

    private Driver findBestDriver(List<Driver> drivers, String senderEmail) {
        return drivers.stream()
                .filter(d -> !d.getEmail().equalsIgnoreCase(senderEmail)) // avoid self-assign
                .filter(d -> Boolean.TRUE.equals(d.getIsAvailable()))
                .min(Comparator.comparingInt(this::getActiveShipmentCount))
                .orElse(null);
    }

    private int getActiveShipmentCount(Driver driver) {
        if (driver.getShipments() == null) return 0;
        return (int) driver.getShipments().stream()
                .filter(s -> s.getStatus() != Status.DELIVERED
                        && s.getStatus() != Status.CANCELLED
                        && s.getStatus() != Status.RETURNED)
                .count();
    }

    /**
     * ✅ returns false if shipment was already assigned by someone else (race-safe)
     */
    private boolean assignDriverToShipment(Shipment shipment, Driver driver) {

        // double check
        if (shipment.getDriver() != null) return false;

        shipment.setDriver(driver);
        shipment.setStatus(Status.ASSIGNED);
        shipmentRepository.save(shipment);

        // ✅ mark driver unavailable
        driver.setIsAvailable(false);
        driverRepository.save(driver);

        // ✅ log TrackingEvent (source of truth)
        trackingEventService.logStatus(
                shipment.getTrackingNumber(),
                Status.ASSIGNED,
                driver.getRole(),         // or Role.ADMIN if SYSTEM assigns
                driver.getId(),
                "AUTO_ASSIGNED"
        );

        notifyParties(shipment, driver);
        return true;
    }

    private void notifyParties(Shipment shipment, Driver driver) {
        try {
            emailService.sendEmail(
                    shipment.getUser().getEmail(),
                    "Shipment Assigned",
                    "Your shipment " + shipment.getTrackingNumber() + " is assigned to: " + driver.getDriverName()
            );

            emailService.sendEmail(
                    driver.getEmail(),
                    "New Task",
                    "You have a new shipment to pick up. Tracking: " + shipment.getTrackingNumber()
            );
        } catch (Exception e) {
            log.error("Notification failed for shipment {}", shipment.getTrackingNumber(), e);
        }
    }
}
