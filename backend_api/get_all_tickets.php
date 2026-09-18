<?php
include 'db_config.php';
header('Content-Type: application/json');

$sql = "SELECT t.*, u.nombre as cliente_nombre
        FROM servicios_tecnicos t
        JOIN usuarios u ON t.usuario_id = u.id
        ORDER BY t.id DESC";
$result = $conn->query($sql);

$tickets = [];
while($row = $result->fetch_assoc()) {
    $row['id'] = (int)$row['id'];
    $row['usuario_id'] = (int)$row['usuario_id'];
    $row['costo_estimado'] = (float)$row['costo_estimado'];
    $tickets[] = $row;
}
echo json_encode($tickets);
$conn->close();
?>