<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['usuario_id']) && isset($data['equipo_modelo'])) {
    $uid = (int)$data['usuario_id'];
    $modelo = $conn->real_escape_string($data['equipo_modelo']);
    $falla = $conn->real_escape_string($data['descripcion_falla']);

    $sql = "INSERT INTO servicios_tecnicos (usuario_id, equipo_modelo, descripcion_falla) VALUES ($uid, '$modelo', '$falla')";

    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Ticket creado", "id" => $conn->insert_id]);
    } else {
        echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Datos incompletos"]);
}
$conn->close();
?>