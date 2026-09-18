<?php
include 'db_config.php';
include 'fcm_helper.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['id']) && isset($data['estado'])) {
    $id = (int)$data['id'];
    $estado = $conn->real_escape_string($data['estado']);
    $costo = isset($data['costo']) ? (float)$data['costo'] : null;

    $sql = "UPDATE servicios_tecnicos SET estado = '$estado'";
    if ($costo !== null) $sql .= ", costo_estimado = $costo";
    $sql .= " WHERE id = $id";

    if ($conn->query($sql)) {
        // Enviar notificación Real
        $res = $conn->query("SELECT u.fcm_token, u.nombre, t.equipo_modelo FROM servicios_tecnicos t JOIN usuarios u ON t.usuario_id = u.id WHERE t.id = $id");
        $row = $res->fetch_assoc();
        if ($row && !empty($row['fcm_token'])) {
            $title = "Soporte Técnico GALY";
            $body = "¡Hola {$row['nombre']}! Tu {$row['equipo_modelo']} cambió a: $estado.";
            sendFCMNotification($row['fcm_token'], $title, $body);
        }

        file_put_contents('fcm_log.txt', "[" . date('Y-m-d H:i:s') . "] TICKET #$id ACTUALIZADO A: $estado\n", FILE_APPEND);
        echo json_encode(["success" => true, "message" => "Estado actualizado"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error: " . $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Datos incompletos"]);
}
$conn->close();
?>