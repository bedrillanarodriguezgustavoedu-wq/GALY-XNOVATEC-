<?php
include 'db_config.php';
header('Content-Type: application/json');

$res = $conn->query("SELECT id, nombre, email, telefono, direccion, imagen_url, es_admin, puntos FROM usuarios ORDER BY id DESC");
$users = [];
while($row = $res->fetch_assoc()) {
    $row['es_admin'] = (bool)$row['es_admin'];
    $row['puntos'] = (int)$row['puntos'];
    $users[] = $row;
}
echo json_encode($users);
$conn->close();
?>