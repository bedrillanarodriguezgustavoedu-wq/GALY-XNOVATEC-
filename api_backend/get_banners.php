<?php
error_reporting(0);
ini_set('display_errors', 0);
require_once __DIR__ . '/db_config.php';

$sql = "SELECT id, imagen AS imagen_url, titulo, subtitulo, enlace AS link,
               COALESCE(color_fondo, '#FF0055') AS color_fondo
        FROM banners
        WHERE activo = 1
          AND (fecha_publicacion IS NULL OR fecha_publicacion <= NOW())
        ORDER BY orden ASC, id ASC";
$result = $conn->query($sql);

if (!$result) {
    http_response_code(500);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode(['success' => false, 'message' => 'No se pudieron consultar los banners.']);
    $conn->close();
    exit;
}

$banners = [];
while ($row = $result->fetch_assoc()) {
    $row['id'] = (int) $row['id'];
    $banners[] = $row;
}

header('Content-Type: application/json; charset=utf-8');
echo json_encode($banners, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
$conn->close();
