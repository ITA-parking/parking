package si.afridau.parking.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import si.afridau.parking.grpc.proto.*;
import si.afridau.parking.model.ParkingSession;
import si.afridau.parking.model.Vehicle;
import si.afridau.parking.repository.ParkingSessionRepo;
import si.afridau.parking.repository.VehicleRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@GrpcService
public class ParkingSessionGrpcService extends ParkingSessionServiceGrpc.ParkingSessionServiceImplBase {

    private final ParkingSessionRepo parkingSessionRepo;
    private final VehicleRepo vehicleRepo;

    public ParkingSessionGrpcService(ParkingSessionRepo parkingSessionRepo, VehicleRepo vehicleRepo) {
        this.parkingSessionRepo = parkingSessionRepo;
        this.vehicleRepo = vehicleRepo;
    }

    @Override
    public void getAll(Empty request, StreamObserver<ParkingSessionListResponse> responseObserver) {
        List<ParkingSessionProto> protos = parkingSessionRepo.findAll().stream()
                .map(this::toProto)
                .toList();
        responseObserver.onNext(ParkingSessionListResponse.newBuilder().addAllSessions(protos).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getById(GetParkingSessionRequest request, StreamObserver<ParkingSessionProto> responseObserver) {
        parkingSessionRepo.findById(UUID.fromString(request.getId()))
                .ifPresentOrElse(
                        s -> {
                            responseObserver.onNext(toProto(s));
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(Status.NOT_FOUND
                                .withDescription("ParkingSession not found: " + request.getId())
                                .asRuntimeException())
                );
    }

    @Override
    public void create(AddParkingSessionRequest request, StreamObserver<ParkingSessionProto> responseObserver) {
        Vehicle vehicle = vehicleRepo.findById(UUID.fromString(request.getVehicleId()))
                .orElseThrow(() -> Status.NOT_FOUND
                        .withDescription("Vehicle not found: " + request.getVehicleId())
                        .asRuntimeException());

        ParkingSession session = ParkingSession.builder()
                .from(parseDateTime(request.getFromTime()))
                .to(parseDateTime(request.getToTime()))
                .licensePlate(request.getLicensePlate())
                .parkingRegionId(request.getParkingRegionId().isBlank() ? null : UUID.fromString(request.getParkingRegionId()))
                .vehicle(vehicle)
                .build();

        ParkingSession saved = parkingSessionRepo.save(session);
        responseObserver.onNext(toProto(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void update(UpdateParkingSessionRequest request, StreamObserver<ParkingSessionProto> responseObserver) {
        parkingSessionRepo.findById(UUID.fromString(request.getId()))
                .ifPresentOrElse(
                        s -> {
                            if (!request.getVehicleId().isBlank()) {
                                Vehicle vehicle = vehicleRepo.findById(UUID.fromString(request.getVehicleId()))
                                        .orElseThrow(() -> Status.NOT_FOUND
                                                .withDescription("Vehicle not found: " + request.getVehicleId())
                                                .asRuntimeException());
                                s.setVehicle(vehicle);
                            }
                            if (!request.getFromTime().isBlank()) s.setFrom(parseDateTime(request.getFromTime()));
                            if (!request.getToTime().isBlank()) s.setTo(parseDateTime(request.getToTime()));
                            if (!request.getLicensePlate().isBlank()) s.setLicensePlate(request.getLicensePlate());
                            if (!request.getParkingRegionId().isBlank())
                                s.setParkingRegionId(UUID.fromString(request.getParkingRegionId()));

                            ParkingSession saved = parkingSessionRepo.save(s);
                            responseObserver.onNext(toProto(saved));
                            responseObserver.onCompleted();
                        },
                        () -> responseObserver.onError(Status.NOT_FOUND
                                .withDescription("ParkingSession not found: " + request.getId())
                                .asRuntimeException())
                );
    }

    @Override
    public void delete(DeleteParkingSessionRequest request, StreamObserver<Empty> responseObserver) {
        UUID id = UUID.fromString(request.getId());
        if (!parkingSessionRepo.existsById(id)) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("ParkingSession not found: " + request.getId())
                    .asRuntimeException());
            return;
        }
        parkingSessionRepo.deleteById(id);
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private ParkingSessionProto toProto(ParkingSession s) {
        ParkingSessionProto.Builder b = ParkingSessionProto.newBuilder()
                .setId(s.getId().toString())
                .setLicensePlate(s.getLicensePlate() != null ? s.getLicensePlate() : "")
                .setParkingRegionId(s.getParkingRegionId() != null ? s.getParkingRegionId().toString() : "")
                .setFromTime(s.getFrom() != null ? s.getFrom().toString() : "")
                .setToTime(s.getTo() != null ? s.getTo().toString() : "");

        if (s.getVehicle() != null) {
            Vehicle v = s.getVehicle();
            b.setVehicle(VehicleProto.newBuilder()
                    .setId(v.getId().toString())
                    .setName(v.getName() != null ? v.getName() : "")
                    .setLicensePlate(v.getLicensePlate() != null ? v.getLicensePlate() : "")
                    .build());
        }
        return b.build();
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDateTime.parse(value);
    }
}
