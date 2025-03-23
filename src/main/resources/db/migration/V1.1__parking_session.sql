CREATE TABLE parking_session
(
    id            UUID PRIMARY KEY,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    from_time     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    to_time       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    license_plate VARCHAR(255)                NOT NULL,
    vehicle_id    UUID                        NOT NULL,
    CONSTRAINT fk_parking_session_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id)
);