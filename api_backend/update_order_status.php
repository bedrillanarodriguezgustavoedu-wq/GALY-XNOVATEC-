<?php
include 'db_config.php';
include 'fcm_helper.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['id']) && isset($data['estado'])) {
    $id = (int)$data['id'];
    $estado = $conn->real_escape_string($data['estado']);

    $sql = "UPDATE pedidos SET estado = '$estado' WHERE id = $id";

    if ($conn->query($sql)) {
        // --- LOGICA DE STOCK ---
        if ($estado === 'PAGADO') {
            $itemsRes = $conn->query("SELECT producto_id, cantidad FROM pedido_productos WHERE pedido_id = $id");
            while($item = $itemsRes->fetch_assoc()) {
                $pid = $item['producto_id'];
                $cant = $item['cantidad'];
                $conn->query("UPDATE productos SET stock = stock - $cant WHERE id = $pid");
            }
        }

        // Enviar notificación Push Real
        $resPedido = $conn->query("SELECT u.nombre as cliente, u.fcm_token FROM pedidos p JOIN usuarios u ON p.usuario_id = u.id WHERE p.id = $id");
        $pedido = $resPedido->fetch_assoc();
        if ($pedido) {
            $nombreCliente = $pedido['cliente'];
            $token = $pedido['fcm_token'];

            $msgTitle = "Pedido GALY #$id";
            $msgBody = "¡Hola $nombreCliente! Tu pedido cambió a: $estado.";

            if (!empty($token)) {
                sendFCMNotification($token, $msgTitle, $msgBody);
            }
            file_put_contents('fcm_log.txt', "[" . date('Y-m-d H:i:s') . "] PUSH ENVIADO A $nombreCliente (#$id): $msgBody\n", FILE_APPEND);
        }

        echo json_encode(["success" => true, "message" => "Estado actualizado"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al actualizar estado"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Datos incompletos"]);
}

$conn->close();
?>