package LogiTrack.Controller;

import LogiTrack.Dto.*;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtilie jwtUtil;
    private final UserMapper userMapper;

    // STEP 1: password -> generate/send OTP. NO JWT is returned here.
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request) {
        String email = request.getEmail().trim();
        log.info("Attempting customer login step 1 for: {}", email);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        authService.generateAndSendOtp(email);
        return ResponseEntity.ok(
                ApiResponse.success("Password verified. OTP sent to your email.", null)
        );
    }

    // STEP 2: valid OTP -> JWT.
    @PostMapping("/verify-login-otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyLoginOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        String email = request.getEmail().trim();
        log.info("Verifying customer OTP for: {}", email);

        if (!authService.verifyOtp(email, request.getOtp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired OTP", null));
        }

        String token = jwtUtil.generateToken(email);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", new LoginResponse(token))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDto>> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        AuthDto authDto = userMapper.toAuthDto(authService.getUserByEmail(email));
        return ResponseEntity.ok(ApiResponse.success("User profile successfully fetched", authDto));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(@Valid @RequestBody AuthDto authDto) {
        log.info("Registering new user: {}", authDto.getUsername());
        authService.registerUser(authDto);
        return new ResponseEntity<>(
                ApiResponse.success("User registered successfully", null),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ProfileUpdateResponse<AuthDto>>> updateUser(
            @RequestBody AuthDto authDto
    ) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User updatedUser = authService.updateUser(currentEmail, authDto);

        String refreshedToken = jwtUtil.generateToken(updatedUser.getEmail());
        AuthDto updatedProfile = userMapper.toAuthDto(updatedUser);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User updated successfully",
                        new ProfileUpdateResponse<>(updatedProfile, refreshedToken)
                )
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.deleteUser(email);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
