package LogiTrack.Services;

import LogiTrack.Entity.Driver;
import LogiTrack.Entity.User;
import LogiTrack.Enums.Role;
import LogiTrack.Repository.DriverRepository;
import LogiTrack.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Search in the User table first
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new CustomUserDetails(
                    user.getId(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole()
            );
        }

        // 2. Search in the Driver table if not found in User table
        Driver driver = driverRepository.findByEmail(email);
        if (driver != null) {
            return new CustomUserDetails(
                    driver.getId(),
                    driver.getEmail(),
                    driver.getPassword(),
                    Role.DRIVER // Explicitly assigning the DRIVER role
            );
        }

        throw new UsernameNotFoundException("User or Driver not found with email: " + email);
    }
}