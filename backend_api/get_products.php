<?php
error_reporting(0);
ini_set('display_errors', 0);
header('Content-Type: application/json; charset=utf-8');
include 'db_config.php';

$sql = "SELECT * FROM productos ORDER BY id ASC";
$result = $conn->query($sql);
$productos = [];

if ($result) {
    while($row = $result->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        $row['precio_normal'] = (float)$row['precio_normal'];
        $row['precio_oferta'] = (float)$row['precio_oferta'];
        $row['descuento'] = (int)$row['descuento'];
        $row['calificacion'] = (float)$row['calificacion'];
        $row['num_revisiones'] = (int)$row['num_revisiones'];
        $row['disponible'] = ((int)$row['disponible'] === 1);
        $row['tiene_envio_gratis'] = ((int)$row['tiene_envio_gratis'] === 1);

        // Limpiamos espacios en blanco accidentales en los nombres de archivos
        if ($row['imagen_url']) $row['imagen_url'] = trim($row['imagen_url']);
        if ($row['categoria_nombre']) $row['categoria_nombre'] = trim($row['categoria_nombre']);

        $productos[] = $row;
    }
}
echo json_encode($productos, JSON_UNESCAPED_UNICODE);
$conn->close();
?>