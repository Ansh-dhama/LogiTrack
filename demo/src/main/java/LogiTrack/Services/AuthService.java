package LogiTrack.Services;

import LogiTrack.Dto.AuthDto;
import LogiTrack.Dto.ShipmentDto;
import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.User;
import LogiTrack.Enums.Role;
import LogiTrack.Exceptions.UserExistException;
import LogiTrack.Exceptions.UserNotFoundException;
import LogiTrack.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    public User getUserByEmail(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }
    public void registerUser(AuthDto authDto) {

        if (userRepository.findByEmail(authDto.getEmail()).isPresent()) {
            throw new UserExistException("Email already in use");
        }

        User newUser = new User();
        newUser.setName(authDto.getUsername());
        newUser.setEmail(authDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(authDto.getPassword()));
        newUser.setRole(authDto.getRole());

        userRepository.save(newUser);

        // Async email sending recommended here
        emailService.sendEmail(newUser.getEmail(), "Welcome to LogiTrack",
                "Your account has been created successfully, " + newUser.getName());
    }
    @Transactional // Ensures if one part fails, the whole update rolls back
    public User updateUser(String email, AuthDto authDto) {
        User existingUser = getUserByEmail(email);

        if (authDto.getEmail() != null && !authDto.getEmail().isEmpty()) {
            existingUser.setEmail(authDto.getEmail());
        }
        if (authDto.getUsername() != null && !authDto.getUsername().isEmpty()) {
            existingUser.setName(authDto.getUsername());
        }
        if (authDto.getPassword() != null && !authDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(authDto.getPassword()));
        }

        // Save
        User savedUser = userRepository.save(existingUser);

        emailService.sendEmail(savedUser.getEmail(), "Account Updated", "Your account details have been updated.");
        return savedUser;
    }
    @Transactional
    public void deleteUser(String email) {
        User user = getUserByEmail(email);

        userRepository.delete(user);

        emailService.sendEmail(email, "Account Deleted", "Your account has been permanently deleted.");
    }
    public void generateAndSendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate 6-digit random code
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        // Save to database
        user.setOtp(otp);
        user.setOtpExpirationTime(LocalDateTime.now().plusMinutes(5)); // Valid for 5 mins
        userRepository.save(user);

        // Send Email
        emailService.sendOtpEmail(email, otp);
    }
    public boolean verifyOtp(String email, String inputOtp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || user.getOtpExpirationTime() == null) {
            throw new RuntimeException("No OTP requested");
        }

        if (user.getOtpExpirationTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        if (user.getOtp().equals(inputOtp)) {
            // Clear OTP after success so it can't be used twice
            user.setOtp(null);
            user.setOtpExpirationTime(null);
            userRepository.save(user);
            return true;
        } else {
            return false;
        }
    }
    public Optional<User> findByName(String username) {
        return userRepository.findByName(username);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}