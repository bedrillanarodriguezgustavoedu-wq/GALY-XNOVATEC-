<?php
header('Content-Type: application/json; charset=utf-8');

// --- CONFIGURACIÓN DE MERCADO PAGO ---
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

$data = json_decode(file_get_contents('php://input'), true);

if (!$data) {
    echo json_encode(["success" => false, "message" => "Datos inválidos"]);
    exit;
}

$items_carrito = $data['items']; // Lista de productos
$total = $data['total'];
$pedido_id = $data['pedido_id'];

// Preparar los items para Mercado Pago
$items_mp = [];
foreach ($items_carrito as $item) {
    $items_mp[] = [
        "title" => $item['nombre'],
        "quantity" => (int)$item['cantidad'],
        "unit_price" => (float)$item['precio'],
        "currency_id" => "PEN"
    ];
}

$preference_data = [
    "items" => $items_mp,
    "external_reference" => (string)$pedido_id,
    "back_urls" => [
        "success" => "https://www.google.com/success", // URLs de retorno temporales
        "failure" => "https://www.google.com/failure",
        "pending" => "https://www.google.com/pending"
    ],
    "auto_return" => "approved",
    "notification_url" => "https://tudominio.com/galy_api/mp_webhook.php" // Opcional para el futuro
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
    echo json_encode([
        "success" => true,
        "init_point" => $res_data['init_point'] // Esta es la URL que abrirá la App
    ]);
} else {
    echo json_encode([
        "success" => false,
        "message" => "Error de Mercado Pago: " . $response
    ]);
}
?>