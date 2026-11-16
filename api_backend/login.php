<?php
include 'db_config.php';

$data = json_decode(file_get_contents('php://input'), true);

if (isset($data['id_token']) && is_string($data['id_token'])) {
    $clientId = GALY_GOOGLE_WEB_CLIENT_ID;
    if ($clientId === '') {
        http_response_code(503);
        echo json_encode(["success" => false, "message" => "El inicio con Google no está configurado en el servidor."]);
        exit;
    }

    $verificationUrl = 'https://oauth2.googleapis.com/tokeninfo?id_token=' . rawurlencode($data['id_token']);
    $curl = curl_init($verificationUrl);
    curl_setopt_array($curl, [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 10,
        CURLOPT_CONNECTTIMEOUT => 5,
    ]);
    $tokenInfoJson = curl_exec($curl);
    $curlError = curl_errno($curl);
    $httpCode = (int)curl_getinfo($curl, CURLINFO_HTTP_CODE);
    curl_close($curl);

    if ($curlError !== 0 || $httpCode !== 200 || !is_string($tokenInfoJson)) {
        http_response_code(401);
        echo json_encode(["success" => false, "message" => "No se pudo validar la sesión de Google."]);
        exit;
    }

    $tokenInfo = json_decode($tokenInfoJson, true);
    $email = strtolower(trim((string)($tokenInfo['email'] ?? '')));
    $issuer = (string)($tokenInfo['iss'] ?? '');
    $expiresAt = (int)($tokenInfo['exp'] ?? 0);
    $verifiedEmail = filter_var($tokenInfo['email_verified'] ?? false, FILTER_VALIDATE_BOOLEAN);

    if (
        !is_array($tokenInfo)
        || !hash_equals($clientId, (string)($tokenInfo['aud'] ?? ''))
        || !in_array($issuer, ['accounts.google.com', 'https://accounts.google.com'], true)
        || $expiresAt <= time()
        || !$verifiedEmail
        || !filter_var($email, FILTER_VALIDATE_EMAIL)
        || strlen($email) > 100
    ) {
        http_response_code(401);
        echo json_encode(["success" => false, "message" => "La cuenta de Google no pudo verificarse."]);
        exit;
    }

    $stmt = $conn->prepare("SELECT id, nombre, email, telefono, direccion, es_admin FROM usuarios WHERE LOWER(email) = ? LIMIT 1");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $user = $stmt->get_result()->fetch_assoc();

    if (!$user) {
        $name = trim((string)($tokenInfo['name'] ?? ''));
        if ($name === '') {
            $name = strstr($email, '@', true) ?: $email;
        }
        $name = function_exists('mb_substr') ? mb_substr($name, 0, 100, 'UTF-8') : substr($name, 0, 100);
        $generatedPassword = password_hash(bin2hex(random_bytes(32)), PASSWORD_DEFAULT);

        try {
            $create = $conn->prepare("INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)");
            $create->bind_param("sss", $name, $email, $generatedPassword);
            $created = $create->execute();
        } catch (mysqli_sql_exception $exception) {
            error_log("Google customer account insert failed: " . $exception->getMessage());
            $created = false;
        }

        if (!$created) {
            $stmt = $conn->prepare("SELECT id, nombre, email, telefono, direccion, es_admin FROM usuarios WHERE LOWER(email) = ? LIMIT 1");
            $stmt->bind_param("s", $email);
            $stmt->execute();
            $user = $stmt->get_result()->fetch_assoc();
        } else {
            $user = [
                'id' => (int)$conn->insert_id,
                'nombre' => $name,
                'email' => $email,
                'telefono' => null,
                'direccion' => null,
                'es_admin' => 0,
            ];
        }

        if (!$user) {
            http_response_code(500);
            echo json_encode(["success" => false, "message" => "No se pudo crear la cuenta cliente GALY. Inténtalo nuevamente."]);
            exit;
        }
    }

    $user['id'] = (int)$user['id'];
    if ((int)($user['es_admin'] ?? 0) === 1) {
        http_response_code(403);
        echo json_encode(["success" => false, "message" => "Los administradores deben iniciar sesión con su correo y contraseña."]);
        exit;
    }
    $user['es_admin'] = false;
    $response = [
        "success" => true,
        "message" => "Inicio con Google verificado",
        "user" => $user,
        "access_token" => $data['id_token'],
    ];
    echo json_encode($response);
} elseif (isset($data['email']) && isset($data['password'])) {
    $email = strtolower(trim((string)$data['email']));
    $password = $data['password'];

    $stmt = $conn->prepare("SELECT id, nombre, email, password, telefono, direccion, es_admin FROM usuarios WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        $user = $result->fetch_assoc();

        $password_is_hash = password_verify($password, $user['password']);
        $password_is_legacy_plaintext = hash_equals((string)$user['password'], $password);

        if ($password_is_hash || $password_is_legacy_plaintext) {
            if ($password_is_legacy_plaintext) {
                $password_hash = password_hash($password, PASSWORD_DEFAULT);
                $update_password = $conn->prepare("UPDATE usuarios SET password = ? WHERE id = ?");
                $update_password->bind_param("si", $password_hash, $user['id']);
                $update_password->execute();
            }

            unset($user['password']);
            $user['id'] = (int)$user['id'];
            $user['es_admin'] = (int)$user['es_admin'] == 1;
            echo json_encode([
                "success" => true,
                "message" => "Login exitoso",
                "access_token" => "galy_access_token_" . (int)$user['id'],
                "admin_token" => null,
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