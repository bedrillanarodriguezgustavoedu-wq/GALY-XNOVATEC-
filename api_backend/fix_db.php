<?php
header('Content-Type: application/json; charset=utf-8');

// Usamos db_config.php si existe, si no, intentamos conectar manual
$host = "localhost";
$user = "root";
$pass = "";
$db_name = "galy";

// 1. Conexión inicial sin base de datos
$conn = new mysqli($host, $user, $pass);
if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Error de conexión: " . $conn->connect_error]));
}

// 2. Crear base de datos si no existe
$conn->query("CREATE DATABASE IF NOT EXISTS $db_name");
$conn->select_db($db_name);
$conn->set_charset("utf8");

function addColumn($conn, $table, $column, $definition) {
    $res = $conn->query("SHOW COLUMNS FROM `$table` LIKE '$column'");
    if ($res->num_rows == 0) {
        $conn->query("ALTER TABLE `$table` ADD `$column` $definition");
        return true;
    }
    return false;
}

$report = [];

// --- TABLA USUARIOS ---
$conn->query("CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    direccion TEXT,
    es_admin TINYINT(1) DEFAULT 0,
    puntos INT DEFAULT 0,
    imagen_url VARCHAR(255),
    fcm_token TEXT
)");
$report['usuarios'] = "Tabla verificada";
addColumn($conn, 'usuarios', 'puntos', "INT DEFAULT 0");
addColumn($conn, 'usuarios', 'imagen_url', "VARCHAR(255)");
addColumn($conn, 'usuarios', 'fcm_token', "TEXT");

// --- TABLA CATEGORIAS ---
$conn->query("CREATE TABLE IF NOT EXISTS categorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    imagen_url VARCHAR(255)
)");
$report['categorias'] = "Tabla verificada";

// --- TABLA PRODUCTOS ---
$conn->query("CREATE TABLE IF NOT EXISTS productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    marca VARCHAR(100),
    categoria_nombre VARCHAR(100),
    precio_oferta DECIMAL(10,2) NOT NULL,
    precio_normal DECIMAL(10,2) NOT NULL,
    imagen_url VARCHAR(255),
    descripcion TEXT,
    disponible TINYINT(1) DEFAULT 1,
    tiene_envio_gratis TINYINT(1) DEFAULT 0,
    etiqueta_especial VARCHAR(50),
    descuento INT,
    calificacion DECIMAL(3,1) DEFAULT 4.5,
    num_revisiones INT DEFAULT 100,
    especificaciones TEXT,
    variantes TEXT,
    imagenes_secundarias TEXT,
    stock INT DEFAULT 10,
    precio_compra DECIMAL(10,2) DEFAULT 0.0
)");
$report['productos'] = "Tabla verificada";
addColumn($conn, 'productos', 'especificaciones', "TEXT");
addColumn($conn, 'productos', 'variantes', "TEXT");
addColumn($conn, 'productos', 'imagenes_secundarias', "TEXT");
addColumn($conn, 'productos', 'stock', "INT DEFAULT 10");
addColumn($conn, 'productos', 'marca', "VARCHAR(100)");
addColumn($conn, 'productos', 'precio_compra', "DECIMAL(10,2) DEFAULT 0.0");

// --- TABLA BANNERS ---
$conn->query("CREATE TABLE IF NOT EXISTS banners_inicio (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255),
    subtitulo VARCHAR(255),
    imagen_url VARCHAR(255),
    link_url VARCHAR(255)
)");
$report['banners'] = "Tabla verificada";

// --- TABLA PEDIDOS ---
$conn->query("CREATE TABLE IF NOT EXISTS pedidos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    total DECIMAL(10,2),
    estado VARCHAR(50) DEFAULT 'PENDIENTE',
    metodo_pago VARCHAR(50),
    cliente_telefono VARCHAR(20),
    cliente_email VARCHAR(100),
    cliente_dni VARCHAR(20),
    metodo_entrega VARCHAR(50) DEFAULT 'DOMICILIO',
    direccion TEXT,
    ciudad VARCHAR(100),
    distrito VARCHAR(100),
    referencia TEXT,
    maps_url TEXT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)");
