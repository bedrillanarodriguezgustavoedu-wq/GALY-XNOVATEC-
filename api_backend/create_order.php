<?php
// Desactivar errores que ensucian el JSON
error_reporting(0);
header('Content-Type: application/json; charset=utf-8');
include 'db_config.php';

$data = json_decode(file_get_contents('php://input'), true);

if (!$data) {
    echo json_encode(["success" => false, "message" => "No se recibieron datos JSON válidos"]);
    exit;
}

$usuario_id = isset($data['usuario_id']) ? (int)$data['usuario_id'] : 0;
$total = isset($data['total']) ? (float)$data['total'] : 0.0;
$items = isset($data['items']) ? $data['items'] : [];
$metodo_pago = isset($data['metodo_pago'])
    ? strtolower(trim((string) $data['metodo_pago']))
    : 'yape';
$metodo_pago_sql = $conn->real_escape_string($metodo_pago);
$es_pago_manual = in_array($metodo_pago, ['yape', 'plin', 'bcp'], true);

// Nuevos campos para la boleta y entrega
$cliente_nombre = isset($data['cliente_nombre']) ? $conn->real_escape_string($data['cliente_nombre']) : '';
$cliente_telefono = isset($data['cliente_telefono']) ? $conn->real_escape_string($data['cliente_telefono']) : '';
$cliente_email = isset($data['cliente_email']) ? $conn->real_escape_string($data['cliente_email']) : '';
$cliente_dni = isset($data['cliente_dni']) ? $conn->real_escape_string($data['cliente_dni']) : '';
$metodo_entrega = isset($data['metodo_entrega']) ? $conn->real_escape_string($data['metodo_entrega']) : 'DOMICILIO';
$direccion = isset($data['direccion']) ? $conn->real_escape_string($data['direccion']) : '';
$ciudad = isset($data['ciudad']) ? $conn->real_escape_string($data['ciudad']) : '';
$distrito = isset($data['distrito']) ? $conn->real_escape_string($data['distrito']) : '';
$referencia = isset($data['referencia']) ? $conn->real_escape_string($data['referencia']) : '';
$maps_url = isset($data['maps_url']) ? $conn->real_escape_string($data['maps_url']) : '';

if ($usuario_id <= 0 || empty($items)) {
    echo json_encode(["success" => false, "message" => "Datos de pedido incompletos"]);
    exit;
}

// 1. Crear el pedido principal con todos los datos
$sqlPedido = "INSERT INTO pedidos (
    usuario_id, total, estado, metodo_pago,
    cliente_nombre, cliente_telefono, cliente_email, cliente_dni, metodo_entrega,
    direccion, ciudad, distrito, referencia, maps_url
) VALUES (
    $usuario_id, $total, 'PENDIENTE', '$metodo_pago_sql',
    '$cliente_nombre', '$cliente_telefono', '$cliente_email', '$cliente_dni', '$metodo_entrega',
    '$direccion', '$ciudad', '$distrito', '$referencia', '$maps_url'
)";

if (!$conn->begin_transaction()) {
    echo json_encode(["success" => false, "message" => "No se pudo iniciar el registro del pedido."]);
    exit;
}

if ($conn->query($sqlPedido)) {
    $pedido_id = $conn->insert_id;
    $todo_ok = true;

    // 2. Insertar cada producto del pedido
    foreach ($items as $item) {
        $prod_id = (int)$item['producto_id'];
        $cant = (int)$item['cantidad'];
        $precio = (float)$item['precio'];

        // Verificamos si la tabla es pedido_productos o pedido_items
        $sqlItem = "INSERT INTO pedido_productos (pedido_id, producto_id, cantidad, precio)
                    VALUES ($pedido_id, $prod_id, $cant, $precio)";

        if (!$conn->query($sqlItem)) {
            $todo_ok = false;
            break;
        }
    }

    if ($todo_ok && $es_pago_manual) {
        $paymentStatement = $conn->prepare(
            "INSERT INTO pedido_pagos_manuales
                (pedido_id, usuario_id, metodo_pago, estado, created_at, updated_at)
             VALUES (?, ?, ?, 'pendiente', NOW(), NOW())"
        );
        if (!$paymentStatement) {
            error_log("GALY manual payment registration prepare failed: " . $conn->error);
            $todo_ok = false;
        } else {
            $paymentStatement->bind_param("iis", $pedido_id, $usuario_id, $metodo_pago);
            if (!$paymentStatement->execute()) {
                error_log("GALY manual payment registration failed: " . $paymentStatement->error);
                $todo_ok = false;
            }
            $paymentStatement->close();
        }
    }

    if ($todo_ok && $conn->commit()) {
        echo json_encode([
            "success" => true,
            "message" => "¡Pedido GALY realizado!",
            "pedido_id" => $pedido_id,
            "manual_payment_registered" => $es_pago_manual,
        ]);
    } else {
        $conn->rollback();
        echo json_encode([
            "success" => false,
            "message" => $es_pago_manual
                ? "No se pudo registrar el pedido y su pago. Inténtalo nuevamente o contacta a la tienda."
                : "No se pudo completar el pedido. Inténtalo nuevamente.",
        ]);
    }
} else {
    $conn->rollback();
    echo json_encode(["success" => false, "message" => "Error al crear pedido principal: " . $conn->error]);
}

$conn->close();
?>