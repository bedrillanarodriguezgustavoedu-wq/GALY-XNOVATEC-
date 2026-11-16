package com.galyxnovatec.tienda.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.galyxnovatec.tienda.data.model.Categoria
import com.galyxnovatec.tienda.data.repository.ProductRepository
import com.galyxnovatec.tienda.ui.theme.ColorGalyRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesScreen(alVolver: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val categories = ProductRepository.listaCategoriasBase
    var categoryEditing by remember { mutableStateOf<Categoria?>(null) }
    var categoryToDelete by remember { mutableStateOf<Categoria?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    fun refreshCategories() {
        scope.launch {
            isLoading = true
            if (!ProductRepository.sincronizarCategorias()) {
                Toast.makeText(context, "No se pudieron cargar las categorías compartidas.", Toast.LENGTH_LONG).show()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refreshCategories() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Categorías compartidas", fontWeight = FontWeight.Black, color = Color.White)
                        Text("Catálogo de la web y GALY", fontSize = 11.sp, color = Color.LightGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { refreshCategories() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    categoryEditing = null
                    showEditor = true
                },
                containerColor = ColorGalyRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear categoría")
            }
        }
    ) { padding ->
        if (isLoading && categories.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorGalyRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Renombrar una categoría actualiza también los productos vinculados. Las categorías con productos no se pueden eliminar.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(categories, key = { it.id }) { category ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(category.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    "${category.productosCount} productos · ${category.slug.orEmpty()}",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = {
                                categoryEditing = category
                                showEditor = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar ${category.nombre}", tint = Color.White)
                            }
                            IconButton(
                                enabled = category.productosCount == 0 && !isDeleting,
                                onClick = { categoryToDelete = category }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Eliminar ${category.nombre}",
                                    tint = if (category.productosCount == 0) ColorGalyRed else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        var name by remember(categoryEditing?.id) { mutableStateOf(categoryEditing?.nombre.orEmpty()) }
        AlertDialog(
            onDismissRequest = { if (!isSaving) showEditor = false },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text(if (categoryEditing == null) "Nueva categoría" else "Editar categoría") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = ColorGalyRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                    )
                )
            },
            confirmButton = {
                Button(
                    enabled = name.isNotBlank() && !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                    onClick = {
                        isSaving = true
                        scope.launch {
                            val error = ProductRepository.guardarCategoria(categoryEditing?.id, name.trim())
                            isSaving = false
                            if (error == null) {
                                showEditor = false
                                Toast.makeText(context, "Categoría guardada en el catálogo compartido.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                    else Text("GUARDAR", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(enabled = !isSaving, onClick = { showEditor = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }

    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { if (!isDeleting) categoryToDelete = null },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text("Eliminar categoría") },
            text = { Text("¿Deseas eliminar \"${category.nombre}\"? Solo se permite si no tiene productos asociados.") },
            confirmButton = {
                Button(
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorGalyRed),
                    onClick = {
                        isDeleting = true
                        scope.launch {
                            val error = ProductRepository.eliminarCategoria(category.id)
                            isDeleting = false
                            categoryToDelete = null
                            Toast.makeText(context, error ?: "Categoría eliminada.", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    if (isDeleting) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                    else Text("ELIMINAR", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(enabled = !isDeleting, onClick = { categoryToDelete = null }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }
}
