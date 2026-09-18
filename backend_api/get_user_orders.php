<?php
include 'db_config.php';
header('Content-Type: application/json');

$usuario_id = isset($_GET['usuario_id']) ? (int)$_GET['usuario_id'] : 0;

if ($usuario_id > 0) {
    // 1. Obtener los pedidos del usuario con info completa
    $sql = "SELECT * FROM pedidos WHERE usuario_id = $usuario_id ORDER BY id DESC";
    $result = $conn->query($sql);

    $pedidos = [];
    while($row = $result->fetch_assoc()) {
        $pedido_id = $row['id'];
        $row['id'] = (int)$row['id'];
        $row['total'] = (float)$row['total'];

        // 2. Para cada pedido, obtener sus productos
        $sqlProd = "SELECT pp.cantidad, pp.precio, p.nombre, p.imagen_url
                    FROM pedido_productos pp
                    JOIN productos p ON pp.producto_id = p.id
                    WHERE pp.pedido_id = $pedido_id";
        $resProd = $conn->query($sqlProd);

        $productos = [];
        while($p = $resProd->fetch_assoc()) {
            $p['cantidad'] = (int)$p['cantidad'];
            $p['precio'] = (float)$p['precio'];
            $productos[] = $p;
        }

        $row['productos'] = $productos;
        $pedidos[] = $row;
    }
    echo json_encode($pedidos);
} else {
    echo json_encode([]);
}

$conn->close();
?>
