package com.galyxnovatec.tienda.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.galyxnovatec.tienda.data.model.MarcaCompartida

@Composable
fun MarcaLogo(marca: MarcaCompartida, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    val logoModifier = modifier
        .clip(shape)
        .background(Color.White)
        .padding(4.dp)

    if (marca.getFullImageUrl() == null) {
        Box(
            modifier = logoModifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = marca.nombre.take(2).uppercase(),
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        return
    }

    SubcomposeAsyncImage(
        model = marca.getFullImageUrl(),
        contentDescription = "Logo ${marca.nombre}",
        modifier = logoModifier,
        contentScale = ContentScale.Fit,
        loading = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(marca.nombre.take(2).uppercase(), color = Color.DarkGray, fontWeight = FontWeight.Bold)
            }
        },
        error = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(marca.nombre.take(2).uppercase(), color = Color.DarkGray, fontWeight = FontWeight.Bold)
            }
        }
    )
}
