<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['nombre'])) {
    $nombre = $conn->real_escape_string($data['nombre']);

    $check = $conn->query("SELECT id FROM categorias WHERE nombre = '$nombre'");
    if ($check->num_rows > 0) {
        echo json_encode(["success" => false, "message" => "La categoría ya existe"]);
    } else {
        $sql = "INSERT INTO categorias (nombre) VALUES ('$nombre')";
        if ($conn->query($sql)) {
            echo json_encode(["success" => true, "message" => "Categoría creada", "id" => $conn->insert_id]);
        } else {
            echo json_encode(["success" => false, "message" => "Error al crear categoría"]);
        }
    }
} else {
    echo json_encode(["success" => false, "message" => "Nombre requerido"]);
}

$conn->close();
?>