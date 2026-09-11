package com.galyxnovatec.tienda.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.galyxnovatec.tienda.R
import com.galyxnovatec.tienda.data.manager.AuthManager
import com.galyxnovatec.tienda.data.manager.CartManager
import com.galyxnovatec.tienda.data.manager.FavoriteManager
import com.galyxnovatec.tienda.data.manager.SpeechManager
import com.galyxnovatec.tienda.data.model.Producto
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.components.*
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    alSeleccionarProducto: (Producto) -> Unit,
    alIrAlCarrito: () -> Unit,
    alIrAFavoritos: () -> Unit,
    alIrACategorias: () -> Unit,
    alIrAPerfil: () -> Unit,
    alIrAServicioTecnico: () -> Unit,
    alIrAChatAi: () -> Unit,
    modifier: Modifier = Modifier,
    categoriaInicial: String? = null,
) {
    var textoBusqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(categoriaInicial) }
    var marcaSeleccionada by remember { mutableStateOf<String?>(null) }
    var showMoreSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val speechManager = remember { SpeechManager(context) }
    var isListening by remember { mutableStateOf(false) }
    
    val listaProductosBase = ProductRepository.productos
    val listaBanners = ProductRepository.banners

    LaunchedEffect(Unit) {
        ProductRepository.cargarCache(context)
        ProductRepository.sincronizarTodo(context)
    }

    val productosAMostrar = remember(textoBusqueda, categoriaSeleccionada, marcaSeleccionada, listaProductosBase.size) {
        var base = listaProductosBase.toList()
        if (categoriaSeleccionada != null) {
            base = base.filter { it.categoria.equals(categoriaSeleccionada, ignoreCase = true) }
        }
        if (marcaSeleccionada != null) {
            base = base.filter { it.marca.equals(marcaSeleccionada, ignoreCase = true) }
        }
        if (textoBusqueda.isNotEmpty()) {
            base = base.filter { 
                it.nombre.contains(textoBusqueda, ignoreCase = true) || 
                it.marca.contains(textoBusqueda, ignoreCase = true) ||
                it.descripcion?.contains(textoBusqueda, ignoreCase = true) == true
            }
        }
        base
    }

    val totalItemsCarrito = CartManager.totalItems()
    val totalFavoritos = FavoriteManager.items.size

    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .statusBarsPadding()
            ) {
                // Header Premium Original
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_2),
                            contentDescription = "GALY Logo",
                            modifier = Modifier.size(36.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("GALY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, lineHeight = 22.sp)
                            Text("STORE", color = Color.White, fontWeight = FontWeight.Light, fontSize = 10.sp, letterSpacing = 3.sp, lineHeight = 10.sp)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgedBox(badge = {
                            if (totalFavoritos > 0) {
                                Badge(containerColor = ColorGalyRed) { Text(totalFavoritos.toString(), color = Color.White) }
                            }
                        }) {
                            IconButton(onClick = alIrAFavoritos) {
                                Icon(Icons.Outlined.FavoriteBorder, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        BadgedBox(badge = {
                            if (totalItemsCarrito > 0) {
                                Badge(containerColor = ColorGalyRed) { Text(totalItemsCarrito.toString(), color = Color.White) }
                            }
                        }) {
                            IconButton(onClick = alIrAlCarrito) {
                                Icon(Icons.Outlined.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                // Buscador Premium con Acceso Directo a AI
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF121212),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (textoBusqueda.isEmpty()) {
                                Text("¿Qué tecnología buscas hoy?", color = Color.Gray, fontSize = 14.sp)
                            }
                            BasicTextField(
                                value = textoBusqueda,
                                onValueChange = { textoBusqueda = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                                modifier = Modifier.fillMaxWidth(),
                                cursorBrush = SolidColor(ColorGalyRed),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                singleLine = true
                            )
                        }
                        // BOTÓN DE VOZ
                        IconButton(onClick = {
                            if (isListening) {
                                speechManager.stopListening()
                                isListening = false
                            } else {
                                isListening = true
                                speechManager.startListening(
                                    onResult = { 
                                        textoBusqueda = it
                                        isListening = false
                                    },
                                    onError = {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                        isListening = false
                                    }
                                )
                            }
                        }) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isListening) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "mic_ripple")
                                    val scale by infiniteTransition.animateFloat(
                                        initialValue = 1f,
                                        targetValue = 2f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "scale"
                                    )
                                    val alpha by infiniteTransition.animateFloat(
                                        initialValue = 0.5f,
                                        targetValue = 0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "alpha"
                                    )
                                    Surface(
                                        modifier = Modifier.size(24.dp).graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
                                        shape = CircleShape,
                                        color = ColorGalyRed
                                    ) {}
                                }
                                Icon(
                                    if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                    null,
                                    tint = if (isListening) ColorGalyRed else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        // BOTÓN DE GALY AI INTEGRADO
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { alIrAChatAi() },
                            color = ColorGalyRed.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoAwesome, null, tint = ColorGalyRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                
                // Filtro Marcas Original
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listOf("Apple", "SONY", "Logitech", "SAMSUNG", "Lenovo", "ASUS")) { marca ->
                        val selected = marcaSeleccionada == marca
                        Surface(
                            modifier = Modifier.clickable { marcaSeleccionada = if (selected) null else marca },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color.White else Color(0xFF1A1A1A),
                            border = BorderStroke(1.dp, if (selected) Color.White else Color(0xFF333333))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (marca == "Apple") Icon(Icons.Default.Laptop, null, tint = if(selected) Color.Black else Color.White, modifier = Modifier.size(14.dp))
                                if (marca == "Apple") Spacer(Modifier.width(6.dp))
                                Text(
                                    text = marca.uppercase(),
                                    color = if (selected) Color.Black else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.Black, tonalElevation = 0.dp) {
                val navItems = listOf(
                    Triple("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
                    Triple("Categorías", Icons.Filled.GridView, Icons.Outlined.GridView),
                    Triple("Mi Cuenta", Icons.Filled.Person, Icons.Outlined.Person),
                    Triple("Más", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
                )
                navItems.forEachIndexed { index, item ->
                    val isSel = index == 0
                    NavigationBarItem(
                        icon = { Icon(if (isSel) item.second else item.third, null) },
                        label = { Text(item.first, fontSize = 10.sp) },
                        selected = isSel,
                        onClick = {
                            when(index) {
                                1 -> alIrACategorias()
                                2 -> alIrAPerfil()
                                3 -> showMoreSheet = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ColorGalyRed,
                            selectedTextColor = ColorGalyRed,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        if (showMoreSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMoreSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF111111),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Explorar GALY", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
                    MoreOptionItem(icon = Icons.Outlined.LocalShipping, title = "Seguir mi pedido", onClick = { showMoreSheet = false })
                    MoreOptionItem(icon = Icons.AutoMirrored.Outlined.HelpCenter, title = "Atención al Cliente", onClick = { showMoreSheet = false; alIrAServicioTecnico() })
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Button(
                        onClick = { showMoreSheet = false; AuthManager.logout(); alIrAPerfil() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ColorGalyRed.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = ColorGalyRed)
                        Spacer(Modifier.width(12.dp))
                        Text("Cerrar Sesión", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().background(Color.Black)
        ) {
            item(span = { GridItemSpan(2) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    BannerPromocional(listaBanners)
                }
            }

            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Categorías", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(
                            "Ver todas >", 
                            color = ColorGalyRed, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 12.sp, 
                            modifier = Modifier.clickable { 
                                categoriaSeleccionada = null
                                marcaSeleccionada = null
                                textoBusqueda = ""
                            } 
                        )
                    }
                    CategoriasRapidas(categoriaSeleccionada) { cat ->
                        categoriaSeleccionada = if(categoriaSeleccionada == cat) null else cat
                    }
                    
                    if (categoriaSeleccionada != null || marcaSeleccionada != null || textoBusqueda.isNotEmpty()) {
                        Button(
                            onClick = { 
                                categoriaSeleccionada = null
                                marcaSeleccionada = null
                                textoBusqueda = ""
                            },
                            modifier = Modifier.padding(top = 12.dp).fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("MOSTRAR TODOS LOS PRODUCTOS (1-22)", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                FlashSaleSection(listaProductosBase.take(6)) { alSeleccionarProducto(it) }
            }

            // NOVEDADES
            item(span = { GridItemSpan(2) }) {
                val novedades = remember(listaProductosBase.size) { 
                    listaProductosBase.filter { it.etiquetaEspecial?.contains("NUEVO", true) == true }.take(10) 
                }
                if (novedades.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text("Lo más nuevo", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 20.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(novedades) { prod ->
                                Box(modifier = Modifier.width(180.dp)) {
                                    TarjetaProducto(prod, { alSeleccionarProducto(it) })
                                }
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recomendados para ti", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("${productosAMostrar.size} productos", color = Color.Gray, fontSize = 11.sp)
                }
            }

            items(items = productosAMostrar, key = { it.id }) { producto ->
                if (producto.id == -1) {
                    ShimmerProductCard()
                } else {
                    Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                        TarjetaProducto(
                            producto = producto, 
                            alHacerClick = { alSeleccionarProducto(it) },
                            alAgregarRapido = { CartManager.agregarProducto(it, 1) }
                        )
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                ServiceBannerSection()
            }

            // SOPORTE TÉCNICO CON IMAGEN (RESTAURADO)
            item(span = { GridItemSpan(2) }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 20.dp)
                        .clickable { alIrAServicioTecnico() },
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF111111)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img),
                            contentDescription = "Soporte",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.9f
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                        )
                        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                            Text("SERVICIO TÉCNICO", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp)
                            Text("Expertos en Laptops y Gaming", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ASISTENTE AI GALY
            item(span = { GridItemSpan(2) }) {
                AIAssistantBanner(onClick = alIrAChatAi)
            }

            item(span = { GridItemSpan(2) }) {
                FooterProfesional()
            }
        }
    }
}

@Composable
fun ServiceBannerSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(Color(0xFF111111), RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ServiceItemSmall(Icons.Default.LocalShipping, "Envío Gratis")
        ServiceItemSmall(Icons.Default.VerifiedUser, "Pago Seguro")
        ServiceItemSmall(Icons.Default.HeadsetMic, "Soporte 24/7")
    }
}

@Composable
fun ServiceItemSmall(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = ColorGalyRed, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(text, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AIAssistantBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(modifier = Modifier.size(54.dp), shape = CircleShape, color = ColorGalyRed.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, null, tint = ColorGalyRed, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("¿Necesitas ayuda experta?", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Text("Habla con la IA de GALY XNOVATEC", color = Color.Gray, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun FooterProfesional() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_2),
            contentDescription = null,
            modifier = Modifier.height(30.dp).alpha(0.2f),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(12.dp))
        Text("© 2026 GALY XNOVATEC S.A.C.", color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MoreOptionItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color(0xFF1A1A1A), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun FlashSaleSection(productos: List<Producto>, onProductClick: (Producto) -> Unit) {
    var tiempoRestante by remember { mutableLongStateOf(2 * 60 * 60 * 1000L + 45 * 60 * 1000L) }
    LaunchedEffect(Unit) {
        while (tiempoRestante > 0) {
            delay(1000)
            tiempoRestante -= 1000
        }
    }
    val h = (tiempoRestante / 3600000)
    val m = (tiempoRestante % 3600000) / 60000
    val s = (tiempoRestante % 60000) / 1000
    val timer = String.format("%02d:%02d:%02d", h, m, s)

    Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FlashOn, null, tint = Color.Yellow, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("OFERTAS FLASH", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Surface(color = ColorGalyRed, shape = RoundedCornerShape(6.dp)) {
                Text(timer, color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(productos) { prod ->
                FlashSaleCard(prod, onProductClick)
            }
        }
    }
}

@Composable
fun FlashSaleCard(producto: Producto, onClick: (Producto) -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).clickable { onClick(producto) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box {
                AsyncImage(
                    model = producto.getFullImageUrl(),
                    contentDescription = null,
                    modifier = Modifier.size(136.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                    error = painterResource(R.drawable.logo_2),
                    placeholder = painterResource(R.drawable.logo_2)
                )
                Surface(
                    color = ColorGalyRed,
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text("-${producto.descuento ?: 25}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(6.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(producto.nombre, color = Color.White, fontSize = 13.sp, maxLines = 1, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Text("S/ ${producto.precioOferta.toInt()}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { CartManager.agregarProducto(producto, 1) },
                    modifier = Modifier.size(30.dp).background(ColorGalyRed, CircleShape)
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
