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
import si.afridau.parking.model.Vehicle;
import si.afridau.parking.repository.VehicleRepo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleGrpcServiceTest {

    @Mock
    VehicleRepo vehicleRepo;

    VehicleGrpcService service;

    @BeforeEach
    void setUp() {
        service = new VehicleGrpcService(vehicleRepo);
    }

    @Test
    void getAll_returnsAllVehicles() {
        Vehicle v1 = Vehicle.builder().name("Car A").licensePlate("AB-123").build();
        Vehicle v2 = Vehicle.builder().name("Car B").licensePlate("CD-456").build();
        setId(v1, UUID.randomUUID());
        setId(v2, UUID.randomUUID());
        when(vehicleRepo.findAll()).thenReturn(List.of(v1, v2));

        @SuppressWarnings("unchecked")
        StreamObserver<VehicleListResponse> observer = mock(StreamObserver.class);
        service.getAll(Empty.getDefaultInstance(), observer);

        ArgumentCaptor<VehicleListResponse> captor = ArgumentCaptor.forClass(VehicleListResponse.class);
        verify(observer).onNext(captor.capture());
        verify(observer).onCompleted();
        assertThat(captor.getValue().getVehiclesList()).hasSize(2);
    }

    @Test
    void getById_found() {
        UUID id = UUID.randomUUID();
        Vehicle v = Vehicle.builder().name("Car A").licensePlate("AB-123").build();
        setId(v, id);
        when(vehicleRepo.findById(id)).thenReturn(Optional.of(v));

        @SuppressWarnings("unchecked")
        StreamObserver<VehicleProto> observer = mock(StreamObserver.class);
        service.getById(GetVehicleRequest.newBuilder().setId(id.toString()).build(), observer);

        ArgumentCaptor<VehicleProto> captor = ArgumentCaptor.forClass(VehicleProto.class);
        verify(observer).onNext(captor.capture());
        verify(observer).onCompleted();
        assertThat(captor.getValue().getName()).isEqualTo("Car A");
        assertThat(captor.getValue().getLicensePlate()).isEqualTo("AB-123");
    }

    @Test
    void getById_notFound() {
        UUID id = UUID.randomUUID();
        when(vehicleRepo.findById(id)).thenReturn(Optional.empty());

        @SuppressWarnings("unchecked")
        StreamObserver<VehicleProto> observer = mock(StreamObserver.class);
        service.getById(GetVehicleRequest.newBuilder().setId(id.toString()).build(), observer);

        verify(observer).onError(any(StatusRuntimeException.class));
        verify(observer, never()).onCompleted();
    }

    @Test
    void create_savesAndReturnsVehicle() {
        UUID savedId = UUID.randomUUID();
        Vehicle saved = Vehicle.builder().name("New Car").licensePlate("XY-999").build();
        setId(saved, savedId);
        when(vehicleRepo.save(any(Vehicle.class))).thenReturn(saved);

        @SuppressWarnings("unchecked")
        StreamObserver<VehicleProto> observer = mock(StreamObserver.class);
        service.create(
                AddVehicleRequest.newBuilder().setName("New Car").setLicensePlate("XY-999").build(),
                observer
        );

        ArgumentCaptor<VehicleProto> captor = ArgumentCaptor.forClass(VehicleProto.class);
        verify(observer).onNext(captor.capture());
        verify(observer).onCompleted();
        assertThat(captor.getValue().getName()).isEqualTo("New Car");
    }

    @Test
    void delete_notFound_sendsError() {
        UUID id = UUID.randomUUID();
        when(vehicleRepo.existsById(id)).thenReturn(false);

        @SuppressWarnings("unchecked")
        StreamObserver<Empty> observer = mock(StreamObserver.class);
        service.delete(DeleteVehicleRequest.newBuilder().setId(id.toString()).build(), observer);

        verify(observer).onError(any(StatusRuntimeException.class));
        verify(vehicleRepo, never()).deleteById(any());
    }

    @Test
    void delete_existing_deletesSuccessfully() {
        UUID id = UUID.randomUUID();
        when(vehicleRepo.existsById(id)).thenReturn(true);

        @SuppressWarnings("unchecked")
        StreamObserver<Empty> observer = mock(StreamObserver.class);
        service.delete(DeleteVehicleRequest.newBuilder().setId(id.toString()).build(), observer);

        verify(vehicleRepo).deleteById(id);
        verify(observer).onNext(Empty.getDefaultInstance());
        verify(observer).onCompleted();
    }

    private void setId(Vehicle vehicle, UUID id) {
        try {
            var field = vehicle.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(vehicle, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
