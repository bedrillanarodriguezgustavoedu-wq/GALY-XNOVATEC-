<?php
include 'db_config.php';
header('Content-Type: application/json');

if (isset($_GET['usuario_id'])) {
    $uid = (int)$_GET['usuario_id'];
    $sql = "SELECT * FROM servicios_tecnicos WHERE usuario_id = $uid ORDER BY id DESC";
    $result = $conn->query($sql);

    $tickets = [];
    while($row = $result->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        $row['usuario_id'] = (int)$row['usuario_id'];
        $row['costo_estimado'] = (float)$row['costo_estimado'];
        $tickets[] = $row;
    }
    echo json_encode($tickets);
} else {
    echo json_encode([]);
}
$conn->close();
?>