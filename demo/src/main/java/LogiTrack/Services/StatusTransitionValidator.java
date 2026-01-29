package LogiTrack.Services;

import LogiTrack.Enums.Status;
import LogiTrack.Enums.Role;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class StatusTransitionValidator {

    public void validateTransition(Status current, Status next, Role role) {
        // 1. ADMIN OVERRIDE (Admins can fix data, e.g., reset a status)
        if (role == Role.ADMIN) return;

        // 2. CHECK PATH
        if (!isValidPath(current, next)) {
            throw new IllegalArgumentException("Invalid status flow: Cannot go from " + current + " to " + next);
        }

        // 3. CHECK PERMISSIONS
        if (!isAllowedForRole(current, next, role)) {
            throw new SecurityException("Role " + role + " is not authorized to perform this action.");
        }
    }

    private boolean isValidPath(Status current, Status next) {
        switch (current) {
            case PENDING:
                return Set.of(Status.CREATED, Status.CANCELLED).contains(next);
            case CREATED:
                return Set.of(Status.ASSIGNED, Status.CANCELLED).contains(next);
            case ASSIGNED:
                return Set.of(Status.IN_TRANSIT, Status.CANCELLED).contains(next);

            case IN_TRANSIT:
                // ✅ UPDATED: Driver can Deliver or Mark Attempted (Failed)
                return Set.of(
                        Status.DELIVERED,
                        Status.DELIVERY_ATTEMPTED // <--- New Path
                ).contains(next);

            case DELIVERY_ATTEMPTED:
                // ✅ NEW STATE LOGIC:
                // 1. Retry next day (Go back to IN_TRANSIT)
                // 2. Success on retry (DELIVERED)
                // 3. Max attempts reached (RETURNED - usually set by System)
                return Set.of(
                        Status.IN_TRANSIT,
                        Status.DELIVERED,
                        Status.RETURNED
                ).contains(next);

            case DELIVERED:
                return false; // ❌ STRICT: No returns after successful delivery

            case CANCELLED:
            case RETURNED:
                return false; // Terminal states
            default:
                return false;
        }
    }

    private boolean isAllowedForRole(Status current, Status next, Role role) {
        switch (role) {
            case USER:
                // User can only cancel early
                if (next == Status.CANCELLED) {
                    return current == Status.PENDING || current == Status.CREATED;
                }
                return false;

            case DRIVER:
                // 1. Start Journey
                if (current == Status.ASSIGNED && next == Status.IN_TRANSIT) return true;

                // 2. Finish Journey (Success)
                if (current == Status.IN_TRANSIT && next == Status.DELIVERED) return true;
                if (current == Status.DELIVERY_ATTEMPTED && next == Status.DELIVERED) return true; // Success on retry

                // 3. ✅ NEW: Driver reports "Customer Not Home" (Attempted)
                if (current == Status.IN_TRANSIT && next == Status.DELIVERY_ATTEMPTED) return true;

                // 4. ✅ NEW: Driver retries delivery next day (Start ride again)
                if (current == Status.DELIVERY_ATTEMPTED && next == Status.IN_TRANSIT) return true;

                return false;

            case ADMIN:
                return true; // Admin can do anything

            default:
                return false;
        }
    }
}