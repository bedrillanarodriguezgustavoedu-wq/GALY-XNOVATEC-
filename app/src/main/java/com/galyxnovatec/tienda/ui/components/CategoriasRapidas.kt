package com.galyxnovatec.tienda.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun CategoriasRapidas(seleccionada: String?, alSeleccionar: (String) -> Unit) {
    val categorias by remember { derivedStateOf { ProductRepository.obtenerCategorias() } }
    
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(categorias) { cat ->
                val es = seleccionada?.equals(cat, ignoreCase = true) == true
                val icon = when {
                    cat.contains("Laptop", true) -> Icons.Default.Laptop
                    cat.contains("Mouse", true) -> Icons.Default.Mouse
                    cat.contains("Audifono", true) || cat.contains("Audífono", true) -> Icons.Default.Headset
                    cat.contains("Teclado", true) -> Icons.Default.Keyboard
                    cat.contains("Monitor", true) -> Icons.Default.DesktopWindows
                    cat.contains("Smartphone", true) -> Icons.Default.Smartphone
                    cat.contains("Gaming", true) -> Icons.Default.Gamepad
                    else -> Icons.Default.Category
                }
                
                CategoryCircleItem(
                    name = cat,
                    icon = icon,
                    isSelected = es,
                    onClick = { alSeleccionar(cat) }
                )
            }
        }
    }
}

@Composable
fun CategoryCircleItem(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.width(70.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isSelected) ColorGalyRed else Color.White,
            modifier = Modifier.size(60.dp),
            shadowElevation = if (isSelected) 8.dp else 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = if (isSelected) Color.White else Color(0xFF333333),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isSelected) ColorGalyRed else Color.White,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}
