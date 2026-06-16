package si.afridau.parking.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import si.afridau.parking.dto.ParkingSessionDto;
import si.afridau.parking.model.ParkingSession;


@Mapper(componentModel = "spring", uses = ParkingSessionMapper.class)
public interface ParkingSessionMapper {
    @Mapping(source = "licensePlate", target = "licensePlate")
    ParkingSession toEntity(ParkingSessionDto dto);

    ParkingSessionDto toDto(ParkingSession session);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget ParkingSession session, ParkingSessionDto dto);
}
