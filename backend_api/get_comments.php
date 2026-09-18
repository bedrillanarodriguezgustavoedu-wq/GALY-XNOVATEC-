<?php
header('Content-Type: application/json');
include 'db.php';

$producto_id = $_GET['producto_id'];

$sql = "SELECT c.*, u.nombre as usuario_nombre
        FROM comentarios c
        JOIN usuarios u ON c.usuario_id = u.id
        WHERE c.producto_id = '$producto_id'
        ORDER BY c.fecha DESC";

$result = $conn->query($sql);
$comentarios = array();

if ($result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $row['estrellas'] = (int)$row['estrellas'];
        $comentarios[] = $row;
    }
}

echo json_encode($comentarios);
$conn->close();
?>