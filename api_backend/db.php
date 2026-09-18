<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "galy";

$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Fallo de conexión"]));
}
?>