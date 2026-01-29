package LogiTrack.Controller;

import LogiTrack.Dto.*;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.User;
import LogiTrack.MapStructs.UserMapper;
import LogiTrack.Services.AuthService;
import LogiTrack.Util.JwtUtilie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor // Lombok generates constructor for final fields automatically
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager; // Inject Manager
    private final JwtUtilie jwtUtil; // Inject Utils
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Attempting login for: {}", request.getEmail());


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        String token = jwtUtil.generateToken(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Token generated succesfully",new LoginResponse(token)));
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        String Email = SecurityContextHolder.getContext().getAuthentication().getName();
        AuthDto authDto = userMapper.toAuthDto(authService.getUserByEmail(Email));
        return ResponseEntity.ok(ApiResponse.success("User profile succesfully fetched",authDto));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody AuthDto authDto) {
        log.info("Registering new user: {}", authDto.getUsername());
           authService.registerUser(authDto);
            return new ResponseEntity<>(ApiResponse.success("User registered successfully",null), HttpStatus.CREATED);
    }
    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody AuthDto authDto) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            authService.updateUser(currentUsername, authDto);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully",null));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
            authService.deleteUser(username);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully",null));
    }
}