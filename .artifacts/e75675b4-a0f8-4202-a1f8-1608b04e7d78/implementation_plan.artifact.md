# Plan de Finalización del Proyecto GALY

Este plan detalla los pasos necesarios para completar las funcionalidades faltantes y convertir a GALY en una aplicación de e-commerce funcional y profesional.

## User Review Required

> [!IMPORTANT]
> Se requiere el uso de una base de datos local (Room) y almacenamiento de preferencias (DataStore) para que la app no pierda datos al cerrarse. Esto implica cambios en la arquitectura de los `Managers`.
>
> [!NOTE]
> La navegación por categorías ahora filtrará el Home en lugar de solo regresar a él.

## Proposed Changes

### 1. Navegación y Modelos
#### [MODIFY] [Screen.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/navigation/Screen.kt)
- Agregar rutas para `Addresses` y `OrderDetail`.
- Modificar la ruta de `Home` para aceptar una categoría opcional.

#### [MODIFY] [NavGraph.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/navigation/NavGraph.kt)
- Configurar los nuevos destinos y el paso de parámetros.

---

### 2. Gestión de Direcciones
#### [NEW] [AddressesScreen.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/ui/screens/AddressesScreen.kt)
- Pantalla para listar, agregar y eliminar direcciones del usuario.
- Integración con `RetrofitClient` para persistencia en XAMPP.

#### [MODIFY] [ProfileScreen.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/ui/screens/ProfileScreen.kt)
- Conectar el botón "Mis Direcciones" con la nueva pantalla.

#### [MODIFY] [CheckoutScreen.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/ui/screens/CheckoutScreen.kt)
- Permitir al usuario seleccionar una de sus direcciones guardadas en lugar de usar una fija.

---

### 3. Historial de Pedidos Detallado
#### [NEW] [OrderDetailScreen.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/ui/screens/OrderDetailScreen.kt)
- Pantalla que muestra el desglose de productos, precios y estado de un pedido específico.

#### [MODIFY] [OrdersScreen.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/ui/screens/OrdersScreen.kt)
- Hacer que cada tarjeta de pedido sea clickable para navegar al detalle.

---

### 4. Flujo de Categorías y Búsqueda
#### [MODIFY] [CategoriesScreen.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/ui/screens/CategoriesScreen.kt)
- Al seleccionar una categoría, navegar al `Home` pasando el nombre de la categoría como argumento.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/ui/screens/HomeScreen.kt)
- Recibir el parámetro de categoría e inicializar el filtro automáticamente.

---

### 5. Persistencia y Sesión
#### [MODIFY] [AuthManager.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/data/manager/AuthManager.kt)
- Implementar `DataStore` para guardar el ID del usuario y mantener la sesión activa.

#### [MODIFY] [CartManager.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/data/manager/CartManager.kt)
- (Opcional en esta fase) Integrar `Room` para que el carrito no se vacíe al cerrar la app.

## Verification Plan

### Automated Tests
- No se requieren pruebas automatizadas nuevas en esta fase, pero se verificará la compilación.

### Manual Verification
1.  **Flujo de Compra**: Registrar una dirección, ir al carrito, seleccionar la dirección en el checkout y finalizar la compra.
2.  **Historial**: Ver el pedido recién realizado en la lista de pedidos y entrar al detalle para ver los productos.
3.  **Categorías**: Ir a la pantalla de categorías, elegir "Laptops" y verificar que el Home solo muestre laptops.
4.  **Sesión**: Cerrar la app y volver a abrirla para verificar que el usuario sigue logueado.
