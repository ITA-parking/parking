package si.afridau.parking.mapper;


import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import si.afridau.parking.dto.AddVehicleReq;
import si.afridau.parking.dto.VehicleDto;
import si.afridau.parking.model.Vehicle;

@Mapper(componentModel = "spring", uses = VehicleMapper.class)
public interface VehicleMapper {
    Vehicle toEntity(VehicleDto dto);
    VehicleDto toDto(Vehicle vehicle);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Vehicle vehicle, VehicleDto dto);
}
