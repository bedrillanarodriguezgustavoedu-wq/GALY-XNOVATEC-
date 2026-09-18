<?php
include 'db_config.php';
header('Content-Type: application/json');
$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['usuario_id']) && isset($data['direccion'])) {
    $user_id = (int)$data['usuario_id'];
    $dir = $conn->real_escape_string($data['direccion']);
    $dis = isset($data['distrito']) ? $conn->real_escape_string($data['distrito']) : "";
    $ciu = $conn->real_escape_string($data['ciudad']);
    $ref = isset($data['referencia']) ? $conn->real_escape_string($data['referencia']) : "";

    // Verificamos si existe la columna maps_url, si no, la ignoramos o la creamos
    $maps = isset($data['maps_url']) ? $conn->real_escape_string($data['maps_url']) : "";

    // Insertamos según la estructura real de tu imagen
    $sql = "INSERT INTO direcciones (usuario_id, direccion, distrito, ciudad, referencia)
            VALUES ($user_id, '$dir', '$dis', '$ciu', '$ref')";

    if ($conn->query($sql)) {
        // También actualizamos la dirección principal del usuario en la tabla usuarios
        $conn->query("UPDATE usuarios SET direccion = '$dir' WHERE id = $user_id");
        echo json_encode(["success" => true]);
    } else {
        echo json_encode(["success" => false, "message" => $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Datos incompletos"]);
}
$conn->close();
?>