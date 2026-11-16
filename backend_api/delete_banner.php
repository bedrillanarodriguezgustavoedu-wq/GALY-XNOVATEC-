<?php
require_once __DIR__ . '/db_config.php';
header('Content-Type: application/json; charset=utf-8');

$data = json_decode(file_get_contents('php://input'), true);
$id = (int) ($data['id'] ?? 0);

if ($id <= 0) {
    http_response_code(422);
    echo json_encode(['success' => false, 'message' => 'ID válido requerido.']);
    $conn->close();
    exit;
}

$statement = $conn->prepare('DELETE FROM banners WHERE id = ?');
if (!$statement) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'No se pudo preparar la eliminación del banner.']);
    $conn->close();
    exit;
}

$statement->bind_param('i', $id);
$success = $statement->execute();
if (!$success) {
    http_response_code(500);
} elseif ($statement->affected_rows === 0) {
    http_response_code(404);
}

echo json_encode([
    'success' => $success && $statement->affected_rows > 0,
    'message' => $success && $statement->affected_rows > 0
        ? 'Banner eliminado.'
        : ($success ? 'No se encontró el banner.' : 'No se pudo eliminar el banner.'),
]);

$statement->close();
$conn->close();
