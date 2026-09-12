package LogiTrack.Services;

import LogiTrack.Dto.AdminDashboardDto;
import LogiTrack.Dto.AdminShipmentDto;
import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Enums.Status;
import LogiTrack.Exceptions.DriverNotFoundException;
import LogiTrack.Exceptions.ShipmentNotFoundException;
import LogiTrack.Repository.DriverRepository;
import LogiTrack.Repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ShipmentRepository shipmentRepository;
    private final DriverRepository driverRepository;

    @Transactional(readOnly = true)
    public List<AdminShipmentDto> getAllShipments() {
        return shipmentRepository.findAll()
                .stream()
                .map(this::toAdminDto)
                .toList();
    }

    @Transactional
    public void assignDriverToShipment(Long shipmentId, Long driverId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + shipmentId));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));

        if (!driver.isAvailable()) {
            throw new IllegalArgumentException("Driver is currently unavailable/inactive");
        }

        shipment.setDriver(driver);
        shipment.setStatus(Status.ASSIGNED);
        shipmentRepository.save(shipment);
    }

    @Transactional
    public String toggleDriverAvailability(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));

        boolean newStatus = !driver.isAvailable();
        driver.setAvailable(newStatus);
        driverRepository.save(driver);
        return newStatus ? "Driver is now Active" : "Driver is now Inactive";
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboardStats() {
        AdminDashboardDto stats = new AdminDashboardDto();
        stats.setTotalShipments(shipmentRepository.count());
        stats.setPendingShipments(shipmentRepository.countByStatus(Status.PENDING));
        stats.setDeliveredShipments(shipmentRepository.countByStatus(Status.DELIVERED));
        stats.setShipmentsToday(shipmentRepository.countShipmentsToday());
        stats.setTotalDrivers(driverRepository.count());
        return stats;
    }

    private AdminShipmentDto toAdminDto(Shipment shipment) {
        AdminShipmentDto dto = new AdminShipmentDto();
        dto.setId(shipment.getId());
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setStatus(shipment.getStatus());
        dto.setSenderAddress(shipment.getSenderAddress());
        dto.setReceiverAddress(shipment.getReceiverAddress());
        dto.setWeight(shipment.getWeight());
        dto.setDeliveryAttempts(shipment.getDeliveryAttempts());
        dto.setCreatedTimeDate(shipment.getCreatedTimeDate());
        dto.setLastModifiedDate(shipment.getLastModifiedDate());
        dto.setCurrentLatitude(shipment.getCurrentLatitude());
        dto.setCurrentLongitude(shipment.getCurrentLongitude());
        dto.setDestinationLatitude(shipment.getDestinationLatitude());
        dto.setDestinationLongitude(shipment.getDestinationLongitude());
        dto.setEstimatedTimeArrival(shipment.getEstimatedTimeArrival());

        if (shipment.getUser() != null) {
            dto.setUserId(shipment.getUser().getId());
            dto.setUsername(shipment.getUser().getName());
            dto.setCustomerEmail(shipment.getUser().getEmail());
        }

        if (shipment.getDriver() != null) {
            dto.setDriverId(shipment.getDriver().getId());
            dto.setDriverName(shipment.getDriver().getDriverName());
            dto.setDriverEmail(shipment.getDriver().getEmail());
        }
        return dto;
    }
}
