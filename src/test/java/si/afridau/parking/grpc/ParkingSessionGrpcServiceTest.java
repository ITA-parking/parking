package si.afridau.parking.grpc;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.afridau.parking.grpc.proto.*;
import si.afridau.parking.model.ParkingSession;
import si.afridau.parking.model.Vehicle;
import si.afridau.parking.repository.ParkingSessionRepo;
import si.afridau.parking.repository.VehicleRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSessionGrpcServiceTest {

    @Mock
    ParkingSessionRepo parkingSessionRepo;
    @Mock
    VehicleRepo vehicleRepo;

    ParkingSessionGrpcService service;

    @BeforeEach
    void setUp() {
        service = new ParkingSessionGrpcService(parkingSessionRepo, vehicleRepo);
    }

    @Test
    void getAll_returnsSessions() {
        Vehicle vehicle = buildVehicle(UUID.randomUUID(), "Car A", "AB-123");
        ParkingSession session = buildSession(UUID.randomUUID(), vehicle);
        when(parkingSessionRepo.findAll()).thenReturn(List.of(session));

        @SuppressWarnings("unchecked")
        StreamObserver<ParkingSessionListResponse> observer = mock(StreamObserver.class);
        service.getAll(Empty.getDefaultInstance(), observer);

        ArgumentCaptor<ParkingSessionListResponse> captor = ArgumentCaptor.forClass(ParkingSessionListResponse.class);
        verify(observer).onNext(captor.capture());
        verify(observer).onCompleted();
        assertThat(captor.getValue().getSessionsList()).hasSize(1);
    }

    @Test
    void getById_notFound_sendsError() {
        UUID id = UUID.randomUUID();
        when(parkingSessionRepo.findById(id)).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked")
        StreamObserver<ParkingSessionProto> observer = mock(StreamObserver.class);
        service.getById(GetParkingSessionRequest.newBuilder().setId(id.toString()).build(), observer);

        verify(observer).onError(any(StatusRuntimeException.class));
        verify(observer, never()).onCompleted();
    }

    @Test
    void create_vehicleNotFound_sendsError() {
        UUID vehicleId = UUID.randomUUID();
        when(vehicleRepo.findById(vehicleId)).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked")
        StreamObserver<ParkingSessionProto> observer = mock(StreamObserver.class);

        try {
            service.create(
                    AddParkingSessionRequest.newBuilder()
                            .setVehicleId(vehicleId.toString())
                            .setFromTime("2024-01-01T10:00:00")
                            .setToTime("2024-01-01T12:00:00")
                            .setLicensePlate("AB-123")
                            .build(),
                    observer
            );
        } catch (StatusRuntimeException e) {
            // expected — service throws NOT_FOUND
        }

        verify(parkingSessionRepo, never()).save(any());
    }

    @Test
    void delete_existing_deletesAndCompletes() {
        UUID id = UUID.randomUUID();
        when(parkingSessionRepo.existsById(id)).thenReturn(true);

        @SuppressWarnings("unchecked")
        StreamObserver<Empty> observer = mock(StreamObserver.class);
        service.delete(DeleteParkingSessionRequest.newBuilder().setId(id.toString()).build(), observer);

        verify(parkingSessionRepo).deleteById(id);
        verify(observer).onNext(Empty.getDefaultInstance());
        verify(observer).onCompleted();
    }

    @Test
    void delete_notFound_sendsError() {
        UUID id = UUID.randomUUID();
        when(parkingSessionRepo.existsById(id)).thenReturn(false);

        @SuppressWarnings("unchecked")
        StreamObserver<Empty> observer = mock(StreamObserver.class);
        service.delete(DeleteParkingSessionRequest.newBuilder().setId(id.toString()).build(), observer);

        verify(observer).onError(any(StatusRuntimeException.class));
        verify(parkingSessionRepo, never()).deleteById(any());
    }

    private Vehicle buildVehicle(UUID id, String name, String plate) {
        Vehicle v = Vehicle.builder().name(name).licensePlate(plate).build();
        setField(v, "id", id);
        return v;
    }

    private ParkingSession buildSession(UUID id, Vehicle vehicle) {
        ParkingSession s = ParkingSession.builder()
                .from(LocalDateTime.now())
                .to(LocalDateTime.now().plusHours(1))
                .licensePlate(vehicle.getLicensePlate())
                .vehicle(vehicle)
                .build();
        setField(s, "id", id);
        return s;
    }

    private void setField(Object obj, String fieldName, Object value) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                var field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }
}
