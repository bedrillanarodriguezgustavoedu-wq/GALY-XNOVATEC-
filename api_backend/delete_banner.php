<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['id'])) {
    $id = (int)$data['id'];
    $sql = "DELETE FROM banners_inicio WHERE id = $id";
    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Banner eliminado"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al eliminar"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "ID requerido"]);
}
$conn->close();
?>