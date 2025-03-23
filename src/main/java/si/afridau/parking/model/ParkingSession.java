package si.afridau.parking.model;


import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ParkingSession extends BaseModel {
    private LocalDateTime from;
    private LocalDateTime to;

    private String licensePlate;
    //TODO when parking spot service is implemented we have to add parking spot to this!
    private UUID vehicleId;
}
