package com.scrymz.propel_hiring.domain.model.kitchen

/**
 * Domain model representing a home kitchen offering daily tiffins.
 */
data class Kitchen(
    val id: String,
    val name: String,
    val cuisine: String,
    val pricePerTiffin: Double,
    val isVeg: Boolean,
    val rating: Double,
    val imageUrl: String = "",
    val description: String = "",
    val address: String = "",
    val weeklyMenu: List<DayMenu> = emptyList()
)

/**
 * Domain model representing a daily menu offering for a kitchen.
 */
data class DayMenu(
    val day: String,
    val mealType: String,
    val items: List<String>
)
