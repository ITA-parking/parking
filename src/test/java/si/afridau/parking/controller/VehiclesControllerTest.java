package si.afridau.parking.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.afridau.parking.dto.AddVehicleReq;
import si.afridau.parking.dto.UpdateVehicleReq;
import si.afridau.parking.dto.VehicleDto;
import si.afridau.parking.mapper.VehicleMapper;
import si.afridau.parking.model.Vehicle;
import si.afridau.parking.repository.VehicleRepo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiclesControllerTest {

    @Mock
    VehicleRepo vehicleRepo;
    @Mock
    VehicleMapper vehicleMapper;

    VehiclesController controller;

    @BeforeEach
    void setUp() {
        controller = new VehiclesController(vehicleRepo, vehicleMapper);
    }

    @Test
    void getVehicles_returnsListFromRepo() {
        Vehicle v1 = new Vehicle();
        Vehicle v2 = new Vehicle();
        VehicleDto dto1 = new VehicleDto();
        VehicleDto dto2 = new VehicleDto();
        when(vehicleRepo.findAll()).thenReturn(List.of(v1, v2));
        when(vehicleMapper.toDto(v1)).thenReturn(dto1);
        when(vehicleMapper.toDto(v2)).thenReturn(dto2);

        List<VehicleDto> result = controller.getVehicles();

        assertThat(result).hasSize(2).containsExactly(dto1, dto2);
    }

    @Test
    void addVehicle_savesAndReturnsDto() {
        VehicleDto inputDto = new VehicleDto();
        inputDto.setName("Car A");
        inputDto.setLicensePlate("AB-123");

        AddVehicleReq req = new AddVehicleReq();
        req.setVehicle(inputDto);

        Vehicle entity = new Vehicle();
        Vehicle saved = new Vehicle();
        VehicleDto returnedDto = new VehicleDto();
        returnedDto.setName("Car A");

        when(vehicleMapper.toEntity(inputDto)).thenReturn(entity);
        when(vehicleRepo.save(entity)).thenReturn(saved);
        when(vehicleMapper.toDto(saved)).thenReturn(returnedDto);

        VehicleDto result = controller.addVehicle(req);

        assertThat(result.getName()).isEqualTo("Car A");
        verify(vehicleRepo).save(entity);
    }

    @Test
    void updateVehicle_notFound_throwsRuntimeException() {
        UUID id = UUID.randomUUID();
        UpdateVehicleReq req = new UpdateVehicleReq();
        req.setVehicle(new VehicleDto());
        when(vehicleRepo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.updateVehicle(req, id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getVehicleById_returnsDto() {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = new Vehicle();
        VehicleDto dto = new VehicleDto();
        when(vehicleRepo.findById(id)).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toDto(vehicle)).thenReturn(dto);

        VehicleDto result = controller.getVehicleById(id);

        assertThat(result).isEqualTo(dto);
    }
}
