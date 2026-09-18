<?php
include 'db_config.php';

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['email']) && isset($data['password'])) {
    $email = $data['email'];
    $password = $data['password'];

    $stmt = $conn->prepare("SELECT id, nombre, email, password, telefono, direccion, es_admin FROM usuarios WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();

        if (password_verify($password, $user['password'])) {
            unset($user['password']);
            $user['id'] = (int)$user['id'];
            $user['es_admin'] = (int)$user['es_admin'] == 1;
            echo json_encode([
                "success" => true,
                "message" => "Login exitoso",
                "user" => $user
            ]);
        } else {
            echo json_encode(["success" => false, "message" => "Contraseña incorrecta"]);
        }
    } else {
        echo json_encode(["success" => false, "message" => "El usuario no existe"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Datos incompletos"]);
}

$conn->close();
?>