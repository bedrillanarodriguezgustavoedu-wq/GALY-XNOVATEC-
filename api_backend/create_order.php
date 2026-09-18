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
$metodo_pago = isset($data['metodo_pago']) ? $data['metodo_pago'] : 'YAPE';

// Nuevos campos para la boleta y entrega
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
    cliente_telefono, cliente_email, cliente_dni, metodo_entrega,
    direccion, ciudad, distrito, referencia, maps_url
) VALUES (
    $usuario_id, $total, 'PENDIENTE', '$metodo_pago',
    '$cliente_telefono', '$cliente_email', '$cliente_dni', '$metodo_entrega',
    '$direccion', '$ciudad', '$distrito', '$referencia', '$maps_url'
)";

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

    if ($todo_ok) {
        echo json_encode(["success" => true, "message" => "¡Pedido GALY realizado!", "pedido_id" => $pedido_id]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al insertar productos: " . $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Error al crear pedido principal: " . $conn->error]);
}

$conn->close();
?>