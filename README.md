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
   - Coloca la carpeta `galy_api` en `htdocs` y conéctala a la misma base de datos `ecommerce` que usa Laravel. No importes un volcado de ejemplo sobre esa base.

3. **Configuración local (`local.properties`):**
   - Crea un archivo `local.properties` en la raíz (si no existe).
   - Configura estas URLs y usa el mismo ID de cliente web OAuth de Laravel para Google. El ID de cliente es público; **no añadas el `client_secret` de Google a la app**.
     ```properties
     BASE_URL="http://10.0.2.2/galy_api/"
     WEB_BASE_URL="http://10.0.2.2:8000/"
     GOOGLE_WEB_CLIENT_ID="EL_MISMO_GOOGLE_CLIENT_ID_DE_LARAVEL"
     WIFI_IP="IP_LOCAL_ACTUAL_DEL_PC"
     ```

4. **Ejecutar en Android Studio:**
   - Abre el proyecto, sincroniza Gradle y presiona "Run".
   - XAMPP debe servir `galy_api` y la tienda Laravel debe estar disponible en el puerto 8000. El servidor PHP local debe tener `GALY_GOOGLE_WEB_CLIENT_ID` configurado con el mismo ID OAuth.
   - Google solo inicia sesión si el token es válido y el correo corresponde a una cuenta **cliente** ya registrada en GALY; las cuentas administradoras se rechazan.
   - La app lee Yape/Plin desde los ajustes de la web. Cada método aparece solo cuando número, titular y QR están completos. Tras crear el pedido, el comprobante se guarda en el almacenamiento Laravel y queda pendiente de revisión del administrador.

### Conexión con XAMPP desde el emulador

Para el emulador oficial de Android, `local.properties` debe usar:

```properties
BASE_URL="http://10.0.2.2/galy_api/"
```

`10.0.2.2` representa al PC anfitrión dentro del emulador. Si el navegador del emulador muestra `ERR_CONNECTION_TIMED_OUT`, abre PowerShell **como administrador** en Windows y permite Apache:

```powershell
New-NetFirewallRule -DisplayName "GALY XAMPP HTTP 80" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 80 -Profile Private
```

Para usar la app desde un teléfono físico, permite también el puerto local 8000 de Laravel en la red privada:

```powershell
New-NetFirewallRule -DisplayName "GALY Laravel HTTP 8000" -Direction Inbound -Action Allow -Protocol TCP -LocalPort 8000 -Profile Private
```

También puedes ejecutar el archivo `allow-galy-xampp.ps1` ubicado en la raíz de este proyecto como administrador. El script crea la regla, intenta iniciar Apache y comprueba el panel local.

Después reinicia Apache desde XAMPP y prueba en el emulador:

`http://10.0.2.2/galy-admin/admin_web.php?section=dashboard`

En un teléfono físico conectado a la misma Wi-Fi, `WIFI_IP`, `BASE_URL` y `WEB_BASE_URL` deben apuntar a la IP local actual del PC; la IP detectada en este equipo es `192.168.1.52`. Si cambia, actualiza `WIFI_IP` en `local.properties` y vuelve a compilar:

```properties
BASE_URL="http://192.168.1.52/galy_api/"
WEB_BASE_URL="http://192.168.1.52:8000/"
```

---
*Desarrollado con ❤️ para GALY XNOVATEC.*
