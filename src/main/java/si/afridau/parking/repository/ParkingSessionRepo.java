package si.afridau.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.afridau.parking.model.ParkingSession;

import java.util.UUID;

@Repository
public interface ParkingSessionRepo extends JpaRepository<ParkingSession, UUID> {
}
