package LogiTrack;

import LogiTrack.Dto.AuthDto;
import LogiTrack.Entity.User;
import LogiTrack.Enums.Role;
import LogiTrack.Repository.UserRepository;
import LogiTrack.Services.AuthService;
import LogiTrack.Services.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock EmailService emailService;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AuthService authService;

    @Test
    void publicRegistrationAlwaysCreatesUserRole() {
        AuthDto request = new AuthDto();
        request.setUsername("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("secret");
        request.setRole(Role.ADMIN); // malicious/incorrect client input

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.registerUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(Role.USER, captor.getValue().getRole());
    }

    @Test
    void profileUpdateNeverChangesRole() {
        User existing = new User();
        existing.setId(7L);
        existing.setName("Alice");
        existing.setEmail("alice@example.com");
        existing.setPassword("old-hash");
        existing.setRole(Role.USER);

        AuthDto update = new AuthDto();
        update.setUsername("Alice Updated");
        update.setRole(Role.ADMIN);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User saved = authService.updateUser("alice@example.com", update);

        assertEquals(Role.USER, saved.getRole());
        assertEquals("Alice Updated", saved.getName());
    }
}
