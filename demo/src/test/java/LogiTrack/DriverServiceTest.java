package LogiTrack;

import LogiTrack.Dto.DriverDto;
import LogiTrack.Entity.Driver;
import LogiTrack.Enums.Role;
import LogiTrack.MapStructs.DriverMapper;
import LogiTrack.Repository.DriverRepository;
import LogiTrack.Services.DriverService;
import LogiTrack.Services.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock DriverMapper driverMapper;
    @Mock DriverRepository driverRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;

    @InjectMocks DriverService driverService;

    @Test
    void statusUpdateUsesAuthenticatedEmail() {
        Driver driver = new Driver();
        driver.setId(11L);
        driver.setEmail("trusted@example.com");
        driver.setAvailable(false);
        driver.setRole(Role.DRIVER);

        when(driverRepository.findByEmail("trusted@example.com")).thenReturn(driver);

        driverService.updateStatus("trusted@example.com", true);

        assertTrue(driver.isAvailable());
        verify(driverRepository).findByEmail("trusted@example.com");
        verify(driverRepository).save(driver);
    }

    @Test
    void profileUpdateSupportsEmailAndPassword() {
        Driver driver = new Driver();
        driver.setId(12L);
        driver.setDriverName("Old Name");
        driver.setEmail("old@example.com");
        driver.setPassword("old-hash");
        driver.setRole(Role.DRIVER);

        DriverDto request = new DriverDto();
        request.setDriverName("New Name");
        request.setEmail("new@example.com");
        request.setPassword("new-password");

        DriverDto mapped = new DriverDto();
        mapped.setId(12L);
        mapped.setDriverName("New Name");
        mapped.setEmail("new@example.com");
        mapped.setRole(Role.DRIVER);

        when(driverRepository.findByEmail("old@example.com")).thenReturn(driver);
        when(driverRepository.findByEmail("new@example.com")).thenReturn(null);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
        when(driverRepository.save(driver)).thenReturn(driver);
        when(driverMapper.toDto(driver)).thenReturn(mapped);

        DriverDto result = driverService.updateDriver(request, "old@example.com");

        assertEquals("new@example.com", driver.getEmail());
        assertEquals("new-hash", driver.getPassword());
        assertEquals("New Name", result.getDriverName());
    }
}
