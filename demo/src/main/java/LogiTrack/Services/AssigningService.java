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

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoAssignShipments() {
        log.info("Starting scheduled auto-assignment task...");

        List<Shipment> unassignedShipments = shipmentRepository.findByDriverIsNullAndStatus(Status.PENDING);
        List<Driver> drivers = driverRepository.findAll();

        if (unassignedShipments.isEmpty() || drivers.isEmpty()) return;

        int assignedCount = 0;
        for (Shipment shipment : unassignedShipments) {
            Driver bestDriver = findBestDriver(drivers, shipment.getUser().getId());

            if (bestDriver != null) {
                assignDriverToShipment(shipment, bestDriver);
                // Update in-memory for current loop load balancing
                if (bestDriver.getShipments() == null) bestDriver.setShipments(new ArrayList<>());
                bestDriver.getShipments().add(shipment);
                assignedCount++;
            }
        }
        log.info("Auto-assignment complete. Assigned {} shipments.", assignedCount);
    }

    private Driver findBestDriver(List<Driver> drivers, Long senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        String senderEmail = sender.getEmail();

        return drivers.stream()
                .filter(d -> !d.getEmail().equals(senderEmail)) // Security: Sender can't be their own driver
                .filter(Driver::isAvailable)
                .min(Comparator.comparingInt(this::getActiveShipmentCount))
                .orElse(null);
    }

    private int getActiveShipmentCount(Driver driver) {
        // Fallback to simple list size if repository count isn't implemented
        return driver.getShipments() != null ? (int) driver.getShipments().stream()
                .filter(s -> s.getStatus() != Status.DELIVERED && s.getStatus() != Status.CANCELLED)
                .count() : 0;
    }

    private void assignDriverToShipment(Shipment shipment, Driver driver) {
        shipment.setDriver(driver);
        shipment.setStatus(Status.ASSIGNED);

        TrackingUpdate update = new TrackingUpdate();
        update.setShipment(shipment);
        update.setStatus(Status.ASSIGNED);
        update.setCreationTime(LocalDateTime.now());
        update.setTrackingNumber(shipment.getTrackingNumber());

        if (shipment.getTrackingUpdates() == null) shipment.setTrackingUpdates(new ArrayList<>());
        shipment.getTrackingUpdates().add(update);

        shipmentRepository.save(shipment);
        notifyParties(shipment, driver);
    }

    private void notifyParties(Shipment shipment, Driver driver) {
        try {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Assigned",
                    "Your shipment " + shipment.getTrackingNumber() + " is assigned to: " + driver.getDriverName());
            emailService.sendEmail(driver.getEmail(), "New Task", "You have a new shipment to pick up.");
        } catch (Exception e) {
            log.error("Notification failed", e);
        }
    }

}