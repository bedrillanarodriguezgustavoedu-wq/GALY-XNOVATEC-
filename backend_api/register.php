<?php
include 'db_config.php';

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['nombre']) && isset($data['email']) && isset($data['password'])) {
    $nombre = $data['nombre'];
    $email = $data['email'];
    $password = password_hash($data['password'], PASSWORD_DEFAULT);

    $check = $conn->prepare("SELECT id FROM usuarios WHERE email = ?");
    $check->bind_param("s", $email);
    $check->execute();
    if ($check->get_result()->num_rows > 0) {
        echo json_encode(["success" => false, "message" => "El correo ya está registrado"]);
    } else {
        $stmt = $conn->prepare("INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)");
        $stmt->bind_param("sss", $nombre, $email, $password);

        if ($stmt->execute()) {
            $id = $stmt->insert_id;
            echo json_encode([
                "success" => true,
                "message" => "Usuario registrado correctamente",
                "user" => [
                    "id" => $id,
                    "nombre" => $nombre,
                    "email" => $email
                ]
            ]);
        } else {
            echo json_encode(["success" => false, "message" => "Error al registrar usuario"]);
        }
    }
} else {
    echo json_encode(["success" => false, "message" => "Faltan datos requeridos"]);
}

$conn->close();
?>