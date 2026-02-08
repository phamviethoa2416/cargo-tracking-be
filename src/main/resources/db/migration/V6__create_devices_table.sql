CREATE TABLE devices (
    id UUID PRIMARY KEY,
    hardware_uid VARCHAR(255) NOT NULL UNIQUE,
    provider_id UUID NOT NULL REFERENCES users(id),
    device_name VARCHAR(100),
    model VARCHAR(50),
    current_shipment_id UUID REFERENCES shipments(id),
    battery_level INTEGER,
    status VARCHAR(50) NOT NULL,
    firmware_version VARCHAR(50),
    total_trips INTEGER NOT NULL DEFAULT 0,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_device_hardware_uid ON devices(hardware_uid);
CREATE INDEX idx_device_provider_id ON devices(provider_id);
CREATE INDEX idx_device_status ON devices(status);

ALTER TABLE shipments ADD CONSTRAINT fk_shipment_device FOREIGN KEY (device_id) REFERENCES devices(id);
