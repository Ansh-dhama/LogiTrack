package LogiTrack;

import LogiTrack.Dto.DriverLocationUpdateDto;
import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Enums.Role;
import LogiTrack.Enums.Status;
import LogiTrack.Repository.DriverRepository;
import LogiTrack.Repository.ShipmentRepository;
import LogiTrack.Repository.TrackingRepository;
import LogiTrack.Repository.UserRepository;
import LogiTrack.Services.EmailService;
import LogiTrack.Services.ShipmentService;
import LogiTrack.Services.StatusTransitionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock ShipmentRepository shipmentRepository;
    @Mock DriverRepository driverRepository;
    @Mock UserRepository userRepository;
    @Mock EmailService emailService;
    @Mock TrackingRepository trackingRepository;
    @Mock StatusTransitionValidator validator;

    @InjectMocks ShipmentService shipmentService;

    private Shipment shipment;
    private Driver assignedDriver;
    private DriverLocationUpdateDto location;

    @BeforeEach
    void setUp() {
        assignedDriver = new Driver();
        assignedDriver.setId(10L);
        assignedDriver.setEmail("driver@example.com");
        assignedDriver.setRole(Role.DRIVER);

        shipment = new Shipment();
        shipment.setId(100L);
        shipment.setStatus(Status.ASSIGNED);
        shipment.setDriver(assignedDriver);

        location = new DriverLocationUpdateDto();
        location.setLatitude(28.6139);
        location.setLongitude(77.2090);
    }

    @Test
    void assignedDriverCanUpdateLocation() {
        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));

        shipmentService.updateDriverLocation(100L, location, 10L, Role.DRIVER);

        assertEquals(28.6139, shipment.getCurrentLatitude());
        assertEquals(77.2090, shipment.getCurrentLongitude());
        assertEquals(Status.IN_TRANSIT, shipment.getStatus());
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void differentDriverCannotUpdateLocation() {
        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));

        assertThrows(
                AccessDeniedException.class,
                () -> shipmentService.updateDriverLocation(100L, location, 99L, Role.DRIVER)
        );

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void adminCanUpdateLocation() {
        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));

        assertDoesNotThrow(
                () -> shipmentService.updateDriverLocation(100L, location, 1L, Role.ADMIN)
        );
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void normalUserCannotUpdateLocation() {
        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(shipment));

        assertThrows(
                AccessDeniedException.class,
                () -> shipmentService.updateDriverLocation(100L, location, 1L, Role.USER)
        );
        verify(shipmentRepository, never()).save(any());
    }
}
