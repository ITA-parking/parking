package si.afridau.parking.dto;

import lombok.Getter;
import lombok.Setter;
import si.afridau.parking.model.Vehicle;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
public class ParkingSessionDto {
    private UUID id;
    private LocalDateTime from;
    private LocalDateTime to;

    private String licensePlate;
    private VehicleDto vehicle;
}
