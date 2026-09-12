package LogiTrack.Services;

import LogiTrack.Dto.AuthDto;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_VALID_MINUTES = 5;

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    @Transactional
    public void registerUser(AuthDto authDto) {
        String email = authDto.getEmail().trim();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserExistException("Email already in use");
        }

        User newUser = new User();
        newUser.setName(authDto.getUsername().trim());
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(authDto.getPassword()));
        newUser.setRole(Role.USER);

        User saved = userRepository.save(newUser);
        emailService.sendEmail(
                saved.getEmail(),
                "Welcome to LogiTrack",
                "Your account has been created successfully, " + saved.getName()
        );
    }

    @Transactional
    public void generateAndSendOtp(String email) {
        User user = getUserByEmail(email);

        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        user.setOtp(passwordEncoder.encode(otp));
        user.setOtpExpirationTime(LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES));
        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "LogiTrack - Your OTP Code",
                "Hello " + user.getName() + ",\n\n" +
                        "Your OTP for LogiTrack login is: " + otp + "\n\n" +
                        "This code expires in " + OTP_VALID_MINUTES + " minutes.\n" +
                        "Do not share this code with anyone."
        );
    }

    @Transactional
    public boolean verifyOtp(String email, String inputOtp) {
        User user = getUserByEmail(email);

        if (user.getOtp() == null || user.getOtpExpirationTime() == null) {
            return false;
        }

        if (user.getOtpExpirationTime().isBefore(LocalDateTime.now())) {
            user.setOtp(null);
            user.setOtpExpirationTime(null);
            userRepository.save(user);
            return false;
        }

        boolean matches = passwordEncoder.matches(inputOtp, user.getOtp());
        if (!matches) {
            return false;
        }

        // One-time use: clear after successful verification.
        user.setOtp(null);
        user.setOtpExpirationTime(null);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public User updateUser(String currentEmail, AuthDto authDto) {
        User existingUser = getUserByEmail(currentEmail);

        if (authDto.getEmail() != null && !authDto.getEmail().isBlank()) {
            String requestedEmail = authDto.getEmail().trim();
            if (!requestedEmail.equalsIgnoreCase(existingUser.getEmail())) {
                userRepository.findByEmail(requestedEmail).ifPresent(other -> {
                    if (!other.getId().equals(existingUser.getId())) {
                        throw new UserExistException("Email already in use");
                    }
                });
                existingUser.setEmail(requestedEmail);
            }
        }

        if (authDto.getUsername() != null && !authDto.getUsername().isBlank()) {
            existingUser.setName(authDto.getUsername().trim());
        }

        if (authDto.getPassword() != null && !authDto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(authDto.getPassword()));
        }

        User savedUser = userRepository.save(existingUser);
        emailService.sendEmail(
                savedUser.getEmail(),
                "Account Updated",
                "Your account details have been updated."
        );
        return savedUser;
    }

    @Transactional
    public void deleteUser(String email) {
        User user = getUserByEmail(email);
        userRepository.delete(user);
        emailService.sendEmail(
                email,
                "Account Deleted",
                "Your account has been permanently deleted."
        );
    }

    public Optional<User> findByName(String username) {
        return userRepository.findByName(username);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }
}
