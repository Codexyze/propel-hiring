package com.scrymz.propel_hiring.data.remote.kitchen

import kotlinx.serialization.Serializable

@Serializable
data class KitchenDto(
    val id: String,
    val name: String,
    val cuisine: String,
    val pricePerTiffin: Double,
    val isVeg: Boolean,
    val rating: Double,
    val imageUrl: String = "",
    val description: String = "",
    val address: String = "",
    val weeklyMenu: List<DayMenuDto> = emptyList()
)

@Serializable
data class DayMenuDto(
    val day: String,
    val mealType: String = "Lunch & Dinner",
    val items: List<String>
)
