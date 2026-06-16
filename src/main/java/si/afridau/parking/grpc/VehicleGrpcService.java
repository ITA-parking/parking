package si.afridau.parking.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import si.afridau.parking.grpc.proto.*;
import si.afridau.parking.model.Vehicle;
import si.afridau.parking.repository.VehicleRepo;

import java.util.List;
import java.util.UUID;

@GrpcService
public class VehicleGrpcService extends VehicleServiceGrpc.VehicleServiceImplBase {

    private final VehicleRepo vehicleRepo;

    public VehicleGrpcService(VehicleRepo vehicleRepo) {
        this.vehicleRepo = vehicleRepo;
    }

    @Override
    public void getAll(Empty request, StreamObserver<VehicleListResponse> responseObserver) {
        List<VehicleProto> protos = vehicleRepo.findAll().stream()
                .map(this::toProto)
                .toList();
        responseObserver.onNext(VehicleListResponse.newBuilder().addAllVehicles(protos).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getById(GetVehicleRequest request, StreamObserver<VehicleProto> responseObserver) {
        vehicleRepo.findById(UUID.fromString(request.getId()))
                .ifPresentOrElse(
                        v -> {
                            responseObserver.onNext(toProto(v));
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(Status.NOT_FOUND
                                .withDescription("Vehicle not found: " + request.getId())
                                .asRuntimeException())
                );
    }

    @Override
    public void create(AddVehicleRequest request, StreamObserver<VehicleProto> responseObserver) {
        Vehicle vehicle = Vehicle.builder()
                .name(request.getName())
                .licensePlate(request.getLicensePlate())
                .build();
        Vehicle saved = vehicleRepo.save(vehicle);
        responseObserver.onNext(toProto(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void update(UpdateVehicleRequest request, StreamObserver<VehicleProto> responseObserver) {
        vehicleRepo.findById(UUID.fromString(request.getId()))
                .ifPresentOrElse(
                        v -> {
                            v.setName(request.getName());
                            v.setLicensePlate(request.getLicensePlate());
                            Vehicle saved = vehicleRepo.save(v);
                            responseObserver.onNext(toProto(saved));
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(Status.NOT_FOUND
                                .withDescription("Vehicle not found: " + request.getId())
                                .asRuntimeException())
                );
    }

    @Override
    public void delete(DeleteVehicleRequest request, StreamObserver<Empty> responseObserver) {
        UUID id = UUID.fromString(request.getId());
        if (!vehicleRepo.existsById(id)) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Vehicle not found: " + request.getId())
                    .asRuntimeException());
            return;
        }
        vehicleRepo.deleteById(id);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private VehicleProto toProto(Vehicle v) {
        return VehicleProto.newBuilder()
                .setId(v.getId().toString())
                .setName(v.getName() != null ? v.getName() : "")
                .setLicensePlate(v.getLicensePlate() != null ? v.getLicensePlate() : "")
                .build();
    }
}
