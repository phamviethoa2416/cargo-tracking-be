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
            logger.debug("Fetching latest telemetry for device: {}", deviceId)
            
            val ingestionResponse = restClient.get()
                .uri("/api/v1/devices/{id}/telemetry/latest", deviceId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
                .retrieve()
                .body(IngestionTelemetryResponse::class.java)
            
            ingestionResponse?.let {
                TelemetryResponse(
                    deviceId = it.deviceId,
                    time = it.time,
                    temperature = it.temperature,
                    humidity = it.humidity,
                    co2 = it.co2,
                    light = it.light,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    speed = it.speed,
                    accuracy = it.accuracy,
                    lean = it.lean,
                    batteryLevel = it.batteryLevel,
                    signalStrength = it.signalStrength,
                    isMoving = it.isMoving
                )
            }
        } catch (e: RestClientException) {
            logger.error("Failed to fetch telemetry for device {}: {}", deviceId, e.message)
            null
        } catch (e: Exception) {
            logger.error("Failed to fetch telemetry for device {}: {}", deviceId, e.message)
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
