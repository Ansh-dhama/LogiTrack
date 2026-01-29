package LogiTrack.Services;

import LogiTrack.Dto.ShipmentDto;
import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.TrackingUpdate;
import LogiTrack.Entity.User;
import LogiTrack.Enums.Status;
import LogiTrack.Exceptions.UserNotFoundException;
import LogiTrack.Repository.DriverRepository;
import LogiTrack.Repository.ShipmentRepository;
import LogiTrack.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssigningService {

    private final ShipmentRepository shipmentRepository;
    private final DriverRepository driverRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    /**
     * Automatically assigns all PENDING/Unassigned shipments to the best available drivers.
     * Runs every minute.
     */
    @Scheduled(cron = "0 * * * * *") // Runs at the top of every minute
    @Transactional
    public void autoAssignShipments() {
        log.info("Starting scheduled auto-assignment task...");

        // 1. Fetch only unassigned shipments directly from DB (Performance Optimization)
        List<Shipment> unassignedShipments = shipmentRepository.findByDriverIsNullAndStatus(Status.PENDING);
        List<Driver> drivers = driverRepository.findAll();

        if (unassignedShipments.isEmpty()) {
            log.info("No unassigned shipments found.");
            return;
        }
        if (drivers.isEmpty()) {
            log.warn("No drivers available for assignment.");
            return;
        }

        int assignedCount = 0;

        for (Shipment shipment : unassignedShipments) {
            // Find best driver
            Driver bestDriver = findBestDriver(drivers, shipment.getUser().getId());

            if (bestDriver != null) {
                assignDriverToShipment(shipment, bestDriver);

                // IMPORTANT: Update the driver's list in memory so the NEXT iteration knows
                // this driver is now busier.
                if (bestDriver.getShipments() == null) {
                    bestDriver.setShipments(new ArrayList<>());
                }
                bestDriver.getShipments().add(shipment);

                assignedCount++;
            } else {
                log.warn("Skipping shipment {}: No suitable driver found (sender cannot be driver).", shipment.getTrackingNumber());
            }
        }

        log.info("Auto-assignment complete. Assigned {} shipments.", assignedCount);
    }

    // --- Core Logic ---

    private Driver findBestDriver(List<Driver> drivers, Long senderId) {
        User u1 = userRepository.findById(senderId)
                .orElseThrow(()-> new UserNotFoundException("User with id " + senderId + " not found."));
        String senderEmail = u1.getEmail();
        return drivers.stream()
<<<<<<< HEAD
                .filter(d->!d.getEmail().equals(senderEmail))
=======
                .filter(d->! d.getEmail().equals(senderEmail))
>>>>>>> c8ec02f (initial commit for LogiTrack)
                .filter(d->d.isAvailable())
                // Rule 2: Load Balancing - Pick driver with fewest active shipments
                .min(Comparator.comparingInt(this::getActiveShipmentCount))
                .orElse(null);
    }

    // Helper to count only active shipments (ignoring delivered/cancelled ones) for load balancing
    private int getActiveShipmentCount(Driver driver) {
        if (driverRepository.countActiveShipmentsPerDriver()== null) return 0;
        return (int) driverRepository.countActiveShipmentsPerDriver().stream()
                .count();
    }

    private void assignDriverToShipment(Shipment shipment, Driver driver) {
        // 1. Update Shipment Status
        shipment.setDriver(driver);
<<<<<<< HEAD
        shipment.setStatus(Status.IN_TRANSIT);
=======
        shipment.setStatus(Status.ASSIGNED);
>>>>>>> c8ec02f (initial commit for LogiTrack)

        // 2. Create Tracking Update Entry
        TrackingUpdate update = new TrackingUpdate();
        update.setShipment(shipment);
<<<<<<< HEAD
        update.setStatus(Status.IN_TRANSIT);
=======
        update.setStatus(Status.ASSIGNED);
>>>>>>> c8ec02f (initial commit for LogiTrack)
        update.setCreationTime(LocalDateTime.now());
        update.setTrackingNumber(shipment.getTrackingNumber()); // Or generate unique ID

        if (shipment.getTrackingUpdates() == null) {
            shipment.setTrackingUpdates(new ArrayList<>());
        }
        shipment.getTrackingUpdates().add(update);

        // 3. Save
        shipmentRepository.save(shipment);

        // 4. Notifications (Wrapped in try-catch so email failure doesn't rollback DB transaction)
        notifyParties(shipment, driver);
    }

    private void notifyParties(Shipment shipment, Driver driver) {
        try {
            // Notify Sender
            emailService.sendEmail(
                    shipment.getUser().getEmail(),
                    "Shipment Assigned",
                    "Good news! Your shipment " + shipment.getTrackingNumber() + " is now IN TRANSIT with driver: " + driver.getDriverName()
            );

            // Notify Driver
            emailService.sendEmail(
                    driver.getEmail(),
                    "New Shipment Assigned",
                    "Hello " + driver.getDriverName() + ", you have a new shipment.\n" +
                            "From: " + shipment.getSenderAddress() + "\n" +
                            "To: " + shipment.getReceiverAddress()
            );
        } catch (Exception e) {
            log.error("Email notification failed for shipment {}", shipment.getTrackingNumber(), e);
        }
    }
}