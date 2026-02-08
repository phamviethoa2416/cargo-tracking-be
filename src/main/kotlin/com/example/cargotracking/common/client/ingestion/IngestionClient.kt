package com.example.cargotracking.common.client.ingestion

import com.example.cargotracking.modules.device.model.dto.response.DeviceEventListResponse
import com.example.cargotracking.modules.device.model.dto.response.LocationHistoryResponse
import com.example.cargotracking.modules.device.model.dto.response.LocationResponse
import com.example.cargotracking.modules.device.model.dto.response.TelemetryResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body
import java.time.Instant
import java.util.*

@Component
class IngestionClient(
    @Value("\${ingestion.url:http://localhost:8080}") private val baseUrl: String
) {
    private val logger = LoggerFactory.getLogger(IngestionClient::class.java)
    
    private val restClient: RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()


    fun getLatestLocation(deviceId: UUID, authToken: String): LocationResponse? {
        return try {
            restClient.get()
                .uri("/api/v1/devices/{id}/location/latest", deviceId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
                .retrieve()
                .body<LocationResponse>()
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
                .body<LocationHistoryResponse>()
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
                .body<DeviceEventListResponse>()
        } catch (e: RestClientException) {
            logger.error("Failed to fetch events for device {}: {}", deviceId, e.message)
            null
        }
    }


    fun getLatestTelemetry(deviceId: UUID, authToken: String): TelemetryResponse? {
        return try {
            val ingestionResponse = restClient.get()
                .uri("/api/v1/devices/{id}/telemetry/latest", deviceId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $authToken")
                .retrieve()
                .body<IngestionResponse>()
                ?: return null

            TelemetryResponse(
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
        } catch (e: RestClientException) {
            logger.error("Failed to fetch telemetry for device {}: {}", deviceId, e.message, e)
            null
        } catch (e: Exception) {
            logger.error("Failed to fetch telemetry for device {}: {}", deviceId, e.message, e)
            null
        }
    }
}