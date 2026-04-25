package com.hfad.digital_assistant.model.api

data class CurrentIndicatorDto(
    val id: Int,
    val name: String,
    val value: Double?,
    val comment: String?
)

data class IndicatorUpdateItemDto(
    val id: Int,
    val value: Int,
    val comment: String? = null
)

data class HistoryPeriodDto(
    val period: String,
    val indicators: List<HistoryIndicatorDto>
)

data class HistoryIndicatorDto(
    val id: Int,
    val name: String,
    val value: Double,
    val comment: String?
)