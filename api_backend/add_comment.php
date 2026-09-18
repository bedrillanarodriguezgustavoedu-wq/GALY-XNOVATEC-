<?php
header('Content-Type: application/json');
include 'db.php';

$data = json_decode(file_get_contents('php://input'), true);
if (!$data) {
    echo json_encode(["success" => false, "message" => "Datos inválidos"]);
    exit;
}

$producto_id = $data['producto_id'];
$usuario_id = $data['usuario_id'];
$comentario = $data['comentario'];
$estrellas = $data['estrellas'];

$sql = "INSERT INTO comentarios (producto_id, usuario_id, comentario, estrellas)
        VALUES ('$producto_id', '$usuario_id', '$comentario', '$estrellas')";

if ($conn->query($sql)) {
    echo json_encode(["success" => true, "message" => "Comentario añadido"]);
} else {
    echo json_encode(["success" => false, "message" => "Error al guardar comentario"]);
}

$conn->close();
?>