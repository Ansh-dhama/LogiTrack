package LogiTrack;

import LogiTrack.Exceptions.ShipmentNotFoundException;
import LogiTrack.Repository.TrackingRepository;
import LogiTrack.Services.TrackingUpdatesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingUpdatesServiceTest {

    @Mock TrackingRepository trackingRepository;
    @InjectMocks TrackingUpdatesService trackingUpdatesService;

    @Test
    void missingTrackingNumberRaisesNotFoundException() {
        when(trackingRepository.findBytrackingNumber("TRK-MISSING")).thenReturn(null);

        assertThrows(
                ShipmentNotFoundException.class,
                () -> trackingUpdatesService.findByTrackingNumber("TRK-MISSING")
        );
    }
}
