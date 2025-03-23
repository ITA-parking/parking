package si.afridau.parking.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VehicleDto {
    private UUID id;
    private String name;
    private String licensePlate;
}
