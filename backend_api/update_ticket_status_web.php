<?php
include 'db_config.php';
include 'fcm_helper.php';

if (isset($_POST['id'])) {
    $id = (int)$_POST['id'];
    $estado = $conn->real_escape_string($_POST['estado']);
    $costo = (float)$_POST['costo'];

    $sql = "UPDATE servicios_tecnicos SET estado = '$estado', costo_estimado = $costo";

    // Subir imagen de ingreso (antes)
    if (isset($_FILES['img_ingreso']) && $_FILES['img_ingreso']['error'] == 0) {
        $name = "t_in_" . $id . "_" . time() . ".jpg";
        if (move_uploaded_file($_FILES['img_ingreso']['tmp_name'], "img/" . $name)) {
            $sql .= ", imagen_ingreso = '$name'";
        }
    }

    // Subir imagen de listo (después)
    if (isset($_FILES['img_listo']) && $_FILES['img_listo']['error'] == 0) {
        $name = "t_out_" . $id . "_" . time() . ".jpg";
        if (move_uploaded_file($_FILES['img_listo']['tmp_name'], "img/" . $name)) {
            $sql .= ", imagen_listo = '$name'";
        }
    }

    $sql .= " WHERE id = $id";

    if ($conn->query($sql)) {
        // Enviar notificación Real
        $res = $conn->query("SELECT u.fcm_token, u.nombre, t.equipo_modelo FROM servicios_tecnicos t JOIN usuarios u ON t.usuario_id = u.id WHERE t.id = $id");
        $row = $res->fetch_assoc();
        if ($row && !empty($row['fcm_token'])) {
            $title = "Soporte Técnico GALY";
            $body = "¡Hola {$row['nombre']}! Tu {$row['equipo_modelo']} ya está en estado: $estado.";
            sendFCMNotification($row['fcm_token'], $title, $body);
        }
        file_put_contents('fcm_log.txt', "[" . date('Y-m-d H:i:s') . "] TICKET #$id ACTUALIZADO WEB A: $estado\n", FILE_APPEND);
    }
}
header("Location: admin_web.php?section=soporte");
?>