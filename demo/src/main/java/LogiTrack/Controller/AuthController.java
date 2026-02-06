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
        log.info("Attempting login (Step 1) for: {}", request.getEmail());

        // A. Verify Password with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // B. Generate & Send OTP (Do NOT return token yet)
        authService.generateAndSendOtp(request.getEmail());

        return ResponseEntity.ok(ApiResponse.success("Password verified. OTP sent to your email.", null));
    }

    // ----------------------------------------------------------------
    // 2. STEP 2: VERIFY OTP (OTP Check -> Return Token)
    // ----------------------------------------------------------------
    @PostMapping("/verify-login-otp")
    public ResponseEntity<ApiResponse> verifyLoginOtp(@RequestBody VerifyOtpRequest otpRequest) {
        log.info("Verifying OTP for: {}", otpRequest.getEmail());

        // A. Check if OTP is valid in Database
        boolean isValid = authService.verifyOtp(otpRequest.getEmail(),otpRequest.getOtp());

        if (isValid) {
            // B. Generate Token NOW (Only after OTP matches)
            String token = jwtUtil.generateToken(otpRequest.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Login Successful. Token generated.", new LoginResponse(token)));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or Expired OTP", null));
        }
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