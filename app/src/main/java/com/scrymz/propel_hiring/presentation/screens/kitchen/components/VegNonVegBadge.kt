package com.scrymz.propel_hiring.presentation.screens.kitchen.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VegNonVegBadge(
    isVeg: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isVeg) Color(0xFF388E3C) else Color(0xFFD32F2F)
    val dotColor = if (isVeg) Color(0xFF388E3C) else Color(0xFFD32F2F)

    Surface(
        modifier = modifier
            .size(18.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(3.dp)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = dotColor
            ) {}
        }
    }
}
