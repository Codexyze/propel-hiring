package com.scrymz.propel_hiring.data.mapper.kitchen

import com.scrymz.propel_hiring.data.remote.kitchen.DayMenuDto
import com.scrymz.propel_hiring.data.remote.kitchen.KitchenDto
import com.scrymz.propel_hiring.domain.model.kitchen.DayMenu
import com.scrymz.propel_hiring.domain.model.kitchen.Kitchen

/**
 * Maps [KitchenDto] to the domain [Kitchen] model.
 */
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

/**
 * Maps [DayMenuDto] to the domain [DayMenu] model.
 */
fun DayMenuDto.toDomain(): DayMenu {
    return DayMenu(
        day = day,
        mealType = mealType,
        items = items
    )
}
