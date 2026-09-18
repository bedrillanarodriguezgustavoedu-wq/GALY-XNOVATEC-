<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['id'])) {
    $id = (int)$data['id'];
    $nombre = $conn->real_escape_string($data['nombre']);
    $marca = $conn->real_escape_string($data['marca']);
    $precio_normal = (float)$data['precio_normal'];
    $precio_oferta = (float)$data['precio_oferta'];
    $precio_compra = (float)($data['precio_compra'] ?? 0.0);
    $descripcion = $conn->real_escape_string($data['descripcion']);
    $especificaciones = $conn->real_escape_string($data['especificaciones']);
    $variantes = isset($data['variantes']) ? $conn->real_escape_string($data['variantes']) : '';

    $imgs_sec = '';
    if (isset($data['imagenes_secundarias'])) {
        if (is_array($data['imagenes_secundarias'])) {
            $imgs_sec = implode(',', $data['imagenes_secundarias']);
        } else {
            $imgs_sec = $data['imagenes_secundarias'];
        }
    }
    $imagenes_secundarias = $conn->real_escape_string($imgs_sec);

    $stock = (int)$data['stock'];

    $sql = "UPDATE productos SET
            nombre = '$nombre',
            marca = '$marca',
            precio_normal = $precio_normal,
            precio_oferta = $precio_oferta,
            descripcion = '$descripcion',
            especificaciones = '$especificaciones',
            variantes = '$variantes',
            imagenes_secundarias = '$imagenes_secundarias',
            stock = $stock,
            precio_compra = $precio_compra";

    if (isset($data['imagen_url']) && !empty($data['imagen_url'])) {
        $imagen_url = $conn->real_escape_string($data['imagen_url']);
        $sql .= ", imagen_url = '$imagen_url'";
    }

    $sql .= " WHERE id = $id";

    if ($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Producto actualizado"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al actualizar: " . $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "ID no proporcionado"]);
}

$conn->close();
?>