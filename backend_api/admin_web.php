<?php
session_start();
require_once 'db.php';

// Desactivar errores visibles que ensucian la pantalla, pero guardarlos en log
error_reporting(E_ALL);
ini_set('display_errors', 0);

// --- Lógica de Login ---
if (isset($_POST['login'])) {
    $email = $_POST['email'];
    $pass = $_POST['password'];
    $stmt = $conn->prepare("SELECT * FROM usuarios WHERE email = ? AND password = ? AND es_admin = 1");
    $stmt->bind_param("ss", $email, $pass);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        $_SESSION['admin_logged'] = true;
        $_SESSION['admin_email'] = $email;
    } else {
        $error = "Acceso denegado. Verifica tus credenciales.";
    }
}

if (isset($_GET['logout'])) {
    session_destroy();
    header("Location: admin_web.php");
    exit();
}

$show_login = !isset($_SESSION['admin_logged']);

// --- ACCIONES MAESTRAS ---
if (!$show_login) {
    // 1. Exportar CSV
    if (isset($_GET['export'])) {
        header('Content-Type: text/csv');
        header('Content-Disposition: attachment; filename="Reporte_GALY_'.date('Ymd').'.csv"');
        $output = fopen('php://output', 'w');
        fputcsv($output, ['ID', 'Fecha', 'Cliente', 'DNI', 'Email', 'Total', 'Estado', 'Metodo', 'Entrega']);
        $res = $conn->query("SELECT p.*, u.nombre as cliente_nombre, u.email as cliente_email FROM pedidos p LEFT JOIN usuarios u ON p.usuario_id = u.id ORDER BY p.id DESC");
        while($row = $res->fetch_assoc()) {
            fputcsv($output, [$row['id'], $row['fecha'], $row['cliente_nombre'], $row['cliente_dni'], $row['cliente_email'], $row['total'], $row['estado'], $row['metodo_pago'], $row['metodo_entrega']]);
        }
        fclose($output);
        exit();
    }

    // 2. Pedidos: Actualizar Estado
    if (isset($_POST['update_order_status'])) {
        $stmt = $conn->prepare("UPDATE pedidos SET estado = ? WHERE id = ?");
        $stmt->bind_param("si", $_POST['new_status'], $_POST['order_id']);
        $stmt->execute();
    }

    // 3. Inventario: Guardar (Nuevo/Editar)
    if (isset($_POST['save_product'])) {
        $id = $_POST['product_id'];
        $nombre = $_POST['nombre'];
        $marca = $_POST['marca'];
        $precio_n = $_POST['precio_normal'];
        $precio_o = $_POST['precio_oferta'];
        $desc = $_POST['descripcion'];
        $cat = $_POST['categoria'];
        $stock = $_POST['stock'];
        $p_compra = $_POST['precio_compra'] ?? 0.0;
        $img = $_POST['imagen_url']; // Fallback si no se sube nueva
        $specs = $_POST['especificaciones'];
        $label = $_POST['etiqueta_especial'];
        $variantes = $_POST['variantes'] ?? '';
        $imgs_sec = $_POST['imagenes_secundarias'] ?? '';

        // Procesar subida de imagen principal
        if (isset($_FILES['img_file']) && $_FILES['img_file']['error'] == 0) {
            $target_dir = "img/";
            $file_name = time() . "_" . basename($_FILES["img_file"]["name"]);
            $target_file = $target_dir . $file_name;
            if (move_uploaded_file($_FILES["img_file"]["tmp_name"], $target_file)) {
                $img = $file_name;
            }
        }

        // Procesar subida de galería (múltiples imágenes)
        if (isset($_FILES['gallery_files'])) {
            $gallery_names = [];
            foreach ($_FILES['gallery_files']['tmp_name'] as $key => $tmp_name) {
                if ($_FILES['gallery_files']['error'][$key] == 0) {
                    $file_name = "gal_" . time() . "_" . $key . "_" . basename($_FILES["gallery_files"]["name"][$key]);
                    if (move_uploaded_file($tmp_name, "img/" . $file_name)) {
                        $gallery_names[] = $file_name;
                    }
                }
            }
            if (!empty($gallery_names)) {
                $imgs_sec = implode(",", $gallery_names);
            }
        }

        if ($id == "") {
            $stmt = $conn->prepare("INSERT INTO productos (nombre, marca, precio_normal, precio_oferta, descripcion, categoria_nombre, stock, precio_compra, imagen_url, especificaciones, etiqueta_especial, variantes, imagenes_secundarias) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
            $stmt->bind_param("ssdddidsissss", $nombre, $marca, $precio_n, $precio_o, $desc, $cat, $stock, $p_compra, $img, $specs, $label, $variantes, $imgs_sec);
        } else {
            $stmt = $conn->prepare("UPDATE productos SET nombre=?, marca=?, precio_normal=?, precio_oferta=?, descripcion=?, categoria_nombre=?, stock=?, precio_compra=?, imagen_url=?, especificaciones=?, etiqueta_especial=?, variantes=?, imagenes_secundarias=? WHERE id=?");
            $stmt->bind_param("ssdddidsissssi", $nombre, $marca, $precio_n, $precio_o, $desc, $cat, $stock, $p_compra, $img, $specs, $label, $variantes, $imgs_sec, $id);
        }
        $stmt->execute();
    }

    // 4. Banners: Guardar (Nuevo/Editar)
    if (isset($_POST['save_banner'])) {
        $id = $_POST['banner_id'];
        $titulo = $_POST['titulo'];
        $subtitulo = $_POST['subtitulo'];
        $img = $_POST['imagen_url'];
        $link = $_POST['link_url'];

        // Procesar subida de imagen de banner
        if (isset($_FILES['banner_file']) && $_FILES['banner_file']['error'] == 0) {
            $target_dir = "img/";
            $file_name = "banner_" . time() . "_" . basename($_FILES["banner_file"]["name"]);
            $target_file = $target_dir . $file_name;
            if (move_uploaded_file($_FILES["banner_file"]["tmp_name"], $target_file)) {
                $img = $file_name;
            }
        }

        // Procesar subida de galería (múltiples imágenes)
        if (isset($_FILES['gallery_files'])) {
            $gallery_names = [];
            foreach ($_FILES['gallery_files']['tmp_name'] as $key => $tmp_name) {
                if ($_FILES['gallery_files']['error'][$key] == 0) {
                    $file_name = "gal_" . time() . "_" . $key . "_" . basename($_FILES["gallery_files"]["name"][$key]);
                    if (move_uploaded_file($tmp_name, "img/" . $file_name)) {
                        $gallery_names[] = $file_name;
                    }
                }
            }
            if (!empty($gallery_names)) {
                $imgs_sec = implode(",", $gallery_names);
            }
        }

        if ($id == "") {
            $stmt = $conn->prepare("INSERT INTO banners_inicio (titulo, subtitulo, imagen_url, link_url) VALUES (?,?,?,?)");
            $stmt->bind_param("ssss", $titulo, $subtitulo, $img, $link);
        } else {
            $stmt = $conn->prepare("UPDATE banners_inicio SET titulo=?, subtitulo=?, imagen_url=?, link_url=? WHERE id=?");
            $stmt->bind_param("ssssi", $titulo, $subtitulo, $img, $link, $id);
        }
        $stmt->execute();
    }

    // 5. Categorías: Guardar
    if (isset($_POST['save_category'])) {
        $nombre = $_POST['cat_nombre'];
        $img = $_POST['cat_img'];
        $stmt = $conn->prepare("INSERT INTO categorias (nombre, imagen_url) VALUES (?,?) ON DUPLICATE KEY UPDATE imagen_url=?");
        $stmt->bind_param("sss", $nombre, $img, $img);
        $stmt->execute();
    }

    // 6. Cupones: Guardar
    if (isset($_POST['save_coupon'])) {
        $codigo = strtoupper($_POST['cup_codigo']);
        $desc = (int)$_POST['cup_desc'];
        $stmt = $conn->prepare("INSERT INTO cupones (codigo, descuento_porcentaje) VALUES (?,?) ON DUPLICATE KEY UPDATE descuento_porcentaje=?");
        $stmt->bind_param("sii", $codigo, $desc, $desc);
        $stmt->execute();
    }

    // 7. Notificaciones Push (Simulación)
    if (isset($_POST['send_push'])) {
        $titulo = $_POST['push_title'];
        $mensaje = $_POST['push_body'];
        file_put_contents('fcm_log.txt', "[" . date('Y-m-d H:i:s') . "] PUSH ENVIADO: $titulo - $mensaje\n", FILE_APPEND);
        $push_success = "Anuncio enviado correctamente a todos los dispositivos.";
    }

    // 8. Borrados
    if (isset($_GET['delete_product'])) {
        $conn->query("DELETE FROM productos WHERE id = " . (int)$_GET['delete_product']);
    }
    if (isset($_GET['delete_category'])) {
        $conn->query("DELETE FROM categorias WHERE id = " . (int)$_GET['delete_category']);
    }
    if (isset($_GET['delete_banner'])) {
        $conn->query("DELETE FROM banners_inicio WHERE id = " . (int)$_GET['delete_banner']);
    }
    if (isset($_GET['delete_coupon'])) {
        $conn->query("DELETE FROM cupones WHERE id = " . (int)$_GET['delete_coupon']);
    }

    // 9. Usuarios: Cambiar Rango / Borrar
    if (isset($_GET['toggle_admin'])) {
        $id = (int)$_GET['toggle_admin'];
        $conn->query("UPDATE usuarios SET es_admin = 1 - es_admin WHERE id = $id");
        header("Location: ?section=usuarios"); exit();
    }
    if (isset($_GET['delete_user'])) {
        $id = (int)$_GET['delete_user'];
        $conn->query("DELETE FROM usuarios WHERE id = $id");
        header("Location: ?section=usuarios"); exit();
    }
}

$section = isset($_GET['section']) ? $_GET['section'] : 'dashboard';

// Gráficos
$sales_labels = []; $sales_values = [];
if ($section == 'dashboard' && !$show_login) {
    for ($i = 6; $i >= 0; $i--) {
        $date = date('Y-m-d', strtotime("-$i days"));
        $sales_labels[] = date('d/m', strtotime($date));
        // Consulta simplificada para asegurar que traiga datos
        $q = $conn->query("SELECT SUM(total) FROM pedidos WHERE DATE(fecha) = '$date'");
        $val = $q->fetch_row()[0] ?? 0;
        $sales_values[] = (float)$val;
    }
}
?>
<!DOCTYPE html>
<html lang="es" data-bs-theme="dark">
<head>
    <meta charset="UTF-8">
    <title>GALY PRO | Panel Maestro</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js"></script>
    <style>
        :root { --galy-red: #E30613; --bg-dark: #000; --card-dark: #0a0a0a; --sidebar-w: 260px; }
        body { background: var(--bg-dark); color: #e0e0e0; font-family: 'Inter', 'Segoe UI', sans-serif; }
        .sidebar { height: 100vh; background: #050505; border-right: 1px solid #1a1a1a; position: fixed; width: var(--sidebar-w); z-index: 1000; }
        .nav-link { color: #888; padding: 14px 25px; border-radius: 12px; margin: 4px 15px; font-weight: 600; display: flex; align-items: center; transition: 0.2s; }
        .nav-link i { font-size: 1.2rem; }
        .nav-link:hover { background: #121212; color: #fff; }
        .nav-link.active { background: var(--galy-red); color: #fff; box-shadow: 0 4px 15px rgba(227, 6, 19, 0.3); }
        .main-content { margin-left: var(--sidebar-w); padding: 40px; }

        .card-stat { background: linear-gradient(145deg, #111, #080808); border: 1px solid #1a1a1a; border-radius: 20px; padding: 25px; transition: 0.3s; }
        .card-stat:hover { border-color: var(--galy-red); transform: translateY(-5px); }

        .table-container { background: var(--card-dark) !important; border: 1px solid #1a1a1a; border-radius: 20px; overflow: hidden; }
        .table { margin-bottom: 0; background-color: transparent !important; --bs-table-bg: transparent !important; }
        .table thead { background: #080808; }
        .table th { color: #888 !important; text-transform: uppercase; font-size: 0.75rem; letter-spacing: 1px; padding: 20px; border: none; }
        .table td { padding: 20px; border-top: 1px solid #151515 !important; vertical-align: middle; color: #fff !important; }
        .text-secondary { color: #888 !important; }
        .extra-small { font-size: 0.7rem; }
        .bg-black-galy { background-color: #050505 !important; }
        .text-white-galy { color: #ffffff !important; }

        /* ETIQUETAS DE ESTADO MAESTRAS */
        .status-badge {
            padding: 8px 16px;
            border-radius: 12px;
            font-size: 10px;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            display: inline-block;
        }
        .bg-pendiente { background: rgba(255, 165, 0, 0.15) !important; color: #ffcc00 !important; border: 1px solid #ffcc00 !important; }
        .bg-pagado { background: rgba(0, 255, 65, 0.15) !important; color: #00ff41 !important; border: 1px solid #00ff41 !important; }
        .bg-entregado { background: rgba(0, 184, 212, 0.15) !important; color: #00e5ff !important; border: 1px solid #00e5ff !important; }

        /* BOTONES CON EFECTO GALY */
        .btn-action {
            width: 38px; height: 38px;
            display: inline-flex; align-items: center; justify-content: center;
            border-radius: 10px; border: 1px solid #333;
            background: #000; color: #fff; transition: 0.3s;
        }
        .btn-action:hover {
            border-color: #E30613;
            box-shadow: 0 0 10px rgba(227, 6, 19, 0.5);
            background: #E30613;
            color: white;
        }
        .btn-galy { background: var(--galy-red); color: #fff; border: none; border-radius: 12px; font-weight: 800; padding: 10px 24px; }

        /* FORMULARIOS PROFESIONALES */
        .form-control, .form-select {
            background: #000 !important;
            border: 1px solid #333 !important;
            color: #fff !important;
            border-radius: 10px;
            padding: 12px;
        }
        .form-control:focus {
            background: #0a0a0a;
            border-color: #E30613 !important;
            box-shadow: 0 0 5px rgba(227, 6, 19, 0.3) !important;
            color: white;
        }

        .modal-content { background: #0a0a0a; border: 1px solid #222; border-radius: 25px; }
        .modal-header { border-bottom: 1px solid #1a1a1a; }
        canvas { width: 100% !important; height: 100% !important; background: #050505; }
    </style>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;800&display=swap" rel="stylesheet">
</head>
<body>

<?php if ($show_login): ?>
    <div class="d-flex align-items-center justify-content-center vh-100" style="background: radial-gradient(circle at center, #1a0000 0%, #000 100%);">
        <div class="card p-5 shadow-lg text-center" style="width: 420px; background: rgba(10,10,10,0.8); backdrop-filter: blur(20px); border: 1px solid rgba(255,255,255,0.05); border-radius: 30px;">
            <img src="img/logo_2.png" width="80" class="mb-4">
            <h2 class="fw-black text-white mb-2">GALY <span class="text-danger">PRO</span></h2>
            <p class="text-secondary small mb-4">Ingresa tus credenciales maestras</p>
            <?php if(isset($error)) echo "<div class='alert alert-danger py-2 small'>$error</div>"; ?>
            <form method="POST">
                <input type="email" name="email" class="form-control mb-3" placeholder="Admin Email" required>
                <input type="password" name="password" class="form-control mb-4" placeholder="Password" required>
                <button type="submit" name="login" class="btn btn-galy w-100 py-3 shadow">INGRESAR AL PANEL</button>
            </form>
        </div>
    </div>
<?php else: ?>
    <div class="sidebar">
        <div class="p-4 text-center border-bottom border-dark mb-4">
            <h4 class="fw-black m-0 text-white">GALY <span class="text-danger">MAESTRO</span></h4>
            <div class="mt-2"><span class="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25" style="font-size: 9px;">SERVIDOR ONLINE</span></div>
        </div>
        <nav class="nav flex-column">
            <a class="nav-link <?php echo $section=='dashboard'?'active':''; ?>" href="?section=dashboard"><i class="bi bi-grid-1x2-fill me-3"></i>Resumen</a>
            <a class="nav-link <?php echo $section=='pedidos'?'active':''; ?>" href="?section=pedidos"><i class="bi bi-bag-check-fill me-3"></i>Ventas Realizadas</a>
            <a class="nav-link <?php echo $section=='productos'?'active':''; ?>" href="?section=productos"><i class="bi bi-box-seam-fill me-3"></i>Mi Inventario</a>
            <a class="nav-link <?php echo $section=='usuarios'?'active':''; ?>" href="?section=usuarios"><i class="bi bi-people-fill me-3"></i>Clientes</a>
            <a class="nav-link <?php echo $section=='categorias'?'active':''; ?>" href="?section=categorias"><i class="bi bi-tags-fill me-3"></i>Categorías</a>
            <a class="nav-link <?php echo $section=='cupones'?'active':''; ?>" href="?section=cupones"><i class="bi bi-ticket-perforated-fill me-3"></i>Cupones</a>
            <a class="nav-link <?php echo $section=='notificaciones'?'active':''; ?>" href="?section=notificaciones"><i class="bi bi-send-fill me-3"></i>Anuncios Push</a>
            <a class="nav-link <?php echo $section=='soporte'?'active':''; ?>" href="?section=soporte"><i class="bi bi-tools me-3"></i>Soporte Técnico</a>
            <a class="nav-link <?php echo $section=='banners'?'active':''; ?>" href="?section=banners"><i class="bi bi-image-fill me-3"></i>Banners Home</a>
            <hr class="mx-3 border-secondary opacity-25">
            <a class="nav-link text-danger" href="?logout=true"><i class="bi bi-power me-3"></i>Cerrar Sesión</a>
        </nav>
    </div>

    <div class="main-content">
        <?php if ($section == 'dashboard'): ?>
            <div class="d-flex justify-content-between align-items-center mb-5">
                <div>
                    <h2 class="fw-black mb-1">¡Hola, Maestro Gustavo!</h2>
                    <p class="text-secondary m-0">Aquí está el rendimiento actual de GALY XNOVATEC</p>
                </div>
                <div class="d-flex gap-2">
                    <button class="btn btn-warning border-0 px-4 fw-bold" onclick="generarReporteContable()"><i class="bi bi-file-earmark-bar-graph me-2"></i>REPORTE CONTABLE</button>
                    <button class="btn btn-outline-info px-4 fw-bold" onclick="renderCharts()"><i class="bi bi-graph-up me-2"></i>RECARGAR GRÁFICOS</button>
                    <button class="btn btn-dark border-secondary px-4" onclick="window.location.reload()"><i class="bi bi-arrow-clockwise"></i></button>
                </div>
            </div>

            <div class="row g-4 mb-5">
                <?php
                // Consultas directas para evitar fallos de lógica
                $total_ventas = $conn->query("SELECT SUM(total) FROM pedidos")->fetch_row()[0] ?? 0;
                $total_ganancia = $total_ventas * 0.3; // Estimación 30%
                $total_pedidos = $conn->query("SELECT COUNT(*) FROM pedidos")->fetch_row()[0] ?? 0;
                $total_clientes = $conn->query("SELECT COUNT(*) FROM usuarios")->fetch_row()[0] ?? 0;
                ?>
                <div class="col-md-3">
                    <div class="card-stat">
                        <i class="bi bi-currency-dollar text-success fs-4 mb-2"></i>
                        <h3 class="fw-black m-0">S/ <?php echo number_format($total_ventas, 0); ?></h3>
                        <p class="text-secondary small">Ventas Totales</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-stat">
                        <i class="bi bi-graph-up-arrow text-warning fs-4 mb-2"></i>
                        <h3 class="fw-black m-0">S/ <?php echo number_format($total_ganancia, 0); ?></h3>
                        <p class="text-secondary small">Ganancia Est.</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-stat">
                        <i class="bi bi-cart-fill text-danger fs-4 mb-2"></i>
                        <h3 class="fw-black m-0"><?php echo $total_pedidos; ?></h3>
                        <p class="text-secondary small">Pedidos Realizados</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card-stat">
                        <i class="bi bi-person-plus-fill text-info fs-4 mb-2"></i>
                        <h3 class="fw-black m-0"><?php echo $total_clientes; ?></h3>
                        <p class="text-secondary small">Clientes Registrados</p>
                    </div>
                </div>
            </div>

            <div class="row g-4">
                <div class="col-lg-7">
                    <div class="table-container p-4" style="height: 350px;">
                        <h5 class="fw-bold mb-4">Ventas (Soles)</h5>
                        <canvas id="masterChart"></canvas>
                    </div>
                </div>
                <div class="col-lg-5">
                    <div class="table-container p-4" style="height: 350px;">
                        <h5 class="fw-bold mb-4">Ventas por Distrito</h5>
                        <canvas id="districtChart"></canvas>
                    </div>
                </div>
            </div>

            <div class="row g-4 mt-2">
                <div class="col-lg-8">
                    <div class="table-container p-4">
                        <h5 class="fw-bold mb-3">Última Actividad</h5>
                        <div class="activity-feed">
                            <?php
                            $recent_orders = $conn->query("SELECT p.*, u.nombre FROM pedidos p JOIN usuarios u ON p.usuario_id = u.id ORDER BY p.id DESC LIMIT 5");
                            while($ro = $recent_orders->fetch_assoc()):
                            ?>
                            <div class="d-flex mb-3 align-items-center">
                                <div class="bg-dark rounded p-2 me-3"><i class="bi bi-cart text-danger"></i></div>
                                <div class="small">
                                    <div class="text-white fw-bold">Pedido #<?php echo $ro['id']; ?> realizado</div>
                                    <div class="text-secondary"><?php echo $ro['nombre']; ?> • S/ <?php echo $ro['total']; ?></div>
                                </div>
                                <div class="ms-auto extra-small text-secondary"><?php echo date('H:i', strtotime($ro['fecha'])); ?></div>
                            </div>
                            <?php endwhile; ?>
                        </div>
                    </div>
                </div>
                <div class="col-lg-4">
                    <div class="table-container p-4 border-danger border-opacity-25" style="background: rgba(227, 6, 19, 0.02);">
                        <h5 class="fw-bold mb-3 text-danger"><i class="bi bi-exclamation-octagon-fill me-2"></i>Stock Crítico</h5>
                        <div class="stock-alerts">
                            <?php
                            $low_stock = $conn->query("SELECT nombre, stock FROM productos WHERE stock < 5 ORDER BY stock ASC LIMIT 4");
                            if($low_stock->num_rows == 0) echo "<p class='text-secondary small'>Todo el inventario está al día. ✅</p>";
                            while($ls = $low_stock->fetch_assoc()):
                            ?>
                            <div class="d-flex justify-content-between align-items-center mb-3 p-2 rounded-3 bg-black">
                                <div class="small text-white-50 text-truncate me-2"><?php echo $ls['nombre']; ?></div>
                                <span class="badge bg-danger"><?php echo $ls['stock']; ?> un.</span>
                            </div>
                            <?php endwhile; ?>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row g-4 mt-4">
                <div class="col-lg-7">
                    <div class="table-container p-4">
                        <h5 class="fw-bold mb-4 text-white"><i class="bi bi-star-fill text-warning me-2"></i>Productos Más Vendidos</h5>
                        <div class="row g-3">
                            <?php
                            $top_sold = $conn->query("SELECT p.nombre, p.marca, SUM(pp.cantidad) as total_vendido, p.imagen_url, p.precio_oferta
                                                     FROM pedido_productos pp
                                                     JOIN productos p ON pp.producto_id = p.id
                                                     GROUP BY pp.producto_id
                                                     ORDER BY total_vendido DESC
                                                     LIMIT 4");
                            if($top_sold->num_rows == 0) echo "<div class='col-12'><p class='text-secondary small'>Aún no hay ventas registradas para este reporte. 📈</p></div>";
                            while($ts = $top_sold->fetch_assoc()):
                            ?>
                            <div class="col-md-3">
                                <div class="bg-black p-3 rounded-4 border border-secondary border-opacity-10 h-100">
                                    <div class="d-flex align-items-center mb-3">
                                        <div class="position-relative">
                                            <img src="img/<?php echo $ts['imagen_url']; ?>" width="60" height="60" class="rounded me-3" style="object-fit: contain; background: #000;">
                                            <span class="position-absolute top-0 start-0 translate-middle badge rounded-pill bg-danger" style="font-size: 10px;">
                                                x<?php echo $ts['total_vendido']; ?>
                                            </span>
                                        </div>
                                        <div>
                                            <div class="text-white fw-bold small text-truncate" style="max-width: 120px;"><?php echo $ts['nombre']; ?></div>
                                            <div class="text-secondary extra-small"><?php echo strtoupper($ts['marca']); ?></div>
                                        </div>
                                    </div>
                                    <div class="d-flex justify-content-between align-items-center mt-2">
                                        <div class="text-white fw-black small">S/ <?php echo number_format($ts['precio_oferta'], 0); ?></div>
                                        <div class="text-success small fw-bold">TOP VENTA</div>
                                    </div>
                                </div>
                            </div>
                            <?php endwhile; ?>
                        </div>
                    </div>
                </div>
                <div class="col-lg-5">
                    <div class="table-container p-4">
                        <h5 class="fw-bold mb-4 text-white"><i class="bi bi-crown-fill text-warning me-2"></i>Top 5 Clientes VIP</h5>
                        <div class="activity-feed">
                            <?php
                            $vips = $conn->query("SELECT u.nombre, u.email, SUM(p.total) as total_gastado
                                                 FROM pedidos p
                                                 JOIN usuarios u ON p.usuario_id = u.id
                                                 WHERE p.estado != 'CANCELADO'
                                                 GROUP BY p.usuario_id
                                                 ORDER BY total_gastado DESC
                                                 LIMIT 5");
                            while($v = $vips->fetch_assoc()):
                            ?>
                            <div class="d-flex mb-3 align-items-center bg-black p-2 rounded-3 border border-secondary border-opacity-10">
                                <div class="bg-warning bg-opacity-10 text-warning rounded-circle p-2 me-3"><i class="bi bi-person-heart"></i></div>
                                <div class="small">
                                    <div class="text-white fw-bold"><?php echo $v['nombre']; ?></div>
                                    <div class="text-secondary extra-small"><?php echo $v['email']; ?></div>
                                </div>
                                <div class="ms-auto text-warning fw-black">S/ <?php echo number_format($v['total_gastado'], 0); ?></div>
                            </div>
                            <?php endwhile; ?>
                        </div>
                    </div>
                </div>
            </div>

        <?php elseif ($section == 'pedidos'): ?>
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="fw-black">Registro de Ventas</h2>
                <div class="d-flex gap-2">
                    <a href="?export=true" class="btn btn-outline-success border-success border-opacity-25 px-4"><i class="bi bi-file-earmark-excel me-2"></i>REPORTE EXCEL</a>
                </div>
            </div>
            <div class="table-container">
                <table class="table align-middle bg-black-galy">
                    <thead>
                        <tr><th>ORDEN</th><th>CLIENTE / DNI</th><th>DETALLE DE ENTREGA</th><th>TOTAL</th><th>ESTADO</th><th>ACCIONES</th></tr>
                    </thead>
                    <tbody>
                        <?php
                        $res = $conn->query("SELECT p.*, u.nombre as user_name FROM pedidos p LEFT JOIN usuarios u ON p.usuario_id = u.id ORDER BY p.id DESC");
                        while($o = $res->fetch_assoc()):
                        ?>
                        <tr>
                            <td><span class="fw-black text-white">#00<?php echo $o['id']; ?></span><br><small class="text-secondary"><?php echo date('d M, H:i', strtotime($o['fecha'])); ?></small></td>
                            <td>
                                <div class="d-flex align-items-center">
                                    <div class="bg-danger bg-opacity-10 text-danger rounded-circle p-2 me-3" style="width: 35px; height: 35px; display: flex; align-items: center; justify-content: center;">
                                        <i class="bi bi-person-fill"></i>
                                    </div>
                                    <div>
                                        <div class="text-white fw-bold"><?php echo $o['user_name'] ?: 'Cliente GALY'; ?></div>
                                        <div class="text-secondary extra-small">ID #<?php echo $o['usuario_id']; ?> | DNI: <?php echo $o['cliente_dni'] ?: 'S/DNI'; ?></div>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <div class="small text-white">
                                    <i class="bi bi-geo-alt-fill text-danger me-1"></i> <?php echo $o['direccion'] ?: 'Recojo en Tienda'; ?><br>
                                    <span class="text-secondary extra-small"><?php echo $o['distrito']; ?>, <?php echo $o['ciudad']; ?></span>
                                </div>
                            </td>
                            <td>
                                <div class="text-white fw-black fs-5">S/ <?php echo number_format($o['total'], 2); ?></div>
                                <div class="text-secondary extra-small"><?php echo strtoupper($o['metodo_pago']); ?></div>
                            </td>
                            <td><span class="status-badge bg-<?php echo strtolower($o['estado']); ?>"><?php echo $o['estado']; ?></span></td>
                            <td>
                                <div class="d-flex gap-2">
                                    <button class="btn-action" onclick='verItems(<?php echo $o["id"]; ?>)' title="Ver Productos"><i class="bi bi-eye"></i></button>
                                    <button class="btn-action" onclick='generarBoletaPDF(<?php echo $o["id"]; ?>)' title="Generar Boleta"><i class="bi bi-file-earmark-pdf"></i></button>
                                    <button class="btn-action" onclick='enviarBoletaWhatsApp(<?php echo $o["id"]; ?>, "<?php echo $o["cliente_telefono"]; ?>", "<?php echo $o["user_name"] ?: "Cliente"; ?>")' style="color: #25D366;" title="Enviar por WhatsApp">
                                        <i class="bi bi-whatsapp"></i>
                                    </button>

                                    <div class="dropdown">
                                        <button class="btn-action" data-bs-toggle="dropdown"><i class="bi bi-gear"></i></button>
                                        <ul class="dropdown-menu dropdown-menu-dark">
                                            <li><form method="POST"><input type="hidden" name="order_id" value="<?php echo $o['id']; ?>"><button type="submit" name="update_order_status" value="PENDIENTE" class="dropdown-item">M. PENDIENTE</button><input type="hidden" name="new_status" value="PENDIENTE"></form></li>
                                            <li><form method="POST"><input type="hidden" name="order_id" value="<?php echo $o['id']; ?>"><button type="submit" name="update_order_status" value="PAGADO" class="dropdown-item">M. PAGADO</button><input type="hidden" name="new_status" value="PAGADO"></form></li>
                                            <li><form method="POST"><input type="hidden" name="order_id" value="<?php echo $o['id']; ?>"><button type="submit" name="update_order_status" value="ENTREGADO" class="dropdown-item">M. ENTREGADO</button><input type="hidden" name="new_status" value="ENTREGADO"></form></li>
                                        </ul>
                                    </div>
                                </div>
                            </td>
                        </tr>
                        <?php endwhile; ?>
                    </tbody>
                </table>
            </div>

        <?php elseif ($section == 'productos'): ?>
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="fw-black">Gestión de Inventario</h2>
                <div class="d-flex gap-3">
                    <input type="text" id="productSearch" class="form-control bg-dark text-white border-secondary form-control-sm" placeholder="Filtrar productos..." onkeyup="filterProducts()">
                    <button class="btn btn-galy text-nowrap" onclick="nuevoProducto()">AÑADIR PRODUCTO</button>
                </div>
            </div>
            <div class="table-container">
                <table class="table align-middle bg-black-galy" id="productTable">
                    <thead><tr><th>IMG</th><th>NOMBRE / MARCA</th><th>PRECIO</th><th>STOCK</th><th>ESTADO</th><th>ACCIONES</th></tr></thead>
                    <tbody>
                        <?php
                        $res = $conn->query("SELECT * FROM productos ORDER BY id DESC");
                        while($p = $res->fetch_assoc()):
                        ?>
                        <tr>
                            <td><img src="img/<?php echo $p['imagen_url']; ?>" width="50" height="50" class="rounded" style="object-fit: contain; background: #000;"></td>
                            <td>
                                <div class="text-white fw-bold"><?php echo $p['nombre']; ?></div>
                                <div class="text-secondary extra-small"><?php echo strtoupper($p['marca']); ?> | <?php echo $p['categoria_nombre']; ?></div>
                            </td>
                            <td><span class="text-danger fw-black">S/ <?php echo $p['precio_oferta']; ?></span><br><del class="text-secondary small">S/ <?php echo $p['precio_normal']; ?></del></td>
                            <td>
                                <div class="d-flex align-items-center">
                                    <span class="fw-bold me-2"><?php echo $p['stock']; ?></span>
                                    <?php if($p['stock'] < 5): ?>
                                        <i class="bi bi-exclamation-triangle-fill text-warning" title="Stock Bajo"></i>
                                    <?php endif; ?>
                                </div>
                            </td>
                            <td><?php echo $p['disponible'] ? '<span class="status-badge bg-pagado">Activo</span>' : '<span class="status-badge bg-pendiente">Oculto</span>'; ?></td>
                            <td>
                                <div class="d-flex gap-2">
                                    <button class="btn-action" onclick='editarProducto(<?php echo json_encode($p); ?>)'><i class="bi bi-pencil-square"></i></button>
                                    <a href="?section=productos&delete_product=<?php echo $p['id']; ?>" class="btn-action" onclick="return confirm('¿Eliminar producto?')"><i class="bi bi-trash3 text-danger"></i></a>
                                </div>
                            </td>
                        </tr>
                        <?php endwhile; ?>
                    </tbody>
                </table>
            </div>

        <?php elseif ($section == 'usuarios'): ?>
            <div class="table-container card overflow-hidden">
                <table class="table align-middle mb-0 bg-black-galy">
                    <thead><tr class="small text-secondary"><th>ID</th><th>NOMBRE</th><th>EMAIL</th><th>PUNTOS</th><th>ADMIN</th><th>ACCIONES</th></tr></thead>
                    <tbody>
                        <?php
                        $res = $conn->query("SELECT * FROM usuarios ORDER BY id DESC");
                        while($u = $res->fetch_assoc()):
                        ?>
                        <tr>
                            <td>#<?php echo $u['id']; ?></td>
                            <td><b><?php echo $u['nombre']; ?></b></td>
                            <td><?php echo $u['email']; ?></td>
                            <td>
                                <span class="badge bg-warning text-dark"><?php echo $u['puntos']; ?> pts</span>
                                <?php
                                    $pts = $u['puntos'];
                                    if($pts >= 1500) echo '<span class="badge bg-warning ms-1" style="background:#FFD700 !important; color:black;">ORO</span>';
                                    else if($pts >= 500) echo '<span class="badge bg-secondary ms-1" style="background:#C0C0C0 !important; color:black;">PLATA</span>';
                                    else echo '<span class="badge bg-dark ms-1 border border-secondary" style="color:#CD7F32;">BRONCE</span>';
                                ?>
                            </td>
                            <td>
                                <a href="?section=usuarios&toggle_admin=<?php echo $u['id']; ?>" class="text-decoration-none">
                                    <?php echo $u['es_admin'] ? '<span class="badge bg-success">SÍ</span>' : '<span class="badge bg-dark">NO</span>'; ?>
                                </a>
                            </td>
                            <td>
                                <a href="?section=usuarios&delete_user=<?php echo $u['id']; ?>" class="btn btn-sm btn-outline-danger" onclick="return confirm('¿Eliminar usuario?')"><i class="bi bi-person-x"></i></a>
                            </td>
                        </tr>
                        <?php endwhile; ?>
                    </tbody>
                </table>
            </div>

        <?php elseif ($section == 'banners'): ?>
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="fw-bold">Gestión de Publicidad (Banners)</h2>
                <button class="btn btn-galy" onclick="nuevoBanner()">AÑADIR BANNER</button>
            </div>
            <div class="row g-4">
                <?php
                $res = $conn->query("SELECT * FROM banners_inicio");
                while($b = $res->fetch_assoc()):
                ?>
                <div class="col-md-6 col-lg-4">
                    <div class="card h-100 overflow-hidden" style="border: 1px solid rgba(255,255,255,0.05); transition: 0.3s;">
                        <img src="img/<?php echo $b['imagen_url']; ?>" class="card-img-top" style="height: 180px; object-fit: cover; filter: brightness(0.8);">
                        <div class="card-body bg-black">
                            <h5 class="card-title fw-bold text-white"><?php echo $b['titulo']; ?></h5>
                            <p class="card-text text-secondary small"><?php echo $b['subtitulo']; ?></p>
                            <div class="d-flex gap-2 mt-3">
                                <button class="btn btn-sm btn-dark flex-grow-1 border-secondary" onclick='editarBanner(<?php echo json_encode($b); ?>)'><i class="bi bi-pencil me-1"></i> EDITAR</button>
                                <a href="?section=banners&delete_banner=<?php echo $b['id']; ?>" class="btn btn-sm btn-outline-danger" onclick="return confirm('¿Eliminar banner?')"><i class="bi bi-trash"></i></a>
                            </div>
                        </div>
                    </div>
                </div>
                <?php endwhile; ?>
            </div>

        <?php elseif ($section == 'categorias'): ?>
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="fw-bold">Gestión de Categorías</h2>
                <button class="btn btn-galy" data-bs-toggle="modal" data-bs-target="#catModal">NUEVA CATEGORÍA</button>
            </div>
            <div class="card overflow-hidden">
                <table class="table table-hover mb-0 align-middle">
                    <thead><tr class="small text-secondary"><th>IMG</th><th>NOMBRE</th><th>PRODUCTOS</th><th>ACCIONES</th></tr></thead>
                    <tbody>
                        <?php
                        $res = $conn->query("SELECT c.*, (SELECT COUNT(*) FROM productos p WHERE p.categoria_nombre = c.nombre) as total FROM categorias c");
                        while($c = $res->fetch_assoc()):
                        ?>
                        <tr>
                            <td><img src="img/<?php echo $c['imagen_url']; ?>" width="30" height="30" class="rounded-circle"></td>
                            <td class="fw-bold"><?php echo $c['nombre']; ?></td>
                            <td><span class="badge bg-dark"><?php echo $c['total']; ?> productos</span></td>
                            <td>
                                <a href="?section=categorias&delete_category=<?php echo $c['id']; ?>" class="btn btn-sm btn-outline-danger" onclick="return confirm('¿Eliminar categoría?')"><i class="bi bi-trash"></i></a>
                            </td>
                        </tr>
                        <?php endwhile; ?>
                    </tbody>
                </table>
            </div>

        <?php elseif ($section == 'cupones'): ?>
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="fw-bold">Marketing: Cupones GALY</h2>
                <button class="btn btn-galy" data-bs-toggle="modal" data-bs-target="#cupModal">NUEVO CUPÓN</button>
            </div>
            <div class="card overflow-hidden">
                <table class="table table-hover mb-0 align-middle">
                    <thead><tr class="small text-secondary"><th>ID</th><th>CÓDIGO</th><th>DESCUENTO</th><th>ESTADO</th><th>ACCIONES</th></tr></thead>
                    <tbody>
                        <?php
                        $res = $conn->query("SELECT * FROM cupones");
                        while($cu = $res->fetch_assoc()):
                        ?>
                        <tr>
                            <td>#<?php echo $cu['id']; ?></td>
                            <td class="fw-bold text-danger"><?php echo $cu['codigo']; ?></td>
                            <td><?php echo $cu['descuento_porcentaje']; ?>% OFF</td>
                            <td><span class="badge bg-success">Activo</span></td>
                            <td>
                                <a href="?section=cupones&delete_coupon=<?php echo $cu['id']; ?>" class="btn btn-sm btn-outline-danger" onclick="return confirm('¿Eliminar cupón?')"><i class="bi bi-trash"></i></a>
                            </td>
                        </tr>
                        <?php endwhile; ?>
                    </tbody>
                </table>
            </div>

        <?php elseif ($section == 'soporte'): ?>
            <h2 class="fw-black mb-4">Gestión de Soporte Técnico</h2>
            <div class="table-container">
                <table class="table table-hover align-middle">
                    <thead>
                        <tr><th>TICKET</th><th>CLIENTE / EQUIPO</th><th>FALLA REPORTADA</th><th>ESTADO</th><th>ACCIONES</th></tr>
                    </thead>
                    <tbody>
                        <?php
                        $res = $conn->query("SELECT t.*, u.nombre as user_name FROM servicios_tecnicos t JOIN usuarios u ON t.usuario_id = u.id ORDER BY t.id DESC");
                        while($t = $res->fetch_assoc()):
                        ?>
                        <tr>
                            <td><span class="fw-black text-white">#T-<?php echo $t['id']; ?></span><br><small class="text-secondary"><?php echo date('d M', strtotime($t['fecha_ingreso'])); ?></small></td>
                            <td><b><?php echo $t['user_name']; ?></b><br><small class="text-info"><?php echo $t['equipo_modelo']; ?></small></td>
                            <td><div class="extra-small text-secondary" style="max-width: 250px;"><?php echo $t['descripcion_falla']; ?></div></td>
                            <td>
                                <span class="status-badge bg-<?php
                                    if($t['estado']=='Recibido') echo 'pendiente';
                                    else if($t['estado']=='Listo') echo 'pagado';
                                    else echo 'entregado';
                                ?>"><?php echo $t['estado']; ?></span>
                            </td>
                            <td>
                                <form method="POST" action="update_ticket_status_web.php" class="d-flex flex-column gap-2" enctype="multipart/form-data">
                                    <input type="hidden" name="id" value="<?php echo $t['id']; ?>">
                                    <div class="d-flex gap-2">
                                        <select name="estado" class="form-select form-select-sm bg-black text-white border-secondary w-auto" style="font-size: 10px;">
                                            <option value="Recibido" <?php echo $t['estado']=='Recibido'?'selected':''; ?>>RECIBIDO</option>
                                            <option value="En Diagnostico" <?php echo $t['estado']=='En Diagnostico'?'selected':''; ?>>DIAGNÓSTICO</option>
                                            <option value="Reparando" <?php echo $t['estado']=='Reparando'?'selected':''; ?>>REPARANDO</option>
                                            <option value="Listo" <?php echo $t['estado']=='Listo'?'selected':''; ?>>¡LISTO!</option>
                                            <option value="Entregado" <?php echo $t['estado']=='Entregado'?'selected':''; ?>>ENTREGADO</option>
                                        </select>
                                        <input type="number" name="costo" class="form-control form-control-sm bg-black text-white border-secondary w-auto" placeholder="S/ Costo" style="width: 80px !important;" value="<?php echo $t['costo_estimado']; ?>">
                                    </div>
                                    <div class="d-flex gap-2 align-items-center">
                                        <div class="flex-grow-1">
                                            <label class="extra-small text-secondary m-0">Foto Ingreso:</label>
                                            <input type="file" name="img_ingreso" class="form-control form-control-sm bg-black text-white border-secondary" style="font-size: 9px;">
                                        </div>
                                        <div class="flex-grow-1">
                                            <label class="extra-small text-secondary m-0">Foto Listo:</label>
                                            <input type="file" name="img_listo" class="form-control form-control-sm bg-black text-white border-secondary" style="font-size: 9px;">
                                        </div>
                                        <button type="submit" class="btn btn-sm btn-galy mt-3">OK</button>
                                    </div>
                                    <?php if($t['imagen_ingreso'] || $t['imagen_listo']): ?>
                                        <div class="d-flex gap-2 mt-1">
                                            <?php if($t['imagen_ingreso']): ?>
                                                <a href="img/<?php echo $t['imagen_ingreso']; ?>" target="_blank" class="badge bg-dark text-decoration-none border border-secondary">VER ANTES</a>
                                            <?php endif; ?>
                                            <?php if($t['imagen_listo']): ?>
                                                <a href="img/<?php echo $t['imagen_listo']; ?>" target="_blank" class="badge bg-success text-decoration-none border border-success border-opacity-25">VER DESPUÉS</a>
                                            <?php endif; ?>
                                        </div>
                                    <?php endif; ?>
                                </form>
                            </td>
                        </tr>
                        <?php endwhile; ?>
                    </tbody>
                </table>
            </div>

        <?php elseif ($section == 'notificaciones'): ?>
            <h2 class="fw-bold mb-4">Anuncios Push (Notificaciones)</h2>
            <div class="card p-5">
                <?php if(isset($push_success)) echo "<div class='alert alert-success'>$push_success</div>"; ?>
                <form method="POST">
                    <div class="mb-4">
                        <label class="form-label small text-secondary">Título del Anuncio</label>
                        <input type="text" name="push_title" class="form-control bg-black text-white border-secondary" placeholder="¡Oferta de Locura!" required>
                    </div>
                    <div class="mb-4">
                        <label class="form-label small text-secondary">Mensaje Corto</label>
                        <textarea name="push_body" class="form-control bg-black text-white border-secondary" rows="3" placeholder="Ingresa el mensaje que verán los clientes en sus celulares..." required></textarea>
                    </div>
                    <div class="alert alert-info bg-dark text-info border-info opacity-75">
                        <i class="bi bi-info-circle me-2"></i> Esta función enviará el mensaje a todos los dispositivos registrados en Firebase.
                    </div>
                    <button type="submit" name="send_push" class="btn btn-galy w-100 py-3">ENVIAR ANUNCIO AHORA</button>
                </form>
            </div>

        <?php endif; ?>
    </div>

    <!-- MODALES Y SCRIPTS -->
    <div class="modal fade" id="productModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content bg-dark text-white p-4">
                <h4 id="modalTitle">Nuevo Producto</h4>
                <hr class="border-secondary">
                <form method="POST" enctype="multipart/form-data">
                    <input type="hidden" name="product_id" id="form_id">
                    <div class="row g-3">
                        <div class="col-md-6">
                            <label class="small text-secondary">Nombre</label>
                            <input type="text" name="nombre" id="form_nombre" class="form-control bg-black text-white border-secondary" required>
                        </div>
                        <div class="col-md-6">
                            <label class="small text-secondary">Marca</label>
                            <input type="text" name="marca" id="form_marca" class="form-control bg-black text-white border-secondary">
                        </div>
                        <div class="col-md-4">
                            <label class="small text-secondary">P. Compra (Costo)</label>
                            <input type="number" step="0.01" name="precio_compra" id="form_p_compra" class="form-control bg-black text-white border-secondary" placeholder="0.00">
                        </div>
                        <div class="col-md-4">
                            <label class="small text-secondary">P. Normal (Venta)</label>
                            <input type="number" step="0.01" name="precio_normal" id="form_precio_n" class="form-control bg-black text-white border-secondary">
                        </div>
                        <div class="col-md-4">
                            <label class="small text-secondary">P. Oferta</label>
                            <input type="number" step="0.01" name="precio_oferta" id="form_precio_o" class="form-control bg-black text-white border-secondary">
                        </div>
                        <div class="col-md-4">
                            <label class="small text-secondary">Stock</label>
                            <input type="number" name="stock" id="form_stock" class="form-control bg-black text-white border-secondary">
                        </div>
                        <div class="col-md-6">
                            <label class="small text-secondary">Categoría</label>
                            <select name="categoria" id="form_cat" class="form-select bg-black text-white border-secondary">
                                <?php
                                $c_res = $conn->query("SELECT nombre FROM categorias");
                                while($cr = $c_res->fetch_row()) echo "<option value='{$cr[0]}'>{$cr[0]}</option>";
                                ?>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="small text-secondary">Subir Imagen (Recomendado)</label>
                            <input type="file" name="img_file" class="form-control bg-black text-white border-secondary" accept="image/*" onchange="previewImg(this)">
                            <input type="hidden" name="imagen_url" id="form_img">
                        </div>
                        <div class="col-12 text-center" id="img_preview_container" style="display:none;">
                            <img id="img_preview" src="" style="max-height: 100px; border-radius: 10px; border: 1px solid #333;">
                        </div>
                        <div class="col-12">
                            <label class="small text-secondary">Descripción</label>
                            <textarea name="descripcion" id="form_desc" class="form-control bg-black text-white border-secondary" rows="2"></textarea>
                        </div>
                        <div class="col-md-6">
                            <label class="small text-secondary">Especificaciones</label>
                            <input type="text" name="especificaciones" id="form_specs" class="form-control bg-black text-white border-secondary" placeholder="CPU: i7, RAM: 16GB">
                        </div>
                        <div class="col-md-6">
                            <label class="small text-secondary">Etiqueta</label>
                            <input type="text" name="etiqueta_especial" id="form_label" class="form-control bg-black text-white border-secondary" placeholder="NUEVO, OFERTA">
                        </div>
                        <div class="col-md-6">
                            <label class="small text-secondary">Variantes (Comas)</label>
                            <input type="text" name="variantes" id="form_variantes" class="form-control bg-black text-white border-secondary" placeholder="Rojo, Azul, Negro">
                        </div>
                        <div class="col-md-6">
                            <label class="small text-secondary">Galería (Subir múltiples)</label>
                            <input type="file" name="gallery_files[]" class="form-control bg-black text-white border-secondary" accept="image/*" multiple onchange="previewGallery(this)">
                            <input type="hidden" name="imagenes_secundarias" id="form_imgs_sec">
                        </div>
                        <div class="col-12" id="gallery_preview_container" style="display:none; background: rgba(255,255,255,0.02); padding: 10px; border-radius: 10px;">
                            <div id="gallery_preview" class="d-flex flex-wrap gap-2"></div>
                        </div>
                    </div>
                    <div class="mt-4">
                        <button type="submit" name="save_product" class="btn btn-galy w-100">GUARDAR PRODUCTO</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- MODAL CUPONES -->
    <div class="modal fade" id="cupModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content bg-dark text-white p-4">
                <h4>Nuevo Cupón</h4>
                <hr class="border-secondary">
                <form method="POST">
                    <div class="mb-3">
                        <label class="small text-secondary">Código del Cupón</label>
                        <input type="text" name="cup_codigo" class="form-control bg-black text-white border-secondary" placeholder="EJ: GALY2025" required>
                    </div>
                    <div class="mb-4">
                        <label class="small text-secondary">Porcentaje de Descuento (%)</label>
                        <input type="number" name="cup_desc" class="form-control bg-black text-white border-secondary" placeholder="10" min="1" max="100" required>
                    </div>
                    <button type="submit" name="save_coupon" class="btn btn-galy w-100">CREAR CUPÓN</button>
                </form>
            </div>
        </div>
    </div>

    <!-- MODAL CATEGORIAS -->
    <div class="modal fade" id="catModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content bg-dark text-white p-4">
                <h4>Nueva Categoría</h4>
                <hr class="border-secondary">
                <form method="POST">
                    <div class="mb-3">
                        <label class="small text-secondary">Nombre de Categoría</label>
                        <input type="text" name="cat_nombre" class="form-control bg-black text-white border-secondary" required>
                    </div>
                    <div class="mb-4">
                        <label class="small text-secondary">Icono/Imagen (Nombre archivo)</label>
                        <input type="text" name="cat_img" class="form-control bg-black text-white border-secondary" placeholder="ej: cat_gaming.png">
                    </div>
                    <button type="submit" name="save_category" class="btn btn-galy w-100">CREAR CATEGORÍA</button>
                </form>
            </div>
        </div>
    </div>

    <div class="modal fade" id="orderModal" tabindex="-1"><div class="modal-dialog"><div class="modal-content bg-dark text-white p-4" id="orderItemsContent"></div></div></div>

    <!-- MODAL BANNERS -->
    <div class="modal fade" id="bannerModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content bg-dark text-white p-4">
                <h4 id="bannerModalTitle">Nuevo Banner</h4>
                <hr class="border-secondary">
                <form method="POST" enctype="multipart/form-data">
                    <input type="hidden" name="banner_id" id="b_form_id">
                    <div class="mb-3">
                        <label class="small text-secondary">Título</label>
                        <input type="text" name="titulo" id="b_form_titulo" class="form-control bg-black text-white border-secondary" required>
                    </div>
                    <div class="mb-3">
                        <label class="small text-secondary">Subtítulo</label>
                        <input type="text" name="subtitulo" id="b_form_subtitulo" class="form-control bg-black text-white border-secondary">
                    </div>
                    <div class="mb-3">
                        <label class="small text-secondary">Subir Imagen</label>
                        <input type="file" name="banner_file" class="form-control bg-black text-white border-secondary" accept="image/*">
                        <input type="hidden" name="imagen_url" id="b_form_img">
                    </div>
                    <div class="mb-4">
                        <label class="small text-secondary">URL de Enlace (Opcional)</label>
                        <input type="text" name="link_url" id="b_form_link" class="form-control bg-black text-white border-secondary">
                    </div>
                    <button type="submit" name="save_banner" class="btn btn-galy w-100">GUARDAR BANNER</button>
                </form>
            </div>
        </div>
    </div>
    <div id="boletaPDF" style="display:none; background:white; color:black; padding:40px; width:800px;">
        <h1 style="color:#E30613; text-align:center;">GALY XNOVATEC</h1>
        <hr>
        <p><strong>CLIENTE:</strong> <span id="pdf_cliente"></span></p>
        <p><strong>DNI:</strong> <span id="pdf_dni"></span></p>
        <p><strong>PEDIDO:</strong> #<span id="pdf_id"></span></p>
        <table style="width:100%; border-top:2px solid #000; margin-top:20px;">
            <thead><tr><th>PRODUCTO</th><th>CANT</th><th>TOTAL</th></tr></thead>
            <tbody id="pdf_items"></tbody>
        </table>
        <h3 style="text-align:right; margin-top:30px;">TOTAL: S/ <span id="pdf_total"></span></h3>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const pModal = new bootstrap.Modal(document.getElementById('productModal'));
        const bModal = new bootstrap.Modal(document.getElementById('bannerModal'));

        function nuevoBanner() {
            document.getElementById('bannerModalTitle').innerText = "Nuevo Banner";
            document.getElementById('b_form_id').value = "";
            document.getElementById('b_form_titulo').value = "";
            document.getElementById('b_form_subtitulo').value = "";
            document.getElementById('b_form_img').value = "";
            document.getElementById('b_form_link').value = "";
            bModal.show();
        }

        function editarBanner(b) {
            document.getElementById('bannerModalTitle').innerText = "Editar Banner";
            document.getElementById('b_form_id').value = b.id;
            document.getElementById('b_form_titulo').value = b.titulo;
            document.getElementById('b_form_subtitulo').value = b.subtitulo;
            document.getElementById('b_form_img').value = b.imagen_url;
            document.getElementById('b_form_link').value = b.link_url;
            bModal.show();
        }

        function previewImg(input) {
            const container = document.getElementById('img_preview_container');
            const img = document.getElementById('img_preview');
            if (input.files && input.files[0]) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    img.src = e.target.result;
                    container.style.display = 'block';
                }
                reader.readAsDataURL(input.files[0]);
            }
        }

        function removeGalleryImage(imgName, btnElement) {
            if(!confirm('¿Quitar esta imagen de la galería?')) return;
            let input = document.getElementById('form_imgs_sec');
            let images = input.value.split(',').filter(name => name.trim() !== imgName && name.trim() !== "");
            input.value = images.join(',');
            btnElement.parentElement.remove();
        }

        function previewGallery(input) {
            const container = document.getElementById('gallery_preview_container');
            const preview = document.getElementById('gallery_preview');
            // Nota: Para archivos nuevos no mostramos botón de borrar individual
            // ya que el input file multiple no permite borrado selectivo fácilmente.
            preview.innerHTML = "";
            if (input.files) {
                container.style.display = 'block';
                Array.from(input.files).forEach(file => {
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        const div = document.createElement('div');
                        div.className = "position-relative";
                        div.innerHTML = `<img src="${e.target.result}" style="height: 60px; border-radius: 5px; border: 1px solid #333;">`;
                        preview.appendChild(div);
                    }
                    reader.readAsDataURL(file);
                });
            }
        }

        function nuevoProducto() {
            document.getElementById('modalTitle').innerText = "Nuevo Producto";
            document.getElementById('form_id').value = "";
            document.querySelectorAll('#productModal input, #productModal textarea').forEach(i => { if(i.type != 'file') i.value = ""; });
            document.getElementById('form_stock').value = "10";
            document.getElementById('img_preview_container').style.display = 'none';
            document.getElementById('gallery_preview_container').style.display = 'none';
            pModal.show();
        }
        function editarProducto(p) {
            document.getElementById('modalTitle').innerText = "Editar Producto";
            document.getElementById('form_id').value = p.id;
            document.getElementById('form_nombre').value = p.nombre;
            document.getElementById('form_marca').value = p.marca;
            document.getElementById('form_p_compra').value = p.precio_compra;
            document.getElementById('form_precio_n').value = p.precio_normal;
            document.getElementById('form_precio_o').value = p.precio_oferta;
            document.getElementById('form_stock').value = p.stock;
            document.getElementById('form_cat').value = p.categoria_nombre;
            document.getElementById('form_img').value = p.imagen_url;
            document.getElementById('form_desc').value = p.descripcion;
            document.getElementById('form_specs').value = p.especificaciones;
            document.getElementById('form_label').value = p.etiqueta_especial;
            document.getElementById('form_variantes').value = p.variantes || "";
            document.getElementById('form_imgs_sec').value = p.imagenes_secundarias || "";

            if (p.imagen_url) {
                document.getElementById('img_preview').src = "img/" + p.imagen_url;
                document.getElementById('img_preview_container').style.display = 'block';
            } else {
                document.getElementById('img_preview_container').style.display = 'none';
            }

            const galPreview = document.getElementById('gallery_preview');
            galPreview.innerHTML = "";
            if (p.imagenes_secundarias) {
                document.getElementById('gallery_preview_container').style.display = 'block';
                p.imagenes_secundarias.split(',').forEach(imgName => {
                    if(imgName.trim() == "") return;
                    const div = document.createElement('div');
                    div.className = "position-relative";
                    div.innerHTML = `
                        <img src="img/${imgName.trim()}" style="height: 60px; border-radius: 5px; border: 1px solid #333;">
                        <button type="button" class="btn btn-danger btn-sm position-absolute top-0 end-0 p-0"
                                style="width:18px; height:18px; font-size:10px; border-radius:50%; transform: translate(30%, -30%);"
                                onclick="removeGalleryImage('${imgName.trim()}', this)">×</button>
                    `;
                    galPreview.appendChild(div);
                });
            } else {
                document.getElementById('gallery_preview_container').style.display = 'none';
            }
            pModal.show();
        }

        function generarReporteContable() {
            fetch('get_all_orders.php').then(r => r.json()).then(data => {
                let h = `<div style="padding:40px; font-family:sans-serif; color:black; background:white;">
                            <h1 style="color:#E30613; text-align:center; margin-bottom:5px;">GALY XNOVATEC S.A.C.</h1>
                            <p style="text-align:center; color:gray; font-size:12px; margin-top:0;">Reporte Contable de Ganancias y Costos</p>
                            <hr style="border:1px solid #eee; margin:20px 0;">

                            <table style="width:100%; border-collapse:collapse; margin-bottom:30px;">
                                <thead style="background:#f9f9f9;">
                                    <tr>
                                        <th style="text-align:left; padding:10px; border-bottom:2px solid #000;">ID</th>
                                        <th style="text-align:left; padding:10px; border-bottom:2px solid #000;">FECHA</th>
                                        <th style="text-align:right; padding:10px; border-bottom:2px solid #000;">VENTA</th>
                                        <th style="text-align:right; padding:10px; border-bottom:2px solid #000;">COSTO</th>
                                        <th style="text-align:right; padding:10px; border-bottom:2px solid #000;">GANANCIA</th>
                                    </tr>
                                </thead>
                                <tbody>`;

                let totalV = 0, totalC = 0, totalG = 0;
                data.filter(o => o.estado == 'ENTREGADO' || o.estado == 'PAGADO').forEach(o => {
                    let costoO = 0;
                    o.productos.forEach(p => costoO += (p.precio_compra * p.cantidad));
                    let gananciaO = o.total - costoO;

                    totalV += o.total; totalC += costoO; totalG += gananciaO;

                    h += `<tr>
                            <td style="padding:10px; border-bottom:1px solid #eee;">#${o.id}</td>
                            <td style="padding:10px; border-bottom:1px solid #eee;">${o.fecha.substring(0,10)}</td>
                            <td style="text-align:right; padding:10px; border-bottom:1px solid #eee;">S/ ${o.total.toFixed(2)}</td>
                            <td style="text-align:right; padding:10px; border-bottom:1px solid #eee;">S/ ${costoO.toFixed(2)}</td>
                            <td style="text-align:right; padding:10px; border-bottom:1px solid #eee; font-weight:bold; color:${gananciaO>=0?'green':'red'};">S/ ${gananciaO.toFixed(2)}</td>
                          </tr>`;
                });

                h += `</tbody></table>
                      <div style="background:#f0f0f0; padding:20px; border-radius:10px; margin-top:20px;">
                        <h3 style="margin-top:0;">Resumen del Reporte:</h3>
                        <p><strong>Ventas Netas:</strong> S/ ${totalV.toFixed(2)}</p>
                        <p><strong>Inversión (Costos):</strong> S/ ${totalC.toFixed(2)}</p>
                        <hr>
                        <h2 style="color:${totalG>=0?'green':'red'}; margin-bottom:0;">UTILIDAD NETA TOTAL: S/ ${totalG.toFixed(2)}</h2>
                      </div>
                      <p style="text-align:center; font-size:10px; color:gray; margin-top:40px;">Generado automáticamente por el Sistema Maestro GALY © 2026</p>
                    </div>`;

                const worker = html2pdf();
                const container = document.createElement('div');
                container.innerHTML = h;
                worker.from(container).set({
                    margin: 10,
                    filename: 'Reporte_Contable_GALY.pdf',
                    image: { type: 'jpeg', quality: 0.98 },
                    html2canvas: { scale: 2 },
                    jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
                }).save();
            });
        }

        function filterProducts() {
            const query = document.getElementById('productSearch').value.toLowerCase();
            const rows = document.querySelectorAll('#productTable tbody tr');
            rows.forEach(row => {
                const text = row.innerText.toLowerCase();
                row.style.display = text.includes(query) ? '' : 'none';
            });
        }

        function verItems(id) {
            fetch('get_all_orders.php').then(r => r.json()).then(data => {
                const o = data.find(x => x.id == id);
                let h = `<div class="modal-header border-0 pb-0">
                            <h5 class="modal-title fw-black">ORDEN #00${id}</h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                         </div>
                         <div class="modal-body">
                            <div class="activity-feed">`;
                o.productos.forEach(p => {
                    h += `<div class="d-flex mb-3 align-items-center bg-black p-3 rounded-4 border border-secondary border-opacity-10">
                            <img src="img/${p.imagen_url}" width="50" height="50" class="rounded me-3" style="object-fit: contain;">
                            <div class="flex-grow-1">
                                <div class="text-white fw-bold small">${p.nombre}</div>
                                <div class="text-secondary extra-small">CANT: ${p.cantidad} • PRECIO: S/ ${p.precio}</div>
                            </div>
                            <div class="text-white fw-black">S/ ${p.precio * p.cantidad}</div>
                          </div>`;
                });
                h += `</div></div>`;
                document.getElementById('orderItemsContent').innerHTML = h;
                new bootstrap.Modal(document.getElementById('orderModal')).show();
            });
        }
        function generarBoletaPDF(id) {
            fetch('get_all_orders.php').then(r => r.json()).then(data => {
                const o = data.find(x => x.id == id);
                document.getElementById('pdf_cliente').innerText = o.cliente_nombre || 'Cliente Galy';
                document.getElementById('pdf_dni').innerText = o.cliente_dni || '---';
                document.getElementById('pdf_id').innerText = o.id;
                document.getElementById('pdf_total').innerText = o.total;
                let ih = ''; o.productos.forEach(p => ih += `<tr><td>${p.nombre}</td><td>${p.cantidad}</td><td>S/ ${p.precio * p.cantidad}</td></tr>`);
                document.getElementById('pdf_items').innerHTML = ih;
                const el = document.getElementById('boletaPDF'); el.style.display = 'block';
                html2pdf().from(el).set({
                    margin: 10,
                    filename: 'Boleta_GALY_'+id+'.pdf',
                    image: { type: 'jpeg', quality: 0.98 },
                    html2canvas: { scale: 2 },
                    jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
                }).save().then(()=>el.style.display='none');
            });
        }

        function enviarBoletaWhatsApp(id, telefono, nombre) {
            // 1. Primero generamos el PDF
            generarBoletaPDF(id);

            // 2. Preparamos el mensaje
            const mensaje = `¡Hola ${nombre}! Te saluda Gustavo de GALY XNOVATEC. 🚀 Te adjunto la boleta de tu pedido #00${id}. ¡Gracias por tu compra!`;
            const phone = "51" + telefono.replace(/\s/g, '');

            // 3. Abrimos WhatsApp después de un pequeño retraso
            setTimeout(() => {
                window.open(`https://wa.me/${phone}?text=${encodeURIComponent(mensaje)}`, '_blank');
            }, 1500);
        }
        <?php if($section == 'dashboard'):
            // Definir datos de distritos dentro del script
            $dist_res = $conn->query("SELECT distrito, COUNT(*) as total FROM pedidos GROUP BY distrito ORDER BY total DESC LIMIT 5");
            $dL = []; $dV = [];
            while($r = $dist_res->fetch_assoc()){
                $dL[] = $r['distrito'] ?: 'Lima';
                $dV[] = (int)$r['total'];
            }
            if(empty($dL)) { $dL = ['Sin Datos']; $dV = [0]; }
        ?>
        function renderCharts() {
            console.log("GALY AI: Cargando gráficos con datos reales...");

            if (typeof Chart === 'undefined') {
                console.error("Error: Chart.js no cargó.");
                return;
            }

            const c1 = document.getElementById('masterChart');
            const c2 = document.getElementById('districtChart');

            if(c1) {
                new Chart(c1, {
                    type: 'line',
                    data: {
                        labels: <?php echo json_encode($sales_labels); ?>,
                        datasets: [{
                            label: 'Ventas (S/)',
                            data: <?php echo json_encode($sales_values); ?>,
                            borderColor: '#00FF41',
                            backgroundColor: 'rgba(0, 255, 65, 0.1)',
                            fill: true,
                            tension: 0.3,
                            pointRadius: 4,
                            pointBackgroundColor: '#00FF41'
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: { legend: { display: false } },
                        scales: {
                            y: { grid: { color: '#222' }, ticks: { color: '#fff' } },
                            x: { grid: { display: false }, ticks: { color: '#fff' } }
                        }
                    }
                });
            }

            if(c2) {
                new Chart(c2, {
                    type: 'bar',
                    data: {
                        labels: <?php echo json_encode($dL); ?>,
                        datasets: [{
                            data: <?php echo json_encode($dV); ?>,
                            backgroundColor: '#E30613',
                            borderRadius: 5
                        }]
                    },
                    options: {
                        indexAxis: 'y',
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: { legend: { display: false } },
                        scales: {
                            x: { grid: { color: '#222' }, ticks: { color: '#fff', stepSize: 1 } },
                            y: { grid: { display: false }, ticks: { color: '#fff', font: { weight: 'bold' } } }
                        }
                    }
                });
            }
        }
        window.onload = renderCharts;
        <?php endif; ?>
    </script>
<?php endif; ?>
</body>
</html>
