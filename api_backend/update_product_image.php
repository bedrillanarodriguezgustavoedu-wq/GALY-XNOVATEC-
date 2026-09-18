<?php
include 'db_config.php';
header('Content-Type: application/json');
$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['id']) && isset($data['imagen_url'])) {
    $id = (int)$data['id'];
    $img = $conn->real_escape_string($data['imagen_url']);

    $sql = "UPDATE productos SET imagen_url = '$img' WHERE id = $id";
    if ($conn->query($sql)) {
        echo json_encode(["success" => true]);
    } else {
        echo json_encode(["success" => false, "message" => $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Datos incompletos"]);
}
$conn->close();
?>