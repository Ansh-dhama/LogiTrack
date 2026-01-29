package LogiTrack.Controller;

import LogiTrack.Dto.AdminDashboardDto;
import LogiTrack.Dto.LoginRequest;
import LogiTrack.Dto.LoginResponse;
import LogiTrack.Entity.Shipment;
import LogiTrack.Services.AdminService;
import LogiTrack.Util.JwtUtilie;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // 👈 Import this
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
<<<<<<< HEAD
@PreAuthorize("hasRole('ADMIN')")
=======
>>>>>>> c8ec02f (initial commit for LogiTrack)
public class AdminController {

    private final AdminService adminService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtilie jwtUtil;

    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginAdmin(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtUtil.generateToken(request.getEmail());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // ✅ Protected by class-level @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/shipments")
    public ResponseEntity<List<Shipment>> getAllShipments() {
        return ResponseEntity.ok(adminService.getAllShipments());
    }

    @PutMapping("/assign/{shipmentId}/{driverId}")
    public ResponseEntity<?> assignDriver(@PathVariable Long shipmentId, @PathVariable Long driverId) {
        adminService.assignDriverToShipment(shipmentId, driverId);
        return ResponseEntity.ok("Driver assigned successfully");
    }

    @PutMapping("/driver-status/{driverId}")
    public ResponseEntity<?> toggleDriverStatus(@PathVariable Long driverId) {
        return ResponseEntity.ok(adminService.toggleDriverAvailability(driverId));
    }
}