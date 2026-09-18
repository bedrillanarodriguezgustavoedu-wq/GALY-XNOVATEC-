<?php
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST' && isset($_FILES['image'])) {
    $target_dir = "img/";
    if (!file_exists($target_dir)) {
        mkdir($target_dir, 0777, true);
    }

    $file_extension = pathinfo($_FILES["image"]["name"], PATHINFO_EXTENSION);
    $new_filename = "galy_" . time() . "_" . uniqid() . "." . $file_extension;
    $target_file = $target_dir . $new_filename;

    if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_file)) {
        echo json_encode([
            "success" => true,
            "message" => "Imagen subida correctamente",
            "filename" => $new_filename,
            "url" => $new_filename
        ]);
    } else {
        echo json_encode([
            "success" => false,
            "message" => "Error al mover el archivo subido"
        ]);
    }
} else {
    echo json_encode([
        "success" => false,
        "message" => "No se recibió ninguna imagen"
    ]);
}
?>