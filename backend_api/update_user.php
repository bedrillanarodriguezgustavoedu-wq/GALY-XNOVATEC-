<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['id'])) {
    $id = (int)$data['id'];
    $nombre = isset($data['nombre']) ? $data['nombre'] : null;
    $email = isset($data['email']) ? $data['email'] : null;
    $telefono = isset($data['telefono']) ? $data['telefono'] : null;
    $direccion = isset($data['direccion']) ? $data['direccion'] : null;
    $password = isset($data['password']) ? $data['password'] : null;

    $updates = [];
    $types = "";
    $params = [];

    if ($nombre) { $updates[] = "nombre = ?"; $types .= "s"; $params[] = $nombre; }
    if ($email) { $updates[] = "email = ?"; $types .= "s"; $params[] = $email; }
    if ($telefono) { $updates[] = "telefono = ?"; $types .= "s"; $params[] = $telefono; }
    if ($direccion) { $updates[] = "direccion = ?"; $types .= "s"; $params[] = $direccion; }
    if ($password) {
        $hashed_password = password_hash($password, PASSWORD_DEFAULT);
        $updates[] = "password = ?"; $types .= "s"; $params[] = $hashed_password;
    }

    if (empty($updates)) {
        echo json_encode(["success" => false, "message" => "No hay datos para actualizar"]);
        exit;
    }

    $sql = "UPDATE usuarios SET " . implode(", ", $updates) . " WHERE id = ?";
    $types .= "i";
    $params[] = $id;

    $stmt = $conn->prepare($sql);
    $stmt->bind_param($types, ...$params);

    if ($stmt->execute()) {
        // Obtener datos actualizados
        $res = $conn->query("SELECT id, nombre, email, telefono, direccion, es_admin FROM usuarios WHERE id = $id");
        $user = $res->fetch_assoc();
        $user['id'] = (int)$user['id'];
        $user['es_admin'] = (int)$user['es_admin'] == 1;

        echo json_encode([
            "success" => true,
            "message" => "Perfil actualizado correctamente",
            "user" => $user
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al actualizar perfil"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "ID de usuario requerido"]);
}

$conn->close();
?>