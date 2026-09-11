package com.galyxnovatec.tienda.ui.theme

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ==============================================
// Esquema de colores - Modo Oscuro
// ==============================================
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// ==============================================
// Esquema de colores - Modo Claro
// ==============================================
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Tema principal de la aplicación GALY.
 *
 * @param darkTheme Activar modo oscuro (sigue al sistema por defecto)
 * @param dynamicColor Activar colores dinámicos (Android 12+)
 * @param content Contenido a aplicar el tema
 */
@Composable
fun GALYTheme(
    darkTheme: Boolean = false, // 🔥 Forzamos Modo Claro siempre
    dynamicColor: Boolean = false, // 🔥 Desactivamos color dinámico para evitar colores raros del sistema
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // 🎨 Barra de estado combinando Negro para el Inicio
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.WHITE // 🔥 Barra blanca para que se vea limpio
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true // Texto oscuro en barra
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}