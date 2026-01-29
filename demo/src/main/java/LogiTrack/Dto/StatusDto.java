package LogiTrack.Dto;

import LogiTrack.Enums.Status;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.convert.DataSizeUnit;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusDto {
    Status status;
    String trackingNumber;
}
