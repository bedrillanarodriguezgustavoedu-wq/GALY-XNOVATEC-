<?php
header('Content-Type: application/json; charset=utf-8');

// --- CONFIGURACIÓN DE MERCADO PAGO ---
// Reemplaza esto con tu Access Token Real de Mercado Pago (Panel de Desarrollador)
$access_token = "TEST-8514936306504285-091415-3733c7f9999999999999999999999";

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