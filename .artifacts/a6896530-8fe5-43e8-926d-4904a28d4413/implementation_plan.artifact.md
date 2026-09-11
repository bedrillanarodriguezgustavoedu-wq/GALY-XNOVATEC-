# Mostrar nuevos productos y columna de descuento

Este plan detalla los cambios necesarios para integrar la nueva columna `descuento` en el modelo de datos y mostrarla en la interfaz de usuario, además de abordar el problema de sincronización con el servidor XAMPP.

## Cambios Propuestos

### Modelo de Datos
#### [MODIFY] [Producto.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/data/model/Producto.kt)
- Agregar el campo `descuento` (Int?) con la anotación `@SerializedName("descuento")`.

### Interfaz de Usuario
#### [MODIFY] [TarjetaProducto.kt](file:///C:/Users/USUARIO/AndroidStudioProjects/GALY/app/src/main/java/com/example/galy/ui/components/TarjetaProducto.kt)
- Agregar una etiqueta (Badge) roja sobre la imagen del producto que muestre el porcentaje de descuento si `descuento > 0`.

## Verificación de Conexión (XAMPP)
- Los logs indican un error **HTTP 404 Not Found**. Se recomienda verificar que la carpeta en `htdocs` se llame exactamente `galy_api` y contenga el archivo `get_products.php`.

## Plan de Verificación
1. Compilar y ejecutar la aplicación.
2. Verificar que los productos con descuento muestren la etiqueta roja.
3. Comprobar en los logs que la petición a `get_products.php` sea exitosa (200 OK) para ver los nuevos productos de la base de datos.