$report['pedidos'] = "Tabla verificada";
addColumn($conn, 'pedidos', 'cliente_telefono', "VARCHAR(20)");
addColumn($conn, 'pedidos', 'cliente_email', "VARCHAR(100)");
addColumn($conn, 'pedidos', 'cliente_dni', "VARCHAR(20)");
addColumn($conn, 'pedidos', 'metodo_entrega', "VARCHAR(50) DEFAULT 'DOMICILIO'");
addColumn($conn, 'pedidos', 'direccion', "TEXT");
addColumn($conn, 'pedidos', 'ciudad', "VARCHAR(100)");
addColumn($conn, 'pedidos', 'distrito', "VARCHAR(100)");
addColumn($conn, 'pedidos', 'referencia', "TEXT");
addColumn($conn, 'pedidos', 'maps_url', "TEXT");

// --- TABLA PEDIDO_PRODUCTOS ---
$conn->query("CREATE TABLE IF NOT EXISTS pedido_productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id INT,
    producto_id INT,
    cantidad INT,
    precio DECIMAL(10,2)
)");
$report['pedido_productos'] = "Tabla verificada";

// --- TABLA DIRECCIONES ---
$conn->query("CREATE TABLE IF NOT EXISTS direcciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    nombre VARCHAR(100),
    telefono VARCHAR(20),
    direccion TEXT,
    distrito VARCHAR(100),
    ciudad VARCHAR(100),
    referencia TEXT,
    maps_url TEXT
)");
$report['direcciones'] = "Tabla verificada";

// --- TABLA COMENTARIOS ---
$conn->query("CREATE TABLE IF NOT EXISTS comentarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    producto_id INT,
    usuario_id INT,
    nombre_usuario VARCHAR(100),
    comentario TEXT,
    calificacion INT,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)");
$report['comentarios'] = "Tabla verificada";

// --- TABLA CATALOGO SERVICIOS (NUEVO PARA SOPORTE) ---
$conn->query("CREATE TABLE IF NOT EXISTS catalogo_servicios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) DEFAULT 0.0,
    icono VARCHAR(50)
)");
$report['catalogo_servicios'] = "Tabla creada para oferta de servicios";

// --- TABLA SERVICIOS TECNICOS (TICKETS) ---
$conn->query("CREATE TABLE IF NOT EXISTS servicios_tecnicos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    equipo_modelo VARCHAR(100) NOT NULL,
    descripcion_falla TEXT,
    estado ENUM('Recibido','En Diagnostico','Reparando','Listo','Entregado') DEFAULT 'Recibido',
    costo_estimado DECIMAL(10,2),
    imagen_ingreso VARCHAR(255),
    imagen_listo VARCHAR(255),
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)");
$report['servicios_tecnicos'] = "Tabla verificada para tickets de soporte";
addColumn($conn, 'servicios_tecnicos', 'imagen_ingreso', "VARCHAR(255)");
addColumn($conn, 'servicios_tecnicos', 'imagen_listo', "VARCHAR(255)");

// --- TABLA CUPONES ---
$conn->query("CREATE TABLE IF NOT EXISTS cupones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    descuento_porcentaje INT NOT NULL,
    estado TINYINT(1) DEFAULT 1
)");
$conn->query("INSERT IGNORE INTO cupones (codigo, descuento_porcentaje) VALUES ('GALY2025', 10), ('GALY2026', 15), ('XNOVATEC', 20)");
$report['cupones'] = "Cupones actualizados";

// --- FIX CATEGORIAS ENCODING ---
$conn->query("UPDATE categorias SET nombre = 'Audífonos' WHERE nombre LIKE 'Aud%'");
$report['categorias_fix'] = "Nombres de categorías corregidos";


echo json_encode([
    "success" => true,
    "message" => "Base de datos GALY optimizada y tablas sincronizadas correctamente.",
    "details" => $report
], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);

$conn->close();
?>