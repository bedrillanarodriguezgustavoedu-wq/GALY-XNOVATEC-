<?php
include 'db_config.php';

$sql = "SELECT id, nombre FROM categorias";
$result = $conn->query($sql);

$categorias = array();

if ($result && $result->num_rows > 0) {
    while($row = $result->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        $categorias[] = $row;
    }
}

header('Content-Type: application/json');
echo json_encode($categorias);
$conn->close();
?>