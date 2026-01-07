package com.example.cargotracking.common.client

import com.example.cargotracking.common.client.dto.IngestionTelemetryResponse
import com.example.cargotracking.modules.device.model.dto.response.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.Instant
import java.util.UUID

@Component
class IngestionClient(
    @Value("\${ingestion.url:http://localhost:8080}") private val baseUrl: String,
    @Value("\${ingestion.timeout:5000}") private val timeout: Long
) {
    private val logger = LoggerFactory.getLogger(IngestionClient::class.java)
    
    private val restClient: RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()


    fun getLatestLocation(deviceId: UUID, authToken: String): LocationResponse? {
        return try {
            logger.debug("Fetching latest location for device: {}", deviceId)
            restClient.get()
                .uri("/api/v1/devices/{id}/location/latest", deviceId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
                .retrieve()
                .body(LocationResponse::class.java)
        } catch (e: RestClientException) {
            logger.error("Failed to fetch location for device {}: {}", deviceId, e.message)
            null
        }
    }

    fun getLocationHistory(
        deviceId: UUID,
        startTime: Instant?,
        endTime: Instant?,
        limit: Int?,
        offset: Int?,
        authToken: String
    ): LocationHistoryResponse? {
        return try {
            logger.debug("Fetching location history for device: {}", deviceId)
            
            val uriBuilder = StringBuilder("/api/v1/devices/$deviceId/location/history?")
            val params = mutableListOf<String>()
            
            startTime?.let { params.add("start_time=${it}") }
            endTime?.let { params.add("end_time=${it}") }
            limit?.let { params.add("limit=$it") }
            offset?.let { params.add("offset=$it") }
            
            val uri = uriBuilder.toString() + params.joinToString("&")
            
            restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
                .retrieve()
                .body(LocationHistoryResponse::class.java)
        } catch (e: RestClientException) {
            logger.error("Failed to fetch location history for device {}: {}", deviceId, e.message)
            null
        }
    }

    fun getDeviceEvents(
        deviceId: UUID,
        startTime: Instant?,
        endTime: Instant?,
        eventType: String?,
        limit: Int?,
        authToken: String
    ): DeviceEventListResponse? {
        return try {
            logger.debug("Fetching events for device: {}", deviceId)
            
            val uriBuilder = StringBuilder("/api/v1/devices/$deviceId/events?")
            val params = mutableListOf<String>()
            
            startTime?.let { params.add("start_time=${it}") }
            endTime?.let { params.add("end_time=${it}") }
            eventType?.let { params.add("event_type=$it") }
            limit?.let { params.add("limit=$it") }
            
            val uri = uriBuilder.toString() + params.joinToString("&")
            
            restClient.get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
                .retrieve()
                .body(DeviceEventListResponse::class.java)
        } catch (e: RestClientException) {
            logger.error("Failed to fetch events for device {}: {}", deviceId, e.message)
            null
        }
    }


    fun getLatestTelemetry(deviceId: UUID, authToken: String): TelemetryResponse? {
        return try {
            logger.info("[INGESTION_CLIENT] Fetching latest telemetry for device: {}", deviceId)
            logger.info("[INGESTION_CLIENT] Calling endpoint: /api/v1/devices/{}/telemetry/latest", deviceId)
            
            val ingestionResponse = restClient.get()
                .uri("/api/v1/devices/{id}/telemetry/latest", deviceId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
                .retrieve()
                .body(IngestionTelemetryResponse::class.java)
            
            if (ingestionResponse == null) {
                logger.warn("[INGESTION_CLIENT] Response body is null for device: {}", deviceId)
                return null
            }
            
            logger.info("[INGESTION_CLIENT] Received IngestionTelemetryResponse for device {}:", deviceId)
            logger.info("  - deviceId: {}", ingestionResponse.deviceId)
            logger.info("  - time: {}", ingestionResponse.time)
            logger.info("  - temperature: {} (null={})", ingestionResponse.temperature, ingestionResponse.temperature == null)
            logger.info("  - humidity: {} (null={})", ingestionResponse.humidity, ingestionResponse.humidity == null)
            logger.info("  - co2: {} (null={})", ingestionResponse.co2, ingestionResponse.co2 == null)
            logger.info("  - light: {} (null={})", ingestionResponse.light, ingestionResponse.light == null)
            logger.info("  - latitude: {} (null={})", ingestionResponse.latitude, ingestionResponse.latitude == null)
            logger.info("  - longitude: {} (null={})", ingestionResponse.longitude, ingestionResponse.longitude == null)
            logger.info("  - speed: {} (null={})", ingestionResponse.speed, ingestionResponse.speed == null)
            logger.info("  - accuracy: {} (null={})", ingestionResponse.accuracy, ingestionResponse.accuracy == null)
            logger.info("  - lean: {} (null={})", ingestionResponse.lean, ingestionResponse.lean == null)
            logger.info("  - batteryLevel: {} (null={})", ingestionResponse.batteryLevel, ingestionResponse.batteryLevel == null)
            logger.info("  - signalStrength: {} (null={})", ingestionResponse.signalStrength, ingestionResponse.signalStrength == null)
            logger.info("  - isMoving: {} (null={})", ingestionResponse.isMoving, ingestionResponse.isMoving == null)
            
            val response = TelemetryResponse(
                deviceId = ingestionResponse.deviceId,
                time = ingestionResponse.time,
                temperature = ingestionResponse.temperature,
                humidity = ingestionResponse.humidity,
                co2 = ingestionResponse.co2,
                light = ingestionResponse.light,
                latitude = ingestionResponse.latitude,
                longitude = ingestionResponse.longitude,
                speed = ingestionResponse.speed,
                accuracy = ingestionResponse.accuracy,
                lean = ingestionResponse.lean,
                batteryLevel = ingestionResponse.batteryLevel,
                signalStrength = ingestionResponse.signalStrength,
                isMoving = ingestionResponse.isMoving
            )
            
            logger.info("[INGESTION_CLIENT] Mapped to TelemetryResponse for device {}:", deviceId)
            logger.info("  - temperature: {} (null={})", response.temperature, response.temperature == null)
            logger.info("  - humidity: {} (null={})", response.humidity, response.humidity == null)
            logger.info("  - co2: {} (null={})", response.co2, response.co2 == null)
            logger.info("  - light: {} (null={})", response.light, response.light == null)
            logger.info("  - batteryLevel: {} (null={})", response.batteryLevel, response.batteryLevel == null)
            logger.info("  - signalStrength: {} (null={})", response.signalStrength, response.signalStrength == null)
            
            response
        } catch (e: RestClientException) {
            logger.error("[INGESTION_CLIENT] RestClientException for device {} - Response: {}",
                deviceId, e.message, e)
            null
        } catch (e: Exception) {
            logger.error("[INGESTION_CLIENT] Exception for device {}: {}", deviceId, e.message, e)
            null
        }
    }

    /**
     * Health check for ingestion service
     */
    fun isHealthy(): Boolean {
        return try {
            restClient.get()
                .uri("/health")
                .retrieve()
                .toBodilessEntity()
            true
        } catch (e: Exception) {
            logger.warn("Ingestion service health check failed: {}", e.message)
            false
        }
    }
}
