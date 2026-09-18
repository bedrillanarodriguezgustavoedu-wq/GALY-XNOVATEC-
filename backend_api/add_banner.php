<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['imagen_url'])) {
    $imagen_url = $conn->real_escape_string($data['imagen_url']);
    $titulo = isset($data['titulo']) ? $conn->real_escape_string($data['titulo']) : '';
    $subtitulo = isset($data['subtitulo']) ? $conn->real_escape_string($data['subtitulo']) : '';
    $color_fondo = isset($data['color_fondo']) ? $conn->real_escape_string($data['color_fondo']) : '#FF0055';

    // Usamos el nombre correcto de la tabla: banners_inicio
    $sql = "INSERT INTO banners_inicio (imagen_url, titulo, subtitulo, color_fondo, orden)
            VALUES ('$imagen_url', '$titulo', '$subtitulo', '$color_fondo', 0)";

    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Banner creado correctamente"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al crear banner: " . $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Imagen requerida"]);
}

$conn->close();
?>