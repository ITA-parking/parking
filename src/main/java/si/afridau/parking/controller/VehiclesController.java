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
import si.afridau.parking.dto.AddVehicleReq;
import si.afridau.parking.dto.UpdateVehicleReq;
import si.afridau.parking.dto.VehicleDto;
import si.afridau.parking.mapper.VehicleMapper;
import si.afridau.parking.model.Vehicle;
import si.afridau.parking.repository.VehicleRepo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("vehicles")
public class VehiclesController {
    private final VehicleRepo vehicleRepo;
    private final VehicleMapper vehicleMapper;

    @GetMapping("{id}")
    public VehicleDto getVehicleById(@PathVariable UUID id) {
        return vehicleMapper.toDto(vehicleRepo.findById(id).orElse(null));
    }

    @GetMapping()
    public List<VehicleDto> getVehicles() {
        return vehicleRepo.findAll().stream().map(vehicleMapper::toDto).collect(Collectors.toList());
    }

    @PostMapping
    public VehicleDto addVehicle(
            @RequestBody AddVehicleReq req
            ) {
        return vehicleMapper.toDto(vehicleRepo.save(this.vehicleMapper.toEntity(req.getVehicle())));
    }

    @DeleteMapping("{id}")
    public void deleteVehicle(@PathVariable UUID id) {
        vehicleRepo.deleteById(id);
    }

    @PutMapping("{id}")
    public VehicleDto updateVehicle(
            @RequestBody UpdateVehicleReq req,
            @PathVariable UUID id
            ) {

        Optional<Vehicle> vehicle = vehicleRepo.findById(id);
        if (vehicle.isPresent()) {
            vehicleMapper.update(vehicle.get(), req.getVehicle());
            return vehicleMapper.toDto(vehicleRepo.save(vehicle.get()));
        }

        throw new RuntimeException("Vehicle not found");
    }
}
