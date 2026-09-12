package LogiTrack.Services;

import LogiTrack.Dto.DriverDto;
import LogiTrack.Dto.DriverStatus;
import LogiTrack.Entity.Driver;
import LogiTrack.Enums.Role;
import LogiTrack.Exceptions.DriverExistException;
import LogiTrack.Exceptions.DriverNotFoundException;
import LogiTrack.MapStructs.DriverMapper;
import LogiTrack.Repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverService {

    private final DriverMapper driverMapper;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public DriverDto getDriverProfile(String email) {
        return driverMapper.toDto(requireDriver(email));
    }

    @Transactional
    public void registerDriver(DriverDto dto) {
        if (driverRepository.findByEmail(dto.getEmail()) != null) {
            throw new DriverExistException("Driver email already exists.");
        }

        Driver driver = new Driver();
        driver.setDriverName(dto.getDriverName());
        driver.setEmail(dto.getEmail().trim());
        driver.setPassword(passwordEncoder.encode(dto.getPassword()));
        driver.setRole(Role.DRIVER);
        driver.setAvailable(false);

        Driver savedDriver = driverRepository.save(driver);
        emailService.sendEmail(savedDriver.getEmail(), "Welcome Driver",
                "Welcome to the team, " + savedDriver.getDriverName());
    }

    @Transactional
    public DriverDto updateDriver(DriverDto dto, String currentEmail) {
        Driver driver = requireDriver(currentEmail);

        if (dto.getDriverName() != null && !dto.getDriverName().isBlank()) {
            driver.setDriverName(dto.getDriverName().trim());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String requestedEmail = dto.getEmail().trim();
            if (!requestedEmail.equalsIgnoreCase(driver.getEmail())) {
                Driver owner = driverRepository.findByEmail(requestedEmail);
                if (owner != null && !owner.getId().equals(driver.getId())) {
                    throw new DriverExistException("Driver email already exists.");
                }
                driver.setEmail(requestedEmail);
            }
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            driver.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Availability is controlled only through /driver/status.
        // A profile update must not silently put a driver online/offline.
        Driver saved = driverRepository.save(driver);
        return driverMapper.toDto(saved);
    }

    @Transactional
    public void deleteDriver(String email) {
        Driver driver = requireDriver(email);
        driverRepository.delete(driver);
        emailService.sendEmail(email, "Account Deleted", "Your account has been deleted.");
    }

    public DriverStatus checkStatus(String email) {
        Driver driver = requireDriver(email);
        DriverStatus status = new DriverStatus();
        status.setStatus(driver.isAvailable());
        status.setEmail(driver.getEmail());
        return status;
    }

    @Transactional
    public void updateStatus(String authenticatedEmail, Boolean requestedStatus) {
        Driver driver = requireDriver(authenticatedEmail);
        if (requestedStatus == null) {
            throw new IllegalArgumentException("Driver status is required");
        }

        if (driver.isAvailable() != requestedStatus) {
            driver.setAvailable(requestedStatus);
            driverRepository.save(driver);
        }
    }

    private Driver requireDriver(String email) {
        Driver driver = driverRepository.findByEmail(email);
        if (driver == null) {
            throw new DriverNotFoundException("Driver not found: " + email);
        }
        return driver;
    }
}
