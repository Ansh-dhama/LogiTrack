package LogiTrack.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileUpdateResponse<T> {
    private T profile;
    private String token;
}
