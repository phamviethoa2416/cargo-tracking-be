CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES users(id),
    provider_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    goods_description VARCHAR(1000) NOT NULL,
    pickup_address VARCHAR(500) NOT NULL,
    delivery_address VARCHAR(500) NOT NULL,
    estimated_delivery_at TIMESTAMP WITH TIME ZONE,
    require_temperature_tracking BOOLEAN NOT NULL DEFAULT false,
    min_temperature DOUBLE PRECISION,
    max_temperature DOUBLE PRECISION,
    require_humidity_tracking BOOLEAN NOT NULL DEFAULT false,
    min_humidity DOUBLE PRECISION,
    max_humidity DOUBLE PRECISION,
    require_location_tracking BOOLEAN NOT NULL DEFAULT true,
    special_requirements VARCHAR(1000),
    shipment_id UUID,
    rejection_reason VARCHAR(500),
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_order_customer_id ON orders(customer_id);
CREATE INDEX idx_order_provider_id ON orders(provider_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_shipment_id ON orders(shipment_id);
