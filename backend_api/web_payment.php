<?php
require_once 'db.php';

$pedido_id = isset($_GET['pedido_id']) ? (int)$_GET['pedido_id'] : 0;
if ($pedido_id <= 0) {
    die("Pedido inválido");
}

$res = $conn->query("SELECT * FROM pedidos WHERE id = $pedido_id");
if (!$res || $res->num_rows === 0) {
    die("Pedido no encontrado");
}
$pedido = $res->fetch_assoc();

$items_res = $conn->query("SELECT pi.*, p.nombre FROM pedido_items pi JOIN productos p ON pi.producto_id = p.id WHERE pi.pedido_id = $pedido_id");
$items_mp = [];
while ($item = $items_res->fetch_assoc()) {
    $items_mp[] = [
        "title" => $item['nombre'],
        "quantity" => (int)$item['cantidad'],
        "unit_price" => (float)$item['precio'],
        "currency_id" => "PEN"
    ];
}

$access_token = getenv('MERCADOPAGO_ACCESS_TOKEN');
if (!$access_token) {
    $env_paths = ['.env', '../.env', '../../.env', __DIR__ . '/.env', __DIR__ . '/../.env'];
    foreach ($env_paths as $env_path) {
        if (file_exists($env_path)) {
            $lines = file($env_path, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
            foreach ($lines as $line) {
                if (strpos(trim($line), '#') === 0) continue;
                if (strpos($line, 'MERCADOPAGO_ACCESS_TOKEN=') === 0) {
                    $access_token = trim(substr($line, strlen('MERCADOPAGO_ACCESS_TOKEN=')));
                    $access_token = trim($access_token, '"\'');
                    break 2;
                }
            }
        }
    }
}
if (!$access_token) {
    $access_token = "TEST-8514936306504285-091415-3733c7f9999999999999999999999";
}

$preference_data = [
    "items" => $items_mp,
    "external_reference" => (string)$pedido_id,
    "back_urls" => [
        "success" => "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . "/success.php?pedido_id=$pedido_id",
        "failure" => "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . "/index.php",
        "pending" => "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . "/index.php"
    ],
    "auto_return" => "approved"
];

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, "https://api.mercadopago.com/checkout/preferences");
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    "Content-Type: application/json",
    "Authorization: Bearer $access_token"
]);
curl_setopt($ch, CURLOPT_POST, 1);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($preference_data));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$response = curl_exec($ch);
$http_code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

if ($http_code == 201 || $http_code == 200) {
    $res_data = json_decode($response, true);
    $init_point = $res_data['init_point'];
    header("Location: $init_point");
    exit;
} else {
    echo "Error al conectar con Mercado Pago. Verifica tus credenciales. Respuesta: " . htmlspecialchars($response);
}
?>
