package LogiTrack;

import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.User;
import LogiTrack.Enums.Status;
import LogiTrack.Exceptions.UserNotFoundException;
import LogiTrack.Repository.UserRepository;
import LogiTrack.Services.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    private User sender;
    private Driver driverBusy;
    private Driver driverFree;
    private Driver driverUnavailable;
    private Driver driverIsTheSender;

    @BeforeEach
    void setUp() {
        // 1. Setup Sender
        sender = new User();
        sender.setId(1L);
        sender.setEmail("sender@test.com");

        // 2. Setup Driver A (Busy - 5 active shipments)
        driverBusy = createDriver(10L, "busy@test.com", true, 5);

        // 3. Setup Driver B (Free - 1 active shipment)
        driverFree = createDriver(11L, "free@test.com", true, 1);

        // 4. Setup Driver C (Unavailable)
        driverUnavailable = createDriver(12L, "lazy@test.com", false, 0);

        // 5. Setup Driver D (Same email as sender)
        driverIsTheSender = createDriver(13L, "sender@test.com", true, 0);
    }

    @Test
    void testFindBestDriver_LoadBalancing() {
        // Scenario: Both are available, but 'driverFree' has fewer shipments.
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        List<Driver> drivers = Arrays.asList(driverBusy, driverFree);

        // CALL PRIVATE METHOD
        Driver result = invokeFindBestDriver(drivers, 1L);

        assertNotNull(result);
        assertEquals(driverFree.getId(), result.getId(), "Should pick the driver with fewest active shipments");
    }

    @Test
    void testFindBestDriver_AvailabilityCheck() {
        // Scenario: 'driverUnavailable' has 0 jobs (best count), but isAvailable=false.
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        List<Driver> drivers = Arrays.asList(driverUnavailable, driverBusy);

        Driver result = invokeFindBestDriver(drivers, 1L);

        assertNotNull(result);
        assertEquals(driverBusy.getId(), result.getId(), "Should skip unavailable drivers even if they are free");
    }

    @Test
    void testFindBestDriver_ExcludeSender() {
        // Scenario: The sender is also registered as a driver. Should not assign to self.
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        List<Driver> drivers = Arrays.asList(driverIsTheSender, driverBusy);

        Driver result = invokeFindBestDriver(drivers, 1L);

        assertNotNull(result);
        assertEquals(driverBusy.getId(), result.getId(), "Should not assign shipment to the sender");
    }

    @Test
    void testFindBestDriver_UserNotFound() {
        // Scenario: Sender ID doesn't exist
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        List<Driver> drivers = Collections.singletonList(driverFree);

        assertThrows(UserNotFoundException.class, () -> {
            invokeFindBestDriver(drivers, 999L);
        });
    }

    @Test
    void testFindBestDriver_NoDriversFound() {
        // Scenario: List is empty
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        Driver result = invokeFindBestDriver(Collections.emptyList(), 1L);

        assertNull(result, "Should return null if list is empty");
    }

    // --- Helpers ---

    /**
     * Helper to bypass Private access using Reflection
     */
    private Driver invokeFindBestDriver(List<Driver> drivers, Long senderId) {
        return ReflectionTestUtils.invokeMethod(shipmentService, "findBestDriver", drivers, senderId);
    }

    /**
     * Helper to create a driver with dummy shipments
     */
    private Driver createDriver(Long id, String email, boolean available, int activeShipmentCount) {
        Driver d = new Driver();
        d.setId(id);
        d.setEmail(email);
        d.setAvailable(available);
        d.setShipments(new ArrayList<>());

        // Add dummy active shipments to simulate load
        for (int i = 0; i < activeShipmentCount; i++) {
            Shipment s = new Shipment();
            s.setStatus(Status.IN_TRANSIT); // Assuming this counts as active
            d.getShipments().add(s);
        }
        return d;
    }
}