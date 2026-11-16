<?php
require_once 'db.php';
$pedido_id = isset($_GET['pedido_id']) ? (int)$_GET['pedido_id'] : 0;
?>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Finalizar Compra - GALY XNOVATEC</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        :root { --galy-red: #E30613; --bg-dark: #000000; --card-dark: #0a0a0a; }
        body { background-color: var(--bg-dark); color: white; font-family: 'Inter', sans-serif; }
        .card-custom { background: var(--card-dark); border: 1px solid rgba(255,255,255,0.08); border-radius: 20px; }
        .btn-galy { background: var(--galy-red); color: white; border: none; border-radius: 12px; padding: 14px; font-weight: bold; width: 100%; }
        .btn-galy:hover { background: #b3050f; color: white; }
        .payment-method-card { background: #111; border: 2px solid #333; border-radius: 16px; padding: 16px; cursor: pointer; transition: 0.3s; }
        .payment-method-card:hover, .payment-method-card.active { border-color: var(--galy-red); background: #180809; }
    </style>
</head>
<body>
<nav class="navbar navbar-dark bg-black p-3 border-bottom border-secondary border-opacity-25">
    <div class="container">
        <a class="navbar-brand fw-bold" href="index.php">
            <img src="img/logo_2.png" width="40" class="me-2"> GALY <span class="text-danger">XNOVATEC</span>
        </a>
    </div>
</nav>

<div class="container my-5" style="max-width: 800px;">
    <h2 class="fw-black mb-4">Finaliza tu compra</h2>
    <div class="card-custom p-4 mb-4">
        <h4 class="fw-bold mb-3">Medio de pago</h4>
        <p class="text-secondary small mb-4">Selecciona cómo deseas pagar tu pedido. El pago con tarjeta es automático y seguro a través de Mercado Pago.</p>

        <div class="row g-3">
            <div class="col-md-6">
                <div class="payment-method-card" onclick="selectMethod('YAPE')">
                    <h5 class="fw-bold text-white mb-1"><i class="bi bi-phone me-2 text-danger"></i> Yape</h5>
                    <p class="text-secondary small mb-0">Transferencia manual</p>
                </div>
            </div>
            <div class="col-md-6">
                <div class="payment-method-card active" onclick="selectMethod('CARD')" id="cardOption">
                    <h5 class="fw-bold text-white mb-1"><i class="bi bi-credit-card me-2 text-danger"></i> Tarjeta Visa o Mastercard</h5>
                    <p class="text-success small mb-0"><i class="bi bi-check-circle me-1"></i> Disponible y activo</p>
                </div>
            </div>
        </div>

        <form id="checkoutForm" action="web_payment.php" method="GET" class="mt-4">
            <input type="hidden" name="pedido_id" value="<?php echo $pedido_id; ?>">
            <div class="mb-3">
                <label class="form-label text-secondary small">ID de Pedido o Simulación</label>
                <input type="number" class="form-control bg-dark text-white border-secondary" name="pedido_id" value="<?php echo $pedido_id ? $pedido_id : 1; ?>" required>
            </div>
            <button type="submit" class="btn btn-galy mt-3">PROCESAR PAGO CON TARJETA</button>
        </form>
    </div>
</div>

<script>
function selectMethod(method) {
    if(method === 'CARD') {
        document.getElementById('cardOption').classList.add('active');
    }
}
</script>
</body>
</html>
