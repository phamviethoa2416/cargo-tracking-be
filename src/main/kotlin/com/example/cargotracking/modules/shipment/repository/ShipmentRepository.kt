package com.example.cargotracking.modules.shipment.repository

import com.example.cargotracking.modules.shipment.model.entity.Shipment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ShipmentRepository : JpaRepository<Shipment, UUID>, JpaSpecificationExecutor<Shipment>