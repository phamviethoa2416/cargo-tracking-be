CREATE TABLE shipments (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES users(id),
    provider_id UUID NOT NULL REFERENCES users(id),
    shipper_id UUID REFERENCES users(id),
    device_id UUID,
    status VARCHAR(50) NOT NULL,
    goods_description VARCHAR(1000) NOT NULL,
    pickup_address VARCHAR(500) NOT NULL,
    delivery_address VARCHAR(500) NOT NULL,
    estimated_delivery_at TIMESTAMP WITH TIME ZONE,
    actual_delivery_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_shipment_customer_id ON shipments(customer_id);
CREATE INDEX idx_shipment_provider_id ON shipments(provider_id);
CREATE INDEX idx_shipment_shipper_id ON shipments(shipper_id);
CREATE INDEX idx_shipment_device_id ON shipments(device_id);
CREATE INDEX idx_shipment_status ON shipments(status);

ALTER TABLE orders ADD CONSTRAINT fk_order_shipment FOREIGN KEY (shipment_id) REFERENCES shipments(id);