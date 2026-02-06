package LogiTrack.Services;

import LogiTrack.Entity.TrackingEvent;
import LogiTrack.Enums.Role;
import LogiTrack.Enums.Status;
import LogiTrack.Repository.TrackingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrackingEventService {
  private final TrackingEventRepository repo;

  public void logStatus(String trackingNumber, Status status, Role role, Long userId, String remark) {
      TrackingEvent e = new TrackingEvent();
      e.setTrackingNumber(trackingNumber);
      e.setStatus(status);
      e.setAtTime(LocalDateTime.now());
      e.setByRole(role);
      e.setByUserId(userId);
      e.setRemark(remark);

      repo.save(e);
  }

  public void logLocation(String trackingNumber, Role role, Long userId, double lat, double lng) {
      TrackingEvent e = new TrackingEvent();
      e.setTrackingNumber(trackingNumber);
      e.setStatus(null); // optional
      e.setAtTime(LocalDateTime.now());
      e.setByRole(role);
      e.setByUserId(userId);
      e.setLat(lat);
      e.setLng(lng);
      e.setRemark("LOCATION_UPDATE");
      repo.save(e);
  }

    public void save(TrackingEvent tk) {
      repo.save(tk);
    }
}
