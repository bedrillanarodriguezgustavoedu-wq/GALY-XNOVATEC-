<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['codigo']) && isset($data['descuento_porcentaje'])) {
    $codigo = strtoupper($conn->real_escape_string($data['codigo']));
    $desc = (int)$data['descuento_porcentaje'];

    $sql = "INSERT INTO cupones (codigo, descuento_porcentaje) VALUES ('$codigo', $desc)
            ON DUPLICATE KEY UPDATE descuento_porcentaje = $desc";

    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Cupón guardado"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Datos incompletos"]);
}

$conn->close();
?>