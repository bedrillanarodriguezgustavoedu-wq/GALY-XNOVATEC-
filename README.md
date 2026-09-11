# GALY XNOVATEC - Tienda de Tecnología 🚀

GALY XNOVATEC es una aplicación móvil moderna desarrollada en **Android Native (Kotlin)** utilizando **Jetpack Compose**. Está diseñada para ofrecer una experiencia de compra premium con un panel de administración integrado para gestionar ventas, inventario y usuarios.

## 🛠️ Tecnologías Utilizadas

- **Interfaz de Usuario:** Jetpack Compose (Material 3)
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Redes:** Retrofit 2 & OkHttp 3
- **Imágenes:** Coil para carga asíncrona
- **Backend (Local):** PHP (XAMPP) & MySQL
- **IA:** Galy AI (Python Brain Integration)
- **Reportes:** Generación de boletas en PDF

## ✨ Características Principales

### Para el Usuario 👤
- Catálogo de productos premium con galería de imágenes.
- Carrito de compras y sistema de favoritos.
- Historial de pedidos detallado con estado de entrega.
- Edición de perfil (foto, nombre, teléfono).
- Chat con Galy AI para recomendaciones.

### Para el Administrador 🛡️
- **Dashboard Maestro:** Resumen de ventas y tendencia de los últimos 7 días.
- **Gestión de Pedidos:** Buscador, filtros por estado y enlace directo a Google Maps.
- **Inventario Pro:** Alta, edición y moderación de galería de fotos de productos.
- **Moderación:** Sistema para editar o eliminar comentarios de clientes.
- **Banners:** Gestión de ofertas principales directamente desde el móvil.

## 🚀 Instalación y Configuración

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/GALY.git
   ```

2. **Servidor Backend:**
   - Asegúrate de tener **XAMPP** (u otro servidor PHP/MySQL) corriendo.
   - Importa la base de datos `galy_db.sql`.
   - Coloca la carpeta `galy_api` en tu directorio `htdocs`.

3. **Configuración de IP:**
   - Por defecto, la app usa `10.0.2.2` para el emulador de Android.
   - Si usas un dispositivo físico, cambia la `BASE_URL` en `RetrofitClient` por la IP local de tu PC.

4. **Ejecutar en Android Studio:**
   - Abre el proyecto, sincroniza Gradle y presiona "Run".

---
*Desarrollado con ❤️ para GALY XNOVATEC.*
