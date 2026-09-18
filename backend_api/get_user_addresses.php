<?php
include 'db_config.php';
header('Content-Type: application/json');

$user_id = isset($_GET['usuario_id']) ? (int)$_GET['usuario_id'] : 0;

if ($user_id > 0) {
    $sql = "SELECT * FROM direcciones WHERE usuario_id = $user_id ORDER BY id DESC";
    $result = $conn->query($sql);
    $direcciones = [];
    while($row = $result->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        $row['usuario_id'] = (int)$row['usuario_id'];
        // Convertimos es_principal de 0/1 a boolean para la App
        $row['es_principal'] = ((int)$row['es_principal'] === 1);
        $direcciones[] = $row;
    }
    echo json_encode($direcciones);
} else {
    echo json_encode([]);
}
$conn->close();
?>