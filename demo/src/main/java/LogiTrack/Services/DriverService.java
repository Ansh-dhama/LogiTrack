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
        Driver driver = driverRepository.findByEmail(email);
        if (driver == null) {
            throw new DriverNotFoundException("Driver not found: " + email);
        }
        return driverMapper.toDto(driver);
    }

    @Transactional
    public void registerDriver(DriverDto dto) {
        if (driverRepository.findByEmail(dto.getEmail()) != null) {
            throw new DriverExistException("Driver Email already exists.");
        }

        Driver driver = new Driver();
        driver.setDriverName(dto.getDriverName());
        driver.setEmail(dto.getEmail());
        driver.setPassword(passwordEncoder.encode(dto.getPassword()));
        driver.setRole(Role.DRIVER);
        driver.setAvailable(false);

        Driver savedDriver = driverRepository.save(driver);
        emailService.sendEmail(savedDriver.getEmail(), "Welcome Driver",
                "Welcome to the team, " + savedDriver.getDriverName());
    }

    @Transactional
    public DriverDto updateDriver(DriverDto dto, String email) {
        Driver driver = driverRepository.findByEmail(email);
        if (driver == null) {
            throw new DriverNotFoundException("Driver profile not found");
        }

        boolean isUpdated = false;

        // Update availability if provided in DTO
        driver.setAvailable(dto.isAvailable());

        if (dto.getDriverName() != null && !dto.getDriverName().isEmpty()) {
            driver.setDriverName(dto.getDriverName());
            isUpdated = true;
        }

        if (isUpdated) {
            driverRepository.save(driver);
        }
        return driverMapper.toDto(driver);
    }

    @Transactional
    public void deleteDriver(String email) {
        Driver driver = driverRepository.findByEmail(email);
        if (driver == null) {
            throw new DriverNotFoundException("Driver not found: " + email);
        }
        driverRepository.delete(driver);
        emailService.sendEmail(email, "Account Deleted", "Your account has been deleted.");
    }

    public DriverStatus checkStatus(String email) {
        Driver driver = driverRepository.findByEmail(email);
        DriverStatus status = new DriverStatus();
        status.setStatus(driver.isAvailable());
        status.setEmail(email);
        return status;
    }

    @Transactional
    public void updateStatus(DriverStatus dto) {
        Driver driver = driverRepository.findByEmail(dto.getEmail());
        if (driver != null && dto.getStatus() != driver.isAvailable()) {
            driver.setAvailable(dto.getStatus());
            driverRepository.save(driver);
        }
    }
}