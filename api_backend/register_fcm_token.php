<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['usuario_id']) && isset($data['token'])) {
    $uid = (int)$data['usuario_id'];
    $token = $conn->real_escape_string($data['token']);

    $sql = "UPDATE usuarios SET fcm_token = '$token' WHERE id = $uid";

    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Token FCM registrado"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al registrar token"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Datos incompletos"]);
}

$conn->close();
?>