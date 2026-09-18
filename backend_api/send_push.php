<?php
include 'db_config.php';
include 'fcm_helper.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['titulo']) && isset($data['mensaje'])) {
    $titulo = $data['titulo'];
    $mensaje = $data['mensaje'];

    // Obtener todos los tokens de la base de datos
    $res = $conn->query("SELECT fcm_token FROM usuarios WHERE fcm_token IS NOT NULL AND fcm_token != ''");

    $count = 0;
    while($row = $res->fetch_assoc()) {
        sendFCMNotification($row['fcm_token'], $titulo, $mensaje);
        $count++;
    }

    file_put_contents('fcm_log.txt', "[" . date('Y-m-d H:i:s') . "] PUSH MASIVO: $titulo ($count dispositivos)\n", FILE_APPEND);

    echo json_encode([
        "success" => true,
        "message" => "Anuncio enviado correctamente a $count dispositivos."
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Datos incompletos"]);
}
?>