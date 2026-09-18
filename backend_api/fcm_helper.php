<?php
function sendFCMNotification($token, $title, $body) {
    if (empty($token)) return false;

    // NOTA: Esta es la URL antigua de FCM (Legacy).
    // Para la nueva (v1), se necesita un archivo JSON de cuenta de servicio.
    // Usaremos la Legacy por simplicidad en local, pero requiere la 'Server Key'.
    $url = 'https://fcm.googleapis.com/fcm/send';

    // REEMPLAZA ESTO con tu 'Server Key' de Firebase (Configuración del proyecto -> Cloud Messaging)
    $serverKey = 'AAAA_TU_SERVER_KEY_AQUI';

    $notification = [
        'title' => $title,
        'body' => $body,
        'sound' => 'default',
        'badge' => '1'
    ];

    $payload = [
        'to' => $token,
        'notification' => $notification,
        'priority' => 'high'
    ];

    $headers = [
        'Authorization: key=' . $serverKey,
        'Content-Type: application/json'
    ];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));

    $result = curl_exec($ch);
    curl_close($ch);

    return $result;
}
?>