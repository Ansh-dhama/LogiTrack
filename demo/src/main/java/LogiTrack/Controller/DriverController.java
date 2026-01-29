    package LogiTrack.Controller;

    import LogiTrack.Dto.*;
    import LogiTrack.Services.DriverService;
    import LogiTrack.Util.JwtUtilie;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.validation.annotation.Validated;
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
        @GetMapping("/profile")
        public ResponseEntity<ApiResponse<DriverDto>> getDriverProfile() {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            // Service now returns DTO directly. Clean!
            DriverDto profile = driverService.getDriverProfile(email);

            return ResponseEntity.ok(
                    ApiResponse.success("Driver profile fetched", profile)
            );
        }

        @PostMapping("/register")
        public ResponseEntity<ApiResponse<String>> registerDriver(@Validated @RequestBody DriverDto dto) {
            driverService.registerDriver(dto);
            return new ResponseEntity<>(
                    ApiResponse.success("Driver registered successfully", null),
                    HttpStatus.CREATED
            );
        }

        @PutMapping("/update")
        public ResponseEntity<ApiResponse<DriverDto>> updateDriver(@Validated @RequestBody DriverDto dto) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            DriverDto updatedProfile = driverService.updateDriver(dto, email);

            return ResponseEntity.ok(
                    ApiResponse.success("Driver updated successfully", updatedProfile)
            );
        }
        @DeleteMapping("/delete")
        public ResponseEntity<ApiResponse<Void>> deleteDriver() {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();

            driverService.deleteDriver(email);

            return ResponseEntity.ok(
                    ApiResponse.success("Driver deleted successfully", null)
            );
        }
        @PostMapping("/status")
        public ResponseEntity<ApiResponse<Void>>  updateDriverStatus(@Validated @RequestBody DriverStatus dto) {
            driverService.updateStatus(dto);
            if(dto.getStatus()== true) {
                return ResponseEntity.ok(ApiResponse.success("you are live now", null));
            }else{
                return ResponseEntity.ok(ApiResponse.success("you are offline now", null));
            }
        }

        @GetMapping("/checkStatus")
            public ResponseEntity<ApiResponse<DriverStatus>> getDriverStatus() {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
              DriverStatus status =   driverService.checkStatus(email);

                return ResponseEntity.ok(
                        ApiResponse.success(
                        "Driver status is",status)
                );
            }
        }
