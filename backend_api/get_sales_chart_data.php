<?php
include 'db_config.php';
header('Content-Type: application/json');

// Obtener ventas de los últimos 7 días
$sql = "SELECT DATE(fecha) as dia, SUM(total) as total
        FROM pedidos
        WHERE estado != 'CANCELADO'
        AND fecha >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        GROUP BY DATE(fecha)
        ORDER BY dia ASC";

$result = $conn->query($sql);
$data = [];

// Rellenar con ceros si no hay ventas en algunos días
$period = new DatePeriod(
    new DateTime('-6 days'),
    new DateInterval('P1D'),
    new DateTime('+1 day')
);

foreach ($period as $date) {
    $data[$date->format('Y-m-d')] = 0;
}

if ($result) {
    while($row = $result->fetch_assoc()) {
        $data[$row['dia']] = (float)$row['total'];
    }
}

$finalData = [];
foreach ($data as $dia => $total) {
    $finalData[] = [
        "dia" => $dia,
        "total" => $total,
        "label" => date('D', strtotime($dia)) // Nombre corto del día
    ];
}

echo json_encode($finalData);
$conn->close();
?>