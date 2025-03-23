package si.afridau.parking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import si.afridau.parking.model.Vehicle;

import java.util.UUID;

@Repository
public interface VehicleRepo extends JpaRepository<Vehicle, UUID> {
}
