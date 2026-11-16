<?php
require_once 'db.php';
$pedido_id = isset($_GET['pedido_id']) ? (int)$_GET['pedido_id'] : 0;
if ($pedido_id > 0) {
    $conn->query("UPDATE pedidos SET estado = 'PAGADO' WHERE id = $pedido_id");
}
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Pago Exitoso - GALY XNOVATEC</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
</head>
<body class="bg-black text-white text-center py-5">
    <div class="container py-5">
        <div class="card bg-dark text-white p-5 border-success mx-auto" style="max-width: 500px;">
            <i class="bi bi-check-circle-fill text-success display-1 mb-3"></i>
            <h1 class="fw-bold">¡Pago Exitoso!</h1>
            <p class="text-secondary">Tu pedido #<?php echo $pedido_id; ?> ha sido pagado y confirmado correctamente.</p>
            <a href="index.php" class="btn btn-danger mt-3">Volver a la Tienda</a>
        </div>
    </div>
</body>
</html>
