<?php
include 'db_config.php';
header('Content-Type: application/json');

// Obtener todos los pedidos con info completa del cliente y entrega
$sql = "SELECT p.*, u.nombre as cliente_nombre, u.email as cliente_email, u.telefono as cliente_telefono
        FROM pedidos p
        LEFT JOIN usuarios u ON p.usuario_id = u.id
        ORDER BY p.id DESC";

$result = $conn->query($sql);

$pedidos = [];
while($row = $result->fetch_assoc()) {
    $pedido_id = $row['id'];
    $row['id'] = (int)$row['id'];
    $row['total'] = (float)$row['total'];

    // Obtener productos
    $sqlProd = "SELECT pp.cantidad, pp.precio, prod.nombre, prod.imagen_url, prod.precio_compra
                FROM pedido_productos pp
                JOIN productos prod ON pp.producto_id = prod.id
                WHERE pp.pedido_id = $pedido_id";
    $resProd = $conn->query($sqlProd);

    $productos = [];
    while($p = $resProd->fetch_assoc()) {
        $p['cantidad'] = (int)$p['cantidad'];
        $p['precio'] = (float)$p['precio'];
        $p['precio_compra'] = (float)$p['precio_compra'];
        $productos[] = $p;
    }

    $row['productos'] = $productos;
    $pedidos[] = $row;
}

echo json_encode($pedidos);
$conn->close();
?>