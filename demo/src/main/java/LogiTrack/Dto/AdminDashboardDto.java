package LogiTrack.Dto;

import lombok.Data;

@Data
public class AdminDashboardDto {
    private long totalShipments;
    private long pendingShipments;
    private long deliveredShipments;
    private long shipmentsToday;
    private long totalDrivers;
}