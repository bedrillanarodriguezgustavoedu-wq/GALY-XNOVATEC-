<?php
header('Content-Type: application/json; charset=utf-8');
include 'db_config.php';

$report = [];

// 1. Limpiar banners, productos y catálogo de servicios
$conn->query("DELETE FROM banners_inicio");
$conn->query("DELETE FROM productos");
$conn->query("DELETE FROM catalogo_servicios");
$conn->query("ALTER TABLE productos AUTO_INCREMENT = 1");

// 2. INSERTAR BANNERS
$banners = [
    ["LAPTOPS GAMER 2026", "Nitro, Victus y LOQ disponibles", "Captura de pantalla 2026-09-09 095351.png"],
    ["SONIDO PROFESIONAL", "Vive la música con GALY", "sony_audifonos.jpg"],
    ["DE VUELTA AL COLE", "Equípate con lo mejor en tecnología", "Captura de pantalla 2026-09-09 095643.png"]
];

foreach ($banners as $b) {
    $stmt = $conn->prepare("INSERT INTO banners_inicio (titulo, subtitulo, imagen_url) VALUES (?, ?, ?)");
    $stmt->bind_param("sss", $b[0], $b[1], $b[2]);
    $stmt->execute();
}
$report['banners'] = "3 banners creados";

// 3. INSERTAR PRODUCTOS
$prods = [
    // Laptops
    ["HP Elitebook Pro", "HP", "Laptops", 3500, 3100, "Captura de pantalla 2026-09-09 095102.png", "Potencia y elegancia para el trabajo.", 11, "BUSINESS", 5, "CPU: i5, RAM: 16GB"],
    ["Acer Nitro V15", "ACER", "Laptops", 4200, 3850, "Captura de pantalla 2026-09-09 100709.png", "Rendimiento gaming superior.", 8, "OFERTA", 10, "RTX 4050, 16GB RAM"],
    ["MacBook Air M2", "APPLE", "Laptops", 5200, 4800, "macbook.jpg", "Chip Apple M2 ultra eficiente.", 8, "PREMIUM", 3, "Batería 18h, Liquid Retina"],

    // Monitores
    ["Monitor LG UltraWide", "LG", "Monitores", 1100, 899, "monitor_lg.jpg", "Más espacio para tu productividad.", 18, "OFERTA", 15, "29 pulgadas, IPS, HDR"],
    ["Monitor Samsung Odyssey", "SAMSUNG", "Monitores", 1500, 1300, "Captura de pantalla 2026-09-09 095300.png", "Curvatura inmersiva para gamers.", 13, "GAMING", 8, "144Hz, 1ms"],

    // Audio
    ["Sony WH-1000XM5", "SONY", "Audífonos", 1450, 1250, "sony_audifonos.jpg", "La mejor cancelación de ruido.", 14, "PREMIUM", 20, "Bluetooth 5.2, 30h batería"],
    ["JBL Quantum 400", "JBL", "Audífonos", 400, 299, "jbl_audifonos.jpg", "Escucha cada detalle del juego.", 25, "OFERTA", 30, "Micrófono plegable, Sonido 3D"],

    // Periféricos
    ["Mouse Logitech G502", "LOGITECH", "Mouses", 250, 199, "mouse_logitech.jpg", "Sensor HERO de alta precisión.", 20, "GAMER CHOICE", 50, "11 botones programables"],
    ["Teclado Keychron K2", "KEYCHRON", "Teclados", 450, 380, "teclado_keychron.jpg", "Teclado mecánico inalámbrico.", 16, "RECOMENDADO", 15, "Switches Brown, Retroiluminado"]
];

foreach ($prods as $p) {
    $sql = "INSERT INTO productos (nombre, marca, categoria_nombre, precio_normal, precio_oferta, imagen_url, descripcion, descuento, etiqueta_especial, stock, especificaciones)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sssddssisis", $p[0], $p[1], $p[2], $p[3], $p[4], $p[5], $p[6], $p[7], $p[8], $p[9], $p[10]);
    $stmt->execute();
}
$report['productos'] = count($prods) . " productos creados";

// 4. INSERTAR CATALOGO DE SERVICIOS
$servicios = [
    ["Reparación de Hardware", "Mantenimiento preventivo de placas madre y cambio de pantallas.", 0.0, "developer_board"],
    ["Optimización Gaming", "Instalación de drivers y limpieza de sistema para máximo FPS.", 0.0, "terminal"],
    ["Instalación de Software", "Windows 11, Office y Suite Adobe completa.", 0.0, "settings"]
];
foreach ($servicios as $s) {
    $stmt = $conn->prepare("INSERT INTO catalogo_servicios (nombre, descripcion, precio, icono) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("ssds", $s[0], $s[1], $s[2], $s[3]);
    $stmt->execute();
}
$report['servicios'] = "3 servicios creados";

echo json_encode([
    "success" => true,
    "message" => "GALY RE-POPULATE: Base de datos sincronizada con imágenes locales exitosamente.",
    "report" => $report
], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);

$conn->close();
?>