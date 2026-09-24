package com.sartori.brick.data.earthquake.network

import kotlinx.serialization.Serializable

@Serializable
data class EarthquakeResponseDto(
    val features: List<EarthquakeFeatureDto> = emptyList()
)

@Serializable
data class EarthquakeFeatureDto(
    val id: String,
    val properties: EarthquakePropertiesDto,
    val geometry: EarthquakeGeometryDto
)

@Serializable
data class EarthquakePropertiesDto(
    val mag: Double? = null,
    val place: String? = null,
    val time: Long? = null,
    val url: String? = null,
    val detail: String? = null,
    val alert: String? = null,
    val tsunami: Int = 0
)

@Serializable
data class EarthquakeGeometryDto(
    val coordinates: List<Double> = emptyList()
)
