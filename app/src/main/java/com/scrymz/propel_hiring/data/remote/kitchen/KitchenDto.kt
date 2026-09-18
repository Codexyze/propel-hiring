package com.scrymz.propel_hiring.data.remote.kitchen

import com.scrymz.propel_hiring.domain.model.kitchen.DayMenu
import com.scrymz.propel_hiring.domain.model.kitchen.Kitchen
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

fun KitchenDto.toDomain(): Kitchen {
    return Kitchen(
        id = id,
        name = name,
        cuisine = cuisine,
        pricePerTiffin = pricePerTiffin,
        isVeg = isVeg,
        rating = rating,
        imageUrl = imageUrl,
        description = description,
        address = address,
        weeklyMenu = weeklyMenu.map { it.toDomain() }
    )
}

fun DayMenuDto.toDomain(): DayMenu {
    return DayMenu(
        day = day,
        mealType = mealType,
        items = items
    )
}
