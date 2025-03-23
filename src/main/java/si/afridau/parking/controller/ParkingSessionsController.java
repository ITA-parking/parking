package si.afridau.parking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import si.afridau.parking.dto.AddParkingSessionReq;
import si.afridau.parking.dto.ParkingSessionDto;
import si.afridau.parking.dto.UpdateParkingSessionReq;
import si.afridau.parking.mapper.ParkingSessionMapper;
import si.afridau.parking.model.ParkingSession;
import si.afridau.parking.repository.ParkingSessionRepo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("parking-sessions")
public class ParkingSessionsController {
    private final ParkingSessionRepo parkingSessionRepo;
    private final ParkingSessionMapper parkingSessionMapper;

    @GetMapping("{id}")
    public ParkingSessionDto getParkingSessionById(@PathVariable UUID id) {
        return parkingSessionMapper.toDto(parkingSessionRepo.findById(id).orElse(null));
    }

    @GetMapping()
    public List<ParkingSessionDto> getParkingSessions() {
        return parkingSessionRepo.findAll().stream().map(parkingSessionMapper::toDto).collect(Collectors.toList());
    }

    @PostMapping
    public ParkingSessionDto addParkingSession(
            @RequestBody AddParkingSessionReq req
            ) {
        return parkingSessionMapper.toDto(parkingSessionRepo.save(this.parkingSessionMapper.toEntity(req.getSession())));
    }

    @DeleteMapping("{id}")
    public void deleteParkingSession(@PathVariable UUID id) {
        parkingSessionRepo.deleteById(id);
    }

    @PutMapping("{id}")
    public ParkingSessionDto updateParkingSession(
            @RequestBody UpdateParkingSessionReq req,
            @PathVariable UUID id
            ) {

        //TODO make sure user doesn't modify start time!!!
        Optional<ParkingSession> session = parkingSessionRepo.findById(id);
        if (session.isPresent()) {
            parkingSessionMapper.update(session.get(), req.getSession());
            return parkingSessionMapper.toDto(parkingSessionRepo.save(session.get()));
        }

        throw new RuntimeException("ParkingSession not found");
    }
}
