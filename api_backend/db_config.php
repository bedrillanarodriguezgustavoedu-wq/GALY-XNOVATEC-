<?php
$host = "localhost";
$user = "root";
$pass = "";
$db = "galy";

// LLAVE DE SEGURIDAD GALY (API KEY)
define('GALY_API_KEY', 'galy_master_key_2026');

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