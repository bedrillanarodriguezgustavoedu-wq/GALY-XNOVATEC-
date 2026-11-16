<?php
$host = "localhost";
$user = "root";
$pass = "";
$db = "ecommerce";

// LLAVE DE SEGURIDAD GALY (API KEY)
define('GALY_API_KEY', (string)(getenv('GALY_API_KEY') ?: ''));
define('GALY_SSO_SECRET', (string)(getenv('GALY_SSO_SECRET') ?: ''));
define('GALY_GOOGLE_WEB_CLIENT_ID', (string)(getenv('GALY_GOOGLE_WEB_CLIENT_ID') ?: ''));
if (strlen(GALY_SSO_SECRET) < 32) { http_response_code(503); exit('Configure GALY_SSO_SECRET for this legacy backend.'); }

function galy_base64url_encode(string $value): string
{
    return rtrim(strtr(base64_encode($value), '+/', '-_'), '=');
}

function galy_create_admin_token(int $user_id, string $email): string
{
    $payload = galy_base64url_encode(json_encode([
        'user_id' => $user_id,
        'email' => $email,
        'exp' => time() + 300,
    ], JSON_THROW_ON_ERROR));
    $signature = galy_base64url_encode(hash_hmac('sha256', $payload, GALY_SSO_SECRET, true));
    return $payload . '.' . $signature;
}

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Error de conexión: " . $conn->connect_error]));
}
$conn->set_charset("utf8");

// VERIFICACIÓN DE SEGURIDAD PARA LA APP
// Solo verificamos si NO estamos en el panel administrativo web
if (basename($_SERVER['PHP_SELF']) != 'admin_web.php') {
    $headers = getallheaders();
    $client_key = isset($headers['X-GALY-KEY']) ? $headers['X-GALY-KEY'] : (isset($headers['x-galy-key']) ? $headers['x-galy-key'] : '');

    if ($client_key !== GALY_API_KEY) {
        header('HTTP/1.1 403 Forbidden');
        echo json_encode(["success" => false, "message" => "Acceso denegado: Llave maestra inválida"]);
        exit;
    }
}
?>