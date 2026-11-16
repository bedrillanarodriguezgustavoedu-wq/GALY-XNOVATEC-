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
import coil.request.ImageRequest
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
    alIrAPanelAdmin: () -> Unit,
    alIrAServicioTecnico: () -> Unit,
    alIrAChatAi: () -> Unit,
    modifier: Modifier = Modifier,
    categoriaInicial: String? = null,
) {
    var textoBusqueda by remember { mutableStateOf("") }
    val categoriaLimpiaInicial = remember(categoriaInicial) {
        if (categoriaInicial.isNullOrBlank() || categoriaInicial == "{category}" || categoriaInicial.startsWith("{")) null else categoriaInicial
    }
    var categoriaSeleccionada by remember(categoriaLimpiaInicial) { mutableStateOf(categoriaLimpiaInicial) }
    var marcaSeleccionada by remember { mutableStateOf<String?>(null) }
    var ordenSeleccionado by remember { mutableStateOf("DEFECTO") }
    var soloEnStock by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val speechManager = remember { SpeechManager(context) }
    var isListening by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val listaProductosBase = ProductRepository.productos
    val listaBanners = ProductRepository.banners

    LaunchedEffect(Unit) {
        ProductRepository.cargarCache(context)
        ProductRepository.sincronizarTodo(context)
    }

    val productosAMostrar by remember(
        listaProductosBase.size,
        categoriaSeleccionada,
        marcaSeleccionada,
        textoBusqueda,
        ordenSeleccionado,
        soloEnStock
    ) {
        derivedStateOf {
            ProductRepository.obtenerProductosFiltrados(
                busqueda = textoBusqueda,
                categoria = categoriaSeleccionada,
                soloEnStock = soloEnStock,
                orden = ordenSeleccionado
            )
        }
    }

    val totalItemsCarrito = remember(CartManager.items.size) { CartManager.totalItems() }
    val totalFavoritos = remember(FavoriteManager.items.size) { FavoriteManager.items.size }

    val cartScale = remember { Animatable(1f) }
    LaunchedEffect(totalItemsCarrito) {
        if (totalItemsCarrito > 0) {
            cartScale.animateTo(1.2f, animationSpec = tween(100))
            cartScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Scaffold(
        modifier = modifier,
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
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .statusBarsPadding()
            ) {
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
                        if (AuthManager.currentUser?.esAdmin == true) {
                            IconButton(onClick = alIrAPanelAdmin) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = "Abrir panel de administrador GALY",
                                    tint = ColorGalyRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
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
                            IconButton(
                                onClick = alIrAlCarrito,
                                modifier = Modifier.graphicsLayer(scaleX = cartScale.value, scaleY = cartScale.value)
                            ) {
                                Icon(Icons.Outlined.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

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
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (textoBusqueda.isEmpty()) {
                                Text("¿Qué tecnología buscas hoy?", color = Color.Gray, fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BasicTextField(
                                    value = textoBusqueda,
                                    onValueChange = { textoBusqueda = it },
                                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                                    modifier = Modifier.weight(1f),
                                    cursorBrush = SolidColor(ColorGalyRed),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                    singleLine = true
                                )
                                if (textoBusqueda.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar",
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { textoBusqueda = "" }
                                    )
                                }
                            }
                        }
                        
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
                                    val rippleScale by infiniteTransition.animateFloat(
                                        initialValue = 1f,
                                        targetValue = 2f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "scale"
                                    )
                                    val rippleAlpha by infiniteTransition.animateFloat(
                                        initialValue = 0.5f,
                                        targetValue = 0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1000),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "alpha"
                                    )
                                    Surface(
                                        modifier = Modifier.size(24.dp).graphicsLayer(scaleX = rippleScale, scaleY = rippleScale, alpha = rippleAlpha),
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
                        
                        IconButton(onClick = { 
                            showFilterSheet = true
                        }) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Filtros y Orden",
                                tint = if (ordenSeleccionado != "DEFECTO" || soloEnStock) ColorGalyRed else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
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
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val marcas = ProductRepository.marcas.ifEmpty {
                        listOf("Apple", "SONY", "Logitech", "SAMSUNG", "Lenovo", "ASUS").mapIndexed { index, nombre ->
                            com.galyxnovatec.tienda.data.model.MarcaCompartida(
                                id = -index - 1,
                                nombre = nombre,
                                slug = nombre.lowercase(),
                                activo = true
                            )
                        }
                    }
                    items(items = marcas, key = { it.id }) { marca ->
                        val selected = marcaSeleccionada.equals(marca.nombre, ignoreCase = true)
                        Surface(
                            modifier = Modifier.clickable { marcaSeleccionada = if (selected) null else marca.nombre },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color.White else Color(0xFF1A1A1A),
                            border = BorderStroke(1.dp, if (selected) Color.White else Color(0xFF333333))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MarcaLogo(marca = marca, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = marca.nombre.uppercase(),
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
                        onClick = { 
                            showMoreSheet = false
                            AuthManager.logout(context) 
                            alIrAPerfil() 
                        },
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
                        categoriaSeleccionada = if(categoriaSeleccionada == (cat as? String)) null else (cat as? String)
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
                            Text("MOSTRAR TODOS LOS PRODUCTOS", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (textoBusqueda.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    FlashSaleSection(listaProductosBase) { alSeleccionarProducto(it) }
                }

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
                                items(items = novedades, key = { it.id }) { prod ->
                                    Box(modifier = Modifier.width(180.dp)) {
                                        TarjetaProducto(prod, { alSeleccionarProducto(it) })
                                    }
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
                    val titleText = when {
                        textoBusqueda.isNotEmpty() -> "Resultados para \"${textoBusqueda}\""
                        categoriaSeleccionada != null -> "Categoría: ${categoriaSeleccionada}"
                        marcaSeleccionada != null -> "Marca: ${marcaSeleccionada}"
                        else -> "Recomendados para ti"
                    }
                    Text(titleText, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("${productosAMostrar.size} productos", color = ColorGalyRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (productosAMostrar.isEmpty() && (textoBusqueda.isNotEmpty() || categoriaSeleccionada != null || marcaSeleccionada != null)) {
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SearchOff, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No se encontraron productos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Prueba buscando por otra marca o categoría", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                textoBusqueda = ""
                                categoriaSeleccionada = null
                                marcaSeleccionada = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed)
                        ) {
                            Text("Limpiar filtros", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
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
                            alAgregarRapido = { 
                                CartManager.agregarProducto(context, it, 1)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "✅ ¡Tu producto ya está en el carrito!",
                                        actionLabel = "VER CARRITO",
                                        duration = SnackbarDuration.Short
                                    ).let { result ->
                                        if (result == SnackbarResult.ActionPerformed) {
                                            alIrAlCarrito()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                ServiceBannerSection()
            }

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

            item(span = { GridItemSpan(2) }) {
                AIAssistantBanner(onClick = alIrAChatAi)
            }

            item(span = { GridItemSpan(2) }) {
                FooterProfesional()
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Color(0xFF1E1E1E)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Filtrar y Ordenar Productos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Text("Ordenar por:", color = Color.Gray, fontSize = 14.sp)

                    val opcionesOrden = listOf(
                        "DEFECTO" to "Recomendados",
                        "PRECIO_MENOR" to "Menor precio a mayor",
                        "PRECIO_MAYOR" to "Mayor precio a menor",
                        "OFERTAS" to "Mayores ofertas",
                        "NOMBRE" to "Nombre (A-Z)"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        opcionesOrden.forEach { (clave, etiqueta) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { ordenSeleccionado = clave }
                                    .background(
                                        if (ordenSeleccionado == clave) ColorGalyRed.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = ordenSeleccionado == clave,
                                    onClick = { ordenSeleccionado = clave },
                                    colors = RadioButtonDefaults.colors(selectedColor = ColorGalyRed)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(etiqueta, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Solo productos con stock disponible", color = Color.White, fontSize = 14.sp)
                        Switch(
                            checked = soloEnStock,
                            onCheckedChange = { soloEnStock = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ColorGalyRed
                            )
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Aplicar Filtros", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
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
fun FlashSaleSection(productos: List<Producto>, onProductClick: (Producto) -> Unit) {
    val ofertasValidas = remember(productos) {
        productos.filter { it.id > 0 && (it.descuento != null && it.descuento > 0 || it.precioOferta < it.precioNormal) && it.stock > 0 }
    }
    if (ofertasValidas.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FlashOn, null, tint = Color.Yellow, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("OFERTAS ACTIVAS", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Text("${ofertasValidas.size} productos", color = Color.LightGray, fontSize = 12.sp)
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = ofertasValidas, key = { it.id }) { prod ->
                FlashSaleCard(prod, onProductClick)
            }
        }
    }
}

@Composable
fun FlashSaleCard(producto: Producto, onClick: (Producto) -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.width(160.dp).clickable { onClick(producto) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(producto.getFullImageUrl())
                        .crossfade(true)
                        .size(300, 300)
                        .build(),
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
                    Text("-${producto.descuento ?: 0}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(6.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(producto.nombre, color = Color.White, fontSize = 13.sp, maxLines = 1, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Column {
                    Text(
                        "S/ ${producto.precioNormal.toInt()}",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        style = TextStyle(textDecoration = TextDecoration.LineThrough)
                    )
                    Text("S/ ${producto.precioOferta.toInt()}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.weight(1f))
                
                Surface(
                    onClick = { 
                        CartManager.agregarProducto(context, producto, 1)
                        Toast.makeText(context, "✅ ¡Agregado al carrito!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = ColorGalyRed
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart, 
                            contentDescription = null, 
                            tint = Color.White, 
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
