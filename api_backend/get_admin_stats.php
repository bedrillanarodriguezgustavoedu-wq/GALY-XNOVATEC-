<?php
include 'db_config.php';
header('Content-Type: application/json');

// 1. Ventas Totales
$resVentas = $conn->query("SELECT SUM(total) as total FROM pedidos WHERE estado != 'CANCELADO'");
$ventas = $resVentas->fetch_assoc()['total'] ?? 0;

// 2. Pedidos Totales
$resPedidos = $conn->query("SELECT COUNT(*) as total FROM pedidos");
$pedidos = $resPedidos->fetch_assoc()['total'] ?? 0;

// 3. Usuarios Totales
$resUsuarios = $conn->query("SELECT COUNT(*) as total FROM usuarios");
$usuarios = $resUsuarios->fetch_assoc()['total'] ?? 0;

// 4. Ganancia Real
$resGanancia = $conn->query("SELECT SUM(pp.cantidad * (pp.precio - p.precio_compra)) as total FROM pedido_productos pp JOIN pedidos ped ON pp.pedido_id = ped.id JOIN productos p ON pp.producto_id = p.id WHERE ped.estado = 'ENTREGADO'");
$ganancia = $resGanancia->fetch_assoc()['total'] ?? 0;

// 5. Stock Crítico
$checkStock = $conn->query("SHOW COLUMNS FROM productos LIKE 'stock'");
$stockStatus = "85%";
if ($checkStock->num_rows > 0) {
    $resStock = $conn->query("SELECT COUNT(*) as total FROM productos WHERE stock < 5");
    $critico = $resStock->fetch_assoc()['total'] ?? 0;
    $stockStatus = (string)$critico;
}

// 6. Top Productos
$resTop = $conn->query("SELECT p.id, p.nombre, SUM(pp.cantidad) as total_vendido
                       FROM pedido_productos pp
                       JOIN productos p ON pp.producto_id = p.id
                       GROUP BY pp.producto_id
                       ORDER BY total_vendido DESC
                       LIMIT 3");
$topProds = [];
while($tp = $resTop->fetch_assoc()) {
    $topProds[] = $tp['nombre'] . " (x" . $tp['total_vendido'] . ")";
}

// 7. Clientes VIP
$resVip = $conn->query("SELECT u.nombre, SUM(p.total) as total_gastado
                       FROM pedidos p
                       JOIN usuarios u ON p.usuario_id = u.id
                       WHERE p.estado != 'CANCELADO'
                       GROUP BY p.usuario_id
                       ORDER BY total_gastado DESC
                       LIMIT 5");
$clientesVip = [];
while($cv = $resVip->fetch_assoc()) {
    $clientesVip[] = [
        "nombre" => $cv['nombre'],
        "total" => "S/ " . number_format($cv['total_gastado'], 0)
    ];
}

echo json_encode([
    "success" => true,
    "ventas" => "S/ " . number_format($ventas / 1000, 1) . "k",
    "ganancia" => "S/ " . number_format($ganancia / 1000, 1) . "k",
    "pedidos" => (string)$pedidos,
    "usuarios" => (string)$usuarios,
    "stock" => $stockStatus,
    "top_productos" => $topProds,
    "clientes_vip" => $clientesVip
]);

$conn->close();
?>