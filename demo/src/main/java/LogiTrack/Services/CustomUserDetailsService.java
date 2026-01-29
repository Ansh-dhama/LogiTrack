<<<<<<< HEAD
    package LogiTrack.Services;

    import LogiTrack.Entity.Driver;
    import LogiTrack.Entity.User;
    import LogiTrack.Repository.DriverRepository;
    import LogiTrack.Repository.UserRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.stereotype.Service;

    import java.util.Optional;

    @Service
    public class CustomUserDetailsService implements UserDetailsService {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private DriverRepository driverRepository;

        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

            // 1. Check User Table by EMAIL (Not Name!)
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                System.out.println("DEBUG: Found USER by email: " + user.get().getEmail());

                return buildUserDetails(
                        user.get().getEmail(), // Set username as email for SecurityContext
                        user.get().getPassword(),
                        user.get().getRole().toString()
                );
            }

            // 2. Check Driver Table by EMAIL (Not Name!)
            // Note: Make sure DriverRepository has 'Optional<Driver> findByEmail(String email);'
          Driver driver = driverRepository.findByEmail(email);
            if (driver != null) {
                System.out.println("DEBUG: Found DRIVER by email: " + driver.getEmail());
                return org.springframework.security.core.userdetails.User
                        .builder()
                        .username(driver.getEmail())
                        .password(driver.getPassword())
                        .roles("DRIVER")
                        .build();
            }

            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        private UserDetails buildUserDetails(String username, String password, String role) {
            String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            return org.springframework.security.core.userdetails.User

                    .builder()
                    .username(username)
                    .password(password)
                    .authorities(authority)
                    .build();
        }
    }
=======
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

        // 1. Check User Table
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

        // 2. Check Driver Table
        Driver driver = driverRepository.findByEmail(email);
        if (driver != null) {
            // Assuming Driver entity doesn't have a 'Role' field stored in DB,
            // we hardcode Role.DRIVER here.
            return new CustomUserDetails(
                    driver.getId(),
                    driver.getEmail(),
                    driver.getPassword(),
                    Role.DRIVER
            );
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
>>>>>>> c8ec02f (initial commit for LogiTrack)
