<?php
require_once __DIR__ . '/db_config.php';
header('Content-Type: application/json; charset=utf-8');

$data = json_decode(file_get_contents('php://input'), true);
$image = trim((string) ($data['imagen_url'] ?? ''));

if ($image === '') {
    http_response_code(422);
    echo json_encode(['success' => false, 'message' => 'Imagen requerida.']);
    $conn->close();
    exit;
}

$title = trim((string) ($data['titulo'] ?? ''));
$subtitle = trim((string) ($data['subtitulo'] ?? ''));
$link = trim((string) ($data['enlace'] ?? $data['link'] ?? ''));
$background = trim((string) ($data['color_fondo'] ?? '#FF0055'));
$order = max(0, (int) ($data['orden'] ?? 0));

$statement = $conn->prepare(
    'INSERT INTO banners (imagen, titulo, subtitulo, enlace, color_fondo, orden, activo, created_at, updated_at)
     VALUES (?, ?, ?, ?, ?, ?, 1, NOW(), NOW())'
);

if (!$statement) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'No se pudo preparar el registro del banner.']);
    $conn->close();
    exit;
}

$statement->bind_param('sssssi', $image, $title, $subtitle, $link, $background, $order);
$success = $statement->execute();

if (!$success) {
    http_response_code(500);
}

echo json_encode([
    'success' => $success,
    'message' => $success ? 'Banner creado correctamente.' : 'No se pudo crear el banner.',
    'id' => $success ? $conn->insert_id : null,
]);

$statement->close();
$conn->close();
