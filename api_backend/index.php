<?php
require_once 'db.php';

// Obtener Categorías
$cats_res = $conn->query("SELECT * FROM categorias");
$cats = [];
while($c = $cats_res->fetch_assoc()) $cats[] = $c;

// Filtro de Categoría y Búsqueda
$cat_filter = isset($_GET['cat']) ? $_GET['cat'] : '';
$search_filter = isset($_GET['q']) ? $_GET['q'] : '';

// Obtener Productos
$sql = "SELECT * FROM productos WHERE 1=1";
if($cat_filter) {
    $sql .= " AND categoria_nombre = '" . $conn->real_escape_string($cat_filter) . "'";
}
if($search_filter) {
    $q = $conn->real_escape_string($search_filter);
    $sql .= " AND (nombre LIKE '%$q%' OR marca LIKE '%$q%' OR descripcion LIKE '%$q%')";
}
$sql .= " ORDER BY id DESC";
$prods_res = $conn->query($sql);

// Obtener Banners
$banners_res = $conn->query("SELECT * FROM banners_inicio");
?>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>GALY XNOVATEC | Lo mejor en Tecnología</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;700;900&display=swap" rel="stylesheet">
    <style>
        :root { --galy-red: #E30613; --bg-dark: #000000; --card-dark: #0a0a0a; }
        body { background-color: var(--bg-dark); color: white; font-family: 'Inter', sans-serif; }
        .navbar { background-color: rgba(0,0,0,0.9); backdrop-filter: blur(10px); border-bottom: 1px solid rgba(255,255,255,0.05); }
        .nav-link { color: #888 !important; font-weight: bold; font-size: 14px; margin: 0 10px; }
        .nav-link:hover, .nav-link.active { color: var(--galy-red) !important; }

        /* Banners */
        .carousel-item { height: 450px; }
        .carousel-item img { object-fit: cover; height: 100%; filter: brightness(0.6); }
        .carousel-caption { bottom: 20%; text-align: left; left: 10%; }
        .carousel-caption h1 { font-weight: 900; font-size: 3.5rem; letter-spacing: -2px; }

        /* Categorías */
        .cat-chip { background: #111; border: 1px solid #333; padding: 10px 25px; border-radius: 30px; color: white; text-decoration: none; transition: 0.3s; margin-right: 10px; display: inline-block; }
        .cat-chip:hover, .cat-chip.active { background: var(--galy-red); border-color: var(--galy-red); color: white; }

        /* Tarjetas de Producto */
        .product-card { background: var(--card-dark); border: 1px solid rgba(255,255,255,0.05); border-radius: 20px; transition: 0.4s; overflow: hidden; height: 100%; }
        .product-card:hover { transform: translateY(-10px); border-color: var(--galy-red); box-shadow: 0 10px 30px rgba(227,6,19,0.2); }
        .img-container { height: 200px; padding: 20px; background: #000; display: flex; align-items: center; justify-content: center; }
        .img-container img { max-height: 100%; max-width: 100%; transition: 0.5s; }
        .product-card:hover .img-container img { transform: scale(1.1); }

        .badge-promo { background: var(--galy-red); color: white; padding: 5px 12px; border-radius: 10px; font-weight: 900; font-size: 10px; position: absolute; top: 15px; left: 15px; }

        .btn-buy { background: var(--galy-red); color: white; border: none; border-radius: 12px; width: 100%; padding: 12px; font-weight: bold; transition: 0.3s; }
        .btn-buy:hover { background: #b3050f; box-shadow: 0 5px 15px rgba(227,6,19,0.4); color: white; }

        .footer { background: #050505; border-top: 1px solid rgba(255,255,255,0.05); padding: 80px 0; }
    </style>
</head>
<body>

<!-- NAVBAR -->
<nav class="navbar navbar-expand-lg navbar-dark sticky-top p-3">
    <div class="container">
        <a class="navbar-brand fw-black d-flex align-items-center" href="index.php">
            <img src="img/logo_2.png" width="40" class="me-2">
            GALY <span class="text-danger">XNOVATEC</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <!-- BUSCADOR WEB -->
            <form class="ms-auto d-flex position-relative me-lg-4" method="GET" action="index.php#productos">
                <i class="bi bi-search position-absolute" style="left: 15px; top: 10px; color: #555;"></i>
                <input type="text" name="q" class="form-control bg-dark border-secondary text-white ps-5 shadow-none" placeholder="Buscar laptop, mouse..." value="<?php echo $search_filter; ?>" style="border-radius: 20px; width: 300px;">
            </form>
            <ul class="navbar-nav">
                <li class="nav-item"><a class="nav-link active" href="index.php">INICIO</a></li>
                <li class="nav-item"><a class="nav-link" href="#productos">PRODUCTOS</a></li>
                <li class="nav-item"><a class="nav-link text-white border border-danger border-opacity-25 rounded px-3 ms-lg-3" href="admin_web.php"><i class="bi bi-person-lock me-2"></i>ADMIN</a></li>
            </ul>
        </div>
    </div>
</nav>

<!-- HERO / BANNERS -->
<div id="heroCarousel" class="carousel slide" data-bs-ride="carousel">
    <div class="carousel-inner">
        <?php
        $first = true;
        while($b = $banners_res->fetch_assoc()):
        ?>
        <div class="carousel-item <?php echo $first?'active':''; ?>">
            <img src="img/<?php echo $b['imagen_url']; ?>" class="d-block w-100">
            <div class="carousel-caption">
                <span class="badge bg-danger mb-3">OFERTA ESPECIAL</span>
                <h1 class="text-uppercase"><?php echo $b['titulo']; ?></h1>
                <p class="fs-5 opacity-75"><?php echo $b['subtitulo']; ?></p>
                <a href="#productos" class="btn btn-galy px-5 py-3 mt-3">VER CATÁLOGO</a>
            </div>
        </div>
        <?php $first = false; endwhile; ?>
    </div>
</div>

<div class="container my-5" id="productos">
    <!-- CATEGORÍAS -->
    <div class="mb-5 overflow-auto text-nowrap pb-2">
        <a href="index.php#productos" class="cat-chip <?php echo !$cat_filter?'active':''; ?>">Todos</a>
        <?php foreach($cats as $c): ?>
            <a href="?cat=<?php echo urlencode($c['nombre']); ?>#productos" class="cat-chip <?php echo $cat_filter==$c['nombre']?'active':''; ?>">
                <?php echo $c['nombre']; ?>
            </a>
        <?php endforeach; ?>
    </div>

    <!-- LISTA PRODUCTOS -->
    <h2 class="fw-black mb-4"><?php echo $cat_filter ? strtoupper($cat_filter) : 'RECOMENDADOS PARA TI'; ?></h2>
    <div class="row g-4">
        <?php while($p = $prods_res->fetch_assoc()): ?>
        <div class="col-6 col-md-4 col-lg-3">
            <div class="product-card position-relative">
                <?php if($p['precio_normal'] > $p['precio_oferta']): ?>
                    <div class="badge-promo">-<?php echo round((($p['precio_normal'] - $p['precio_oferta']) / $p['precio_normal']) * 100); ?>%</div>
                <?php endif; ?>
                <div class="img-container">
                    <img src="img/<?php echo $p['imagen_url']; ?>" alt="<?php echo $p['nombre']; ?>">
                </div>
                <div class="p-4">
                    <small class="text-secondary fw-bold text-uppercase"><?php echo $p['marca']; ?></small>
                    <h6 class="fw-bold text-white text-truncate mt-1"><?php echo $p['nombre']; ?></h6>
                    <div class="mt-3 d-flex align-items-end justify-content-between mb-3">
                        <div>
                            <?php if($p['precio_normal'] > $p['precio_oferta']): ?>
                                <del class="text-secondary small">S/ <?php echo number_format($p['precio_normal'], 0); ?></del><br>
                            <?php endif; ?>
                            <span class="fs-4 fw-black text-white">S/ <?php echo number_format($p['precio_oferta'], 0); ?></span>
                        </div>
                        <span class="badge bg-success bg-opacity-10 text-success small border border-success border-opacity-25">En Stock</span>
                    </div>
                    <?php
                        $msg = "Hola GALY, estoy interesado en el producto: " . $p['nombre'] . " (S/ " . $p['precio_oferta'] . ")";
                        $wa_link = "https://wa.me/51978104136?text=" . urlencode($msg);
                    ?>
                    <a href="<?php echo $wa_link; ?>" target="_blank" class="btn btn-buy"><i class="bi bi-whatsapp me-2"></i>COMPRAR</a>
                </div>
            </div>
        </div>
        <?php endwhile; ?>
    </div>
</div>

<!-- FOOTER -->
<footer class="footer mt-5">
    <div class="container text-center">
        <img src="img/logo_2.png" width="60" class="mb-4 opacity-50">
        <h4 class="fw-black mb-1">GALY <span class="text-danger">XNOVATEC</span></h4>
        <p class="text-secondary small">Lo último en tecnología al alcance de un clic.</p>
        <div class="d-flex justify-content-center gap-4 my-4 fs-4 text-secondary">
            <i class="bi bi-facebook"></i>
            <i class="bi bi-instagram"></i>
            <i class="bi bi-tiktok"></i>
        </div>
        <hr class="border-secondary opacity-25">
        <p class="text-secondary extra-small mt-4">© 2026 GALY XNOVATEC S.A.C. - Todos los derechos reservados.</p>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
