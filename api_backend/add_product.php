<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['nombre'])) {
    $nombre = $conn->real_escape_string($data['nombre']);
    $marca = $conn->real_escape_string($data['marca']);
    $categoria = $conn->real_escape_string($data['categoria']);
    $precio_normal = (float)$data['precio_normal'];
    $precio_oferta = (float)$data['precio_oferta'];
    $precio_compra = (float)($data['precio_compra'] ?? 0.0);
    $imagen_url = isset($data['imagen_url']) ? $conn->real_escape_string($data['imagen_url']) : '';
    $descripcion = isset($data['descripcion']) ? $conn->real_escape_string($data['descripcion']) : '';
    $descuento = (int)$data['descuento'];
    $etiqueta_especial = isset($data['etiqueta_especial']) ? $conn->real_escape_string($data['etiqueta_especial']) : '';
    $especificaciones = isset($data['especificaciones']) ? $conn->real_escape_string($data['especificaciones']) : '';
    $variantes = isset($data['variantes']) ? $conn->real_escape_string($data['variantes']) : '';

    $imagenes_secundarias = '';
    if (isset($data['imagenes_secundarias'])) {
        if (is_array($data['imagenes_secundarias'])) {
            $imagenes_secundarias = implode(',', $data['imagenes_secundarias']);
        } else {
            $imagenes_secundarias = $data['imagenes_secundarias'];
        }
    }
    $imagenes_secundarias = $conn->real_escape_string($imagenes_secundarias);

    $stock = isset($data['stock']) ? (int)$data['stock'] : 10;

    // Nombres de columnas según la BD
    $sql = "INSERT INTO productos (nombre, marca, categoria_nombre, precio_normal, precio_oferta, precio_compra, imagen_url, descripcion, descuento, etiqueta_especial, stock, especificaciones, variantes, imagenes_secundarias)
            VALUES ('$nombre', '$marca', '$categoria', $precio_normal, $precio_oferta, $precio_compra, '$imagen_url', '$descripcion', $descuento, '$etiqueta_especial', $stock, '$especificaciones', '$variantes', '$imagenes_secundarias')";

    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Producto añadido", "id" => $conn->insert_id]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al añadir: " . $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Datos incompletos"]);
}

$conn->close();
?>