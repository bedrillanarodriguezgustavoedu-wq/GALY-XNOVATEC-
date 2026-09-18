<?php
error_reporting(0);
ini_set('display_errors', 0);
include 'db_config.php';

// Verificamos si la columna 'subtitulo' existe o si debemos usar 'link'
// Según tu tabla es 'subtitulo'
$sql = "SELECT id, imagen_url, titulo, subtitulo, color_fondo FROM banners_inicio ORDER BY orden ASC";
$result = $conn->query($sql);

$banners = [];
if ($result) {
    while($row = $result->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        // Si la App aún espera 'link', lo duplicamos para evitar errores
        $row['link'] = $row['subtitulo'];
        $banners[] = $row;
    }
}

header('Content-Type: application/json; charset=utf-8');
echo json_encode($banners, JSON_UNESCAPED_UNICODE);
$conn->close();
?>