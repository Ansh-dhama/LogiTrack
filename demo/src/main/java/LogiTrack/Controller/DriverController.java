package LogiTrack.Controller;

import LogiTrack.Dto.*;
import LogiTrack.Services.DriverService;
import LogiTrack.Util.JwtUtilie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtilie jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginDriver(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtUtil.generateToken(request.getEmail());
        return ResponseEntity.ok(
                ApiResponse.success("Token generated successfully", new LoginResponse(token))
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerDriver(@Valid @RequestBody DriverDto dto) {
        driverService.registerDriver(dto);
        return new ResponseEntity<>(
                ApiResponse.success("Driver registered successfully", null),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DriverDto>> getDriverProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(
                ApiResponse.success("Driver profile fetched", driverService.getDriverProfile(email))
        );
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<ProfileUpdateResponse<DriverDto>>> updateDriver(@RequestBody DriverDto dto) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        DriverDto updatedProfile = driverService.updateDriver(dto, currentEmail);

        // Email is the JWT subject, so issue a fresh token after profile changes.
        String refreshedToken = jwtUtil.generateToken(updatedProfile.getEmail());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Driver updated successfully",
                        new ProfileUpdateResponse<>(updatedProfile, refreshedToken)
                )
        );
    }

    @PreAuthorize("hasRole('DRIVER')")
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteDriver() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        driverService.deleteDriver(email);
        return ResponseEntity.ok(ApiResponse.success("Driver deleted successfully", null));
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/status")
    public ResponseEntity<ApiResponse<Void>> updateDriverStatus(@Valid @RequestBody DriverStatus dto) {
        String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // SECURITY: never trust dto.email. The authenticated principal decides
        // which driver row can be changed.
        driverService.updateStatus(authenticatedEmail, dto.getStatus());

        String message = Boolean.TRUE.equals(dto.getStatus())
                ? "you are live now"
                : "you are offline now";
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping("/checkStatus")
    public ResponseEntity<ApiResponse<DriverStatus>> getDriverStatus() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        DriverStatus status = driverService.checkStatus(email);
        return ResponseEntity.ok(ApiResponse.success("Driver status is", status));
    }
}
