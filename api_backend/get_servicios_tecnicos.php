<?php
include 'db_config.php';

$sql = "SELECT id, nombre, descripcion, precio, icono FROM catalogo_servicios";
$result = $conn->query($sql);

$servicios = array();

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        $row['precio'] = (float)$row['precio'];
        $servicios[] = $row;
    }
}

header('Content-Type: application/json');
echo json_encode($servicios);
$conn->close();
?>