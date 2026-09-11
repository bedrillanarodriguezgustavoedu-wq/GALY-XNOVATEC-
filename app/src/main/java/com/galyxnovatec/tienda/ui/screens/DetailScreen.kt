package com.galyxnovatec.tienda.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.FavoriteManager
import com.galyxnovatec.tienda.data.model.Producto
import com.galyxnovatec.tienda.data.network.Comment
import com.galyxnovatec.tienda.data.network.RetrofitClient
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.components.TarjetaProducto
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    producto: Producto,
    alVolver: () -> Unit,
    alAgregarAlCarrito: (Producto, Int) -> Unit,
    alComprarAhora: (Producto, Int) -> Unit,
    alSeleccionarProductoRelacionado: (Producto) -> Unit
) {
    val context = LocalContext.current
    var cantidad by remember { mutableIntStateOf(1) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var nuevoComentario by remember { mutableStateOf("") }
    var calificacionNueva by remember { mutableIntStateOf(5) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    // Estados para edición (Moderación)
    var comentarioAEditar by remember { mutableStateOf<Comment?>(null) }
    var textoEdicion by remember { mutableStateOf("") }
    var isUpdatingComment by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val esFavorito = FavoriteManager.esFavorito(producto)
    val snackbarHostState = remember { SnackbarHostState() }

    // Animación de pulso para el botón de compra
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    LaunchedEffect(producto.id) {
        try {
            comments = RetrofitClient.instance.getComments(producto.id)
        } catch (e: Exception) {
            Log.e("GALY", "Error cargando comentarios: ${e.message}")
        }
    }

    val productosRelacionados = remember(producto) {
        ProductRepository.obtenerTodosLosProductos()
            .filter { it.categoria == producto.categoria && it.id != producto.id }
            .take(6)
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color.White,
                    actionColor = ColorGalyRed,
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp)
                )
            } 
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Precio Final", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(
                            "S/ ${(producto.precioOferta * cantidad).toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    
                    IconButton(
                        onClick = { alAgregarAlCarrito(producto, cantidad) },
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
                    ) {
                        Icon(Icons.Default.AddShoppingCart, null, tint = Color.White)
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Button(
                        onClick = { alComprarAhora(producto, cantidad) },
                        modifier = Modifier
                            .height(54.dp)
                            .weight(1.5f)
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed)
                    ) {
                        Text("COMPRAR AHORA", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
        ) {
            // Galería de Imágenes Pager Premium (Restaurada y Mejorada)
            val imageList = producto.getAllImageUrls()
            val pagerState = rememberPagerState(pageCount = { imageList.size })

            Box(modifier = Modifier.fillMaxWidth().height(380.dp).background(Color(0xFF0A0A0A))) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = imageList[page],
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentScale = ContentScale.Fit,
                        error = painterResource(R.drawable.logo_2),
                        placeholder = painterResource(R.drawable.logo_2)
                    )
                }
                
                // Indicador de Puntos (Solo si hay más de una imagen)
                if (imageList.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(imageList.size) { iteration ->
                            val color = if (pagerState.currentPage == iteration) ColorGalyRed else Color.White.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .padding(3.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }

                // Acciones Superiores
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CircleActionIcon(Icons.AutoMirrored.Filled.ArrowBack, alVolver)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircleActionIcon(Icons.Outlined.Share, {
                            val shareMessage = """
                                🚀 ¡Mira esta joya en GALY XNOVATEC!
                                
                                🔥 *${producto.nombre}*
                                🏷️ Marca: ${producto.marca}
                                💰 Precio: S/ ${producto.precioOferta.toInt()}
                                
                                Compra aquí: http://galyxnovatec.com/p/${producto.id}
                            """.trimIndent()
                            
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Compartir con un amigo"))
                        })
                        CircleActionIcon(
                            if (esFavorito) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            { FavoriteManager.toggleFavorito(producto) },
                            if (esFavorito) ColorGalyRed else Color.White
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                // Cabecera con Nombre y Marca
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        producto.marca.uppercase(),
                        color = ColorGalyRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).background(Color(0xFF00FF41), CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text("STOCK DISPONIBLE", color = Color(0xFF00FF41), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Text(
                    text = producto.nombre,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 34.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Rating y Reseñas
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                    Text(" 4.9", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(" (${producto.numRevisiones} reseñas)", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Selector de Cantidad Premium
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111111), RoundedCornerShape(20.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cantidad", modifier = Modifier.padding(start = 16.dp).weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (cantidad > 1) cantidad-- }, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                            Icon(Icons.Default.Remove, null, tint = Color.White)
                        }
                        Text(cantidad.toString(), color = Color.White, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 20.dp))
                        IconButton(onClick = { cantidad++ }, modifier = Modifier.background(ColorGalyRed.copy(alpha = 0.1f), CircleShape)) {
                            Icon(Icons.Default.Add, null, tint = ColorGalyRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Descripción expandible o directa
                Text("Descripción del Producto", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                Text(
                    text = producto.descripcion ?: "Calidad garantizada por GALY XNOVATEC.",
                    color = Color.Gray,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Especificaciones Técnicas
                val specs = producto.getEspecificacionesList()
                if (specs.isNotEmpty()) {
                    Text("Ficha Técnica", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                    Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        specs.forEach { spec ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = ColorGalyRed, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(spec, color = Color.LightGray, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Productos Relacionados
                Text("También te puede interesar", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                LazyRow(
                    modifier = Modifier.padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(productosRelacionados) { rel ->
                        Box(modifier = Modifier.width(160.dp)) {
                            TarjetaProducto(producto = rel, alHacerClick = { alSeleccionarProductoRelacionado(it) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // SECCIÓN DE COMENTARIOS
                Text("Reseñas de Clientes", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                
                // Formulario para añadir comentario (solo si hay usuario)
                val currentUser = AuthManager.currentUser
                if (currentUser != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF111111),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Escribe tu experiencia", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            
                            // Calificación con estrellas
                            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                                repeat(5) { index ->
                                    val active = (index + 1) <= calificacionNueva
                                    IconButton(
                                        onClick = { calificacionNueva = index + 1 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            if (active) Icons.Default.Star else Icons.Default.StarBorder,
                                            null,
                                            tint = if (active) Color(0xFFFFB400) else Color.Gray
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = nuevoComentario,
                                onValueChange = { nuevoComentario = it },
                                placeholder = { Text("¿Qué te pareció el producto?", fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ColorGalyRed,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (nuevoComentario.isNotBlank()) {
                                        scope.launch {
                                            isSubmitting = true
                                            try {
                                                val data = mapOf(
                                                    "usuario_id" to currentUser.id,
                                                    "producto_id" to producto.id,
                                                    "comentario" to nuevoComentario,
                                                    "estrellas" to calificacionNueva
                                                )
                                                val res = RetrofitClient.instance.addComment(data)
                                                if (res["success"] == true) {
                                                    nuevoComentario = ""
                                                    calificacionNueva = 5
                                                    // Refrescar lista
                                                    comments = RetrofitClient.instance.getComments(producto.id)
                                                    Toast.makeText(context, "✅ Reseña publicada", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "❌ Error al publicar", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isSubmitting = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isSubmitting && nuevoComentario.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("PUBLICAR RESEÑA", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Inicia sesión para dejar una reseña.", color = Color.Gray, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Lista de comentarios existentes
                if (comments.isEmpty()) {
                    Text("Aún no hay comentarios. ¡Sé el primero!", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp))
                } else {
                    comments.forEach { comment ->
                        CommentCard(
                            comment = comment,
                            onDelete = if (currentUser?.esAdmin == true) {
                                {
                                    scope.launch {
                                        try {
                                            val res = RetrofitClient.instance.deleteComment(mapOf("id" to comment.id))
                                            if (res["success"] == true) {
                                                Toast.makeText(context, "Reseña eliminada", Toast.LENGTH_SHORT).show()
                                                comments = RetrofitClient.instance.getComments(producto.id)
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else null,
                            onEdit = if (currentUser?.esAdmin == true) {
                                {
                                    comentarioAEditar = comment
                                    textoEdicion = comment.text
                                }
                            } else null
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Diálogo de Moderación de Comentarios (Edición)
    if (comentarioAEditar != null) {
        AlertDialog(
            onDismissRequest = { if (!isUpdatingComment) comentarioAEditar = null },
            title = { Text("Moderar Comentario", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("Editando comentario de ${comentarioAEditar?.userName}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = textoEdicion,
                        onValueChange = { textoEdicion = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isUpdatingComment = true
                            try {
                                val res = RetrofitClient.instance.updateComment(
                                    mapOf("id" to comentarioAEditar!!.id, "comentario" to textoEdicion)
                                )
                                if (res["success"] == true) {
                                    Toast.makeText(context, "Comentario actualizado", Toast.LENGTH_SHORT).show()
                                    comments = RetrofitClient.instance.getComments(producto.id)
                                    comentarioAEditar = null
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                            } finally {
                                isUpdatingComment = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                    enabled = !isUpdatingComment && textoEdicion.isNotBlank()
                ) {
                    if (isUpdatingComment) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("GUARDAR CAMBIOS")
                }
            },
            dismissButton = {
                TextButton(onClick = { comentarioAEditar = null }, enabled = !isUpdatingComment) {
                    Text("CANCELAR", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun CommentCard(comment: Comment, onDelete: (() -> Unit)? = null, onEdit: (() -> Unit)? = null) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0A0A0A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.userName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (onEdit != null || onDelete != null) {
                        Spacer(Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onEdit != null) {
                                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(4.dp))
                            }
                            if (onDelete != null) {
                                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = ColorGalyRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                Row {
                    repeat(5) { i ->
                        Icon(
                            Icons.Default.Star,
                            null,
                            modifier = Modifier.size(12.dp),
                            tint = if (i < comment.stars) Color(0xFFFFB400) else Color.Gray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
            Text(
                text = comment.text,
                color = Color.LightGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = comment.date,
                color = Color.DarkGray,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun CircleActionIcon(icon: ImageVector, onClick: () -> Unit, tint: Color = Color.White) {
    Surface(
        modifier = Modifier.size(46.dp).clickable { onClick() },
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}
