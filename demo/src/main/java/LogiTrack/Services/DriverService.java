<<<<<<< HEAD
package LogiTrack.Services;

import LogiTrack.Dto.DriverDto;
import LogiTrack.Dto.DriverStatus;
import LogiTrack.Dto.LoginRequest;
import LogiTrack.Dto.ShipmentDto; // Ensure this import
import LogiTrack.Entity.Driver;
import LogiTrack.Enums.Role;
import LogiTrack.Exceptions.DriverExistException;
import LogiTrack.Exceptions.DriverNotFoundException;
import LogiTrack.MapStructs.DriverMapper;
import LogiTrack.Repository.DriverRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        return driverMapper.toDto(driver); // Use the mapper!
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

        Driver savedDriver = driverRepository.save(driver);

        emailService.sendEmail(savedDriver.getEmail(), "Welcome Driver",
                "Welcome to the team, " + savedDriver.getDriverName());

    }

    @Transactional
    public DriverDto updateDriver(DriverDto dto, String email) {
        Driver driver = driverRepository.findByEmail(email);
        if (driver == null) {
            throw new DriverNotFoundException("Driver profile not found with this  email");
        }

        boolean isUpdated = false;

        if (dto.getDriverName() != null && !dto.getDriverName().isEmpty()) {
            driver.setDriverName(dto.getDriverName());
            isUpdated = true;
        }

        if (isUpdated) {
            driverRepository.save(driver);
        }
        return driverMapper.toDto(driver); // Return the updated profile as DTO
    }

    @Transactional
    public void deleteDriver(String email) {
        Driver driver = driverRepository.findByEmail(email);
        if (driver == null) {
            throw new DriverNotFoundException("Driver not found with this email" + email);
        }
        driverRepository.delete(driver);
        emailService.sendEmail(email, "Account Deleted", "Your account has been deleted.");
    }
    public DriverStatus checkStatus(String email) {
        Driver d1 = driverRepository.findByEmail(email);
        DriverStatus d2 = new DriverStatus();
        d2.setStatus(d1.isAvailable());
        d2.setEmail(email);
        return d2;
    }


    public void updateStatus(DriverStatus dto) {
       Driver driver = driverRepository.findByEmail(dto.getEmail());
       if(dto.getStatus() != driver.isAvailable()) {
           driver.setAvailable(dto.getStatus());
           DriverService.this.updateDriver(driverMapper.toDto(driver), driver.getEmail());
       }
    }
}
=======
    package LogiTrack.Services;

    import LogiTrack.Dto.DriverDto;
    import LogiTrack.Dto.DriverStatus;
    import LogiTrack.Dto.LoginRequest;
    import LogiTrack.Dto.ShipmentDto; // Ensure this import
    import LogiTrack.Entity.Driver;
    import LogiTrack.Enums.Role;
    import LogiTrack.Exceptions.DriverExistException;
    import LogiTrack.Exceptions.DriverNotFoundException;
    import LogiTrack.MapStructs.DriverMapper;
    import LogiTrack.Repository.DriverRepository;
    import lombok.AllArgsConstructor;
    import lombok.NoArgsConstructor;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.bind.annotation.RequestBody;

    import java.util.List;
    import java.util.Optional;
    import java.util.stream.Collectors;

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
            return driverMapper.toDto(driver); // Use the mapper!
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

            Driver savedDriver = driverRepository.save(driver);

            emailService.sendEmail(savedDriver.getEmail(), "Welcome Driver",
                    "Welcome to the team, " + savedDriver.getDriverName());

        }

        @Transactional
        public DriverDto updateDriver(DriverDto dto, String email) {
            Driver driver = driverRepository.findByEmail(email);
            if (driver == null) {
                throw new DriverNotFoundException("Driver profile not found with this  email");
            }

            boolean isUpdated = false;
            driver.setAvailable(dto.isAvailable());
            if (dto.getDriverName() != null && !dto.getDriverName().isEmpty()) {
                driver.setDriverName(dto.getDriverName());
                isUpdated = true;
            }

            if (isUpdated) {
                driverRepository.save(driver);
            }
            return driverMapper.toDto(driver); // Return the updated profile as DTO
        }

        @Transactional
        public void deleteDriver(String email) {
            Driver driver = driverRepository.findByEmail(email);
            if (driver == null) {
                throw new DriverNotFoundException("Driver not found with this email" + email);
            }
            driverRepository.delete(driver);
            emailService.sendEmail(email, "Account Deleted", "Your account has been deleted.");
        }
        public DriverStatus checkStatus(String email) {
            Driver d1 = driverRepository.findByEmail(email);
            DriverStatus d2 = new DriverStatus();
            d2.setStatus(d1.isAvailable());
            d2.setEmail(email);
            return d2;
        }


        public void updateStatus(DriverStatus dto) {
           Driver driver = driverRepository.findByEmail(dto.getEmail());
           if(dto.getStatus() != driver.isAvailable()) {
               driver.setAvailable(dto.getStatus());
               DriverService.this.updateDriver(driverMapper.toDto(driver), driver.getEmail());
           }
        }
    }
>>>>>>> c8ec02f (initial commit for LogiTrack)
