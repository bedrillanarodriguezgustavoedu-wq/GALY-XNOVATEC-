-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 18-09-2026 a las 23:08:03
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `galy`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `banners_inicio`
--

CREATE TABLE `banners_inicio` (
  `id` int(11) NOT NULL,
  `titulo` varchar(100) DEFAULT NULL,
  `subtitulo` varchar(255) DEFAULT NULL,
  `imagen_url` varchar(255) DEFAULT NULL,
  `color_fondo` varchar(20) DEFAULT '#FF0055',
  `orden` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `banners_inicio`
--

INSERT INTO `banners_inicio` (`id`, `titulo`, `subtitulo`, `imagen_url`, `color_fondo`, `orden`) VALUES
(13, 'LAPTOPS GAMER 2026', 'Nitro, Victus y LOQ disponibles', 'Captura de pantalla 2026-09-09 095351.png', '#FF0055', 0),
(14, 'SONIDO PROFESIONAL', 'Vive la música con GALY', 'sony_audifonos.jpg', '#FF0055', 0),
(15, 'DE VUELTA AL COLE', 'Equípate con lo mejor en tecnología', 'Captura de pantalla 2026-09-09 095643.png', '#FF0055', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `carrito`
--

CREATE TABLE `carrito` (
  `id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `producto_id` int(11) NOT NULL,
  `cantidad` int(11) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `catalogo_servicios`
--

CREATE TABLE `catalogo_servicios` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `precio` decimal(10,2) DEFAULT 0.00,
  `icono` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `catalogo_servicios`
--

INSERT INTO `catalogo_servicios` (`id`, `nombre`, `descripcion`, `precio`, `icono`) VALUES
(1, 'Reparación de Hardware', 'Mantenimiento preventivo de placas madre y cambio de pantallas.', 0.00, 'developer_board'),
(2, 'Optimización Gaming', 'Instalación de drivers y limpieza de sistema para máximo FPS.', 0.00, 'terminal'),
(3, 'Instalación de Software', 'Windows 11, Office y Suite Adobe completa.', 0.00, 'settings');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categorias`
--

CREATE TABLE `categorias` (
  `id` int(11) NOT NULL,
  `nombre` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `categorias`
--

INSERT INTO `categorias` (`id`, `nombre`) VALUES
(5, 'Laptops'),
(6, 'Mouses'),
(7, 'Teclados'),
(8, 'Audífonos'),
(9, 'Monitores'),
(10, 'Gaming');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `comentarios`
--

CREATE TABLE `comentarios` (
  `id` int(11) NOT NULL,
  `producto_id` int(11) DEFAULT NULL,
  `usuario_id` int(11) DEFAULT NULL,
  `comentario` text DEFAULT NULL,
  `estrellas` int(11) DEFAULT 5,
  `fecha` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cupones`
--

CREATE TABLE `cupones` (
  `id` int(11) NOT NULL,
  `codigo` varchar(20) NOT NULL,
  `descuento_porcentaje` int(11) NOT NULL,
  `fecha_expiracion` date DEFAULT NULL,
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `cupones`
--

INSERT INTO `cupones` (`id`, `codigo`, `descuento_porcentaje`, `fecha_expiracion`, `activo`) VALUES
(1, 'BIENVENIDA', 10, '2026-12-31', 1),
(2, 'GALY2025', 10, NULL, 1),
(3, 'GALY2026', 15, NULL, 1),
(4, 'XNOVATEC', 20, NULL, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `direcciones`
--

CREATE TABLE `direcciones` (
  `id` int(11) NOT NULL,
  `usuario_id` int(11) DEFAULT NULL,
  `direccion` varchar(255) NOT NULL,
  `distrito` varchar(100) DEFAULT NULL,
  `ciudad` varchar(100) DEFAULT 'Lima',
  `referencia` text DEFAULT NULL,
  `es_principal` tinyint(1) DEFAULT 0,
  `maps_url` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `direcciones`
--

INSERT INTO `direcciones` (`id`, `usuario_id`, `direccion`, `distrito`, `ciudad`, `referencia`, `es_principal`, `maps_url`) VALUES
(22, 1, '<x<<x', 'azxa<', 'x', '<zxz<', 0, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `favoritos`
--

CREATE TABLE `favoritos` (
  `id` int(11) NOT NULL,
  `usuario_id` int(11) DEFAULT NULL,
  `producto_id` int(11) DEFAULT NULL,
  `fecha_agregado` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pedidos`
--

CREATE TABLE `pedidos` (
  `id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `fecha` timestamp NOT NULL DEFAULT current_timestamp(),
  `estado` varchar(50) DEFAULT 'PAGADO',
  `metodo_pago` varchar(50) DEFAULT 'YAPE',
  `metodo_entrega` varchar(20) DEFAULT 'DOMICILIO',
  `cliente_email` varchar(100) DEFAULT NULL,
  `cliente_dni` varchar(11) DEFAULT NULL,
  `cliente_telefono` varchar(20) DEFAULT NULL,
  `direccion` text DEFAULT NULL,
  `ciudad` varchar(100) DEFAULT NULL,
  `distrito` varchar(100) DEFAULT NULL,
  `referencia` text DEFAULT NULL,
  `maps_url` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Volcado de datos para la tabla `pedidos`
--

INSERT INTO `pedidos` (`id`, `usuario_id`, `total`, `fecha`, `estado`, `metodo_pago`, `metodo_entrega`, `cliente_email`, `cliente_dni`, `cliente_telefono`, `direccion`, `ciudad`, `distrito`, `referencia`, `maps_url`) VALUES
(1, 999, 25000.00, '2026-09-11 03:12:34', 'PAGADO', 'YAPE', 'DOMICILIO', NULL, '74582136', NULL, 'Av. Las Gardenias 450', 'Lima', 'Miraflores', NULL, NULL),
(2, 999, 12500.00, '2026-09-11 03:15:32', 'PAGADO', 'YAPE', 'DOMICILIO', NULL, '74582136', NULL, 'Av. Las Gardenias 450', 'Lima', 'Miraflores', NULL, NULL),
(3, 999, 12500.00, '2026-09-11 03:17:44', 'PAGADO', 'YAPE', 'DOMICILIO', NULL, '74582136', NULL, 'Av. Las Gardenias 450', 'Lima', 'Los Olivos', NULL, NULL),
(4, 1, 450.00, '2026-09-11 19:53:14', 'PAGADO', 'YAPE', 'DOMICILIO', NULL, '74582136', NULL, 'Av. Las Gardenias 450', 'Lima', 'Los Olivos', NULL, NULL),
(5, 1, 13280.00, '2026-09-11 20:19:21', 'PAGADO', 'YAPE', 'DOMICILIO', NULL, '74582136', NULL, 'Av. Las Gardenias 450', 'Lima', 'Los Olivos', NULL, NULL),
(6, 1, 14489.00, '2026-09-14 20:29:00', 'PAGADO', 'YAPE', 'DOMICILIO', NULL, '74582136', NULL, 'Av. Las Gardenias 450', 'Lima', 'Los Olivos', NULL, NULL),
(7, 1, 12500.00, '2026-09-18 22:55:13', 'PAGADO', 'YAPE', 'DOMICILIO', NULL, '74582136', NULL, 'Av. Las Gardenias 450', 'Lima', 'Miraflores', NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pedido_productos`
--

CREATE TABLE `pedido_productos` (
  `id` int(11) NOT NULL,
  `pedido_id` int(11) NOT NULL,
  `producto_id` int(11) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `precio` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Volcado de datos para la tabla `pedido_productos`
--

INSERT INTO `pedido_productos` (`id`, `pedido_id`, `producto_id`, `cantidad`, `precio`) VALUES
(1, 1, 1, 2, 12500.00),
(2, 2, 1, 1, 12500.00),
(3, 3, 1, 1, 12500.00),
(4, 4, 2, 1, 450.00),
(5, 5, 5, 2, 390.00),
(6, 5, 1, 1, 12500.00),
(7, 6, 5, 1, 390.00),
(8, 6, 4, 2, 4800.00),
(9, 6, 9, 1, 4499.00),
(10, 7, 1, 1, 12500.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `productos`
--

CREATE TABLE `productos` (
  `id` int(11) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `marca` varchar(100) NOT NULL,
  `categoria_id` int(11) DEFAULT NULL,
  `precio_oferta` decimal(10,2) NOT NULL,
  `precio_normal` decimal(10,2) NOT NULL,
  `descuento` int(3) DEFAULT 0,
  `imagen_url` varchar(255) DEFAULT 'https://via.placeholder.com/300',
  `descripcion` text DEFAULT NULL,
  `etiqueta_especial` varchar(100) DEFAULT NULL,
  `disponible` tinyint(1) DEFAULT 1,
  `tiene_envio_gratis` tinyint(1) DEFAULT 0,
  `calificacion` decimal(2,1) DEFAULT 4.5,
  `num_revisiones` int(11) DEFAULT 0,
  `variantes` varchar(255) DEFAULT NULL,
  `especificaciones` text DEFAULT NULL,
  `categoria_nombre` varchar(100) DEFAULT NULL,
  `stock` int(11) DEFAULT 10,
  `imagenes_secundarias` text DEFAULT NULL,
  `precio_compra` decimal(10,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `productos`
--

INSERT INTO `productos` (`id`, `nombre`, `marca`, `categoria_id`, `precio_oferta`, `precio_normal`, `descuento`, `imagen_url`, `descripcion`, `etiqueta_especial`, `disponible`, `tiene_envio_gratis`, `calificacion`, `num_revisiones`, `variantes`, `especificaciones`, `categoria_nombre`, `stock`, `imagenes_secundarias`, `precio_compra`) VALUES
(1, 'HP Elitebook Pro', 'HP', NULL, 3100.00, 3500.00, 11, 'Captura de pantalla 2026-09-09 095102.png', 'Potencia y elegancia para el trabajo.', 'BUSINESS', 1, 0, 4.5, 0, NULL, 'CPU: i5, RAM: 16GB', 'Laptops', 5, NULL, 0.00),
(2, 'Acer Nitro V15', 'ACER', NULL, 3850.00, 4200.00, 8, 'Captura de pantalla 2026-09-09 100709.png', 'Rendimiento gaming superior.', 'OFERTA', 1, 0, 4.5, 0, NULL, 'RTX 4050, 16GB RAM', 'Laptops', 10, NULL, 0.00),
(3, 'MacBook Air M2', 'APPLE', NULL, 4800.00, 5200.00, 8, 'macbook.jpg', 'Chip Apple M2 ultra eficiente.', 'PREMIUM', 1, 0, 4.5, 0, NULL, 'Batería 18h, Liquid Retina', 'Laptops', 3, NULL, 0.00),
(4, 'Monitor LG UltraWide', 'LG', NULL, 899.00, 1100.00, 18, 'monitor_lg.jpg', 'Más espacio para tu productividad.', 'OFERTA', 1, 0, 4.5, 0, NULL, '29 pulgadas, IPS, HDR', 'Monitores', 15, NULL, 0.00),
(5, 'Monitor Samsung Odyssey', 'SAMSUNG', NULL, 1300.00, 1500.00, 13, 'Captura de pantalla 2026-09-09 095300.png', 'Curvatura inmersiva para gamers.', 'GAMING', 1, 0, 4.5, 0, NULL, '144Hz, 1ms', 'Monitores', 8, NULL, 0.00),
(6, 'Sony WH-1000XM5', 'SONY', NULL, 1250.00, 1450.00, 14, 'sony_audifonos.jpg', 'La mejor cancelación de ruido.', 'PREMIUM', 1, 0, 4.5, 0, NULL, 'Bluetooth 5.2, 30h batería', 'Audífonos', 20, NULL, 0.00),
(7, 'JBL Quantum 400', 'JBL', NULL, 299.00, 400.00, 25, 'jbl_audifonos.jpg', 'Escucha cada detalle del juego.', 'OFERTA', 1, 0, 4.5, 0, NULL, 'Micrófono plegable, Sonido 3D', 'Audífonos', 30, NULL, 0.00),
(8, 'Mouse Logitech G502', 'LOGITECH', NULL, 199.00, 250.00, 20, 'mouse_logitech.jpg', 'Sensor HERO de alta precisión.', 'GAMER CHOICE', 1, 0, 4.5, 0, NULL, '11 botones programables', 'Mouses', 50, NULL, 0.00),
(9, 'Teclado Keychron K2', 'KEYCHRON', NULL, 380.00, 450.00, 16, 'teclado_keychron.jpg', 'Teclado mecánico inalámbrico.', 'RECOMENDADO', 1, 0, 4.5, 0, NULL, 'Switches Brown, Retroiluminado', 'Teclados', 15, NULL, 0.00);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `servicios_tecnicos`
--

CREATE TABLE `servicios_tecnicos` (
  `id` int(11) NOT NULL,
  `usuario_id` int(11) DEFAULT NULL,
  `equipo_modelo` varchar(100) NOT NULL,
  `descripcion_falla` text DEFAULT NULL,
  `estado` enum('Recibido','En Diagnostico','Reparando','Listo','Entregado') DEFAULT 'Recibido',
  `costo_estimado` decimal(10,2) DEFAULT NULL,
  `fecha_ingreso` timestamp NOT NULL DEFAULT current_timestamp(),
  `imagen_ingreso` varchar(255) DEFAULT NULL,
  `imagen_listo` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `direccion` text DEFAULT NULL,
  `fecha_registro` timestamp NOT NULL DEFAULT current_timestamp(),
  `es_admin` tinyint(1) DEFAULT 0,
  `puntos` int(11) DEFAULT 0,
  `imagen_url` varchar(255) DEFAULT NULL,
  `fcm_token` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `email`, `password`, `telefono`, `direccion`, `fecha_registro`, `es_admin`, `puntos`, `imagen_url`, `fcm_token`) VALUES
(1, 'gusgus ', 'bedrillanarodriguezgustavoedu@gmail.com', '$2y$10$EtUV8XK5jJkjlw5FUJoyqeqXCJltl5FG4BUYH8dOcJQ727mKGhGRG', NULL, '<x<<x', '2026-09-02 16:49:09', 1, 0, NULL, NULL),
(2, 'Administrador GALY', 'admin@galyxnovatec.com', 'GALY_PRO_2025', '978104136', 'Sede Central - GALY XNOVATEC', '2026-09-08 16:45:40', 1, 0, NULL, NULL);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `banners_inicio`
--
ALTER TABLE `banners_inicio`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `carrito`
--
ALTER TABLE `carrito`
  ADD PRIMARY KEY (`id`),
  ADD KEY `usuario_id` (`usuario_id`),
  ADD KEY `producto_id` (`producto_id`);

--
-- Indices de la tabla `catalogo_servicios`
--
ALTER TABLE `catalogo_servicios`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `categorias`
--
ALTER TABLE `categorias`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `comentarios`
--
ALTER TABLE `comentarios`
  ADD PRIMARY KEY (`id`),
  ADD KEY `producto_id` (`producto_id`),
  ADD KEY `usuario_id` (`usuario_id`);

--
-- Indices de la tabla `cupones`
--
ALTER TABLE `cupones`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codigo` (`codigo`);

--
-- Indices de la tabla `direcciones`
--
ALTER TABLE `direcciones`
  ADD PRIMARY KEY (`id`),
  ADD KEY `usuario_id` (`usuario_id`);

--
-- Indices de la tabla `favoritos`
--
ALTER TABLE `favoritos`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `usuario_id` (`usuario_id`,`producto_id`),
  ADD KEY `producto_id` (`producto_id`);

--
-- Indices de la tabla `pedidos`
--
ALTER TABLE `pedidos`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `pedido_productos`
--
ALTER TABLE `pedido_productos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `pedido_id` (`pedido_id`);

--
-- Indices de la tabla `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `categoria_id` (`categoria_id`);

--
-- Indices de la tabla `servicios_tecnicos`
--
ALTER TABLE `servicios_tecnicos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `usuario_id` (`usuario_id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `banners_inicio`
--
ALTER TABLE `banners_inicio`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT de la tabla `carrito`
--
ALTER TABLE `carrito`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `catalogo_servicios`
--
ALTER TABLE `catalogo_servicios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT de la tabla `comentarios`
--
ALTER TABLE `comentarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `cupones`
--
ALTER TABLE `cupones`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=27;

--
-- AUTO_INCREMENT de la tabla `direcciones`
--
ALTER TABLE `direcciones`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT de la tabla `favoritos`
--
ALTER TABLE `favoritos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `pedidos`
--
ALTER TABLE `pedidos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de la tabla `pedido_productos`
--
ALTER TABLE `pedido_productos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT de la tabla `productos`
--
ALTER TABLE `productos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT de la tabla `servicios_tecnicos`
--
ALTER TABLE `servicios_tecnicos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `carrito`
--
ALTER TABLE `carrito`
  ADD CONSTRAINT `carrito_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `carrito_ibfk_2` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`);

--
-- Filtros para la tabla `comentarios`
--
ALTER TABLE `comentarios`
  ADD CONSTRAINT `comentarios_ibfk_1` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  ADD CONSTRAINT `comentarios_ibfk_2` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`);

--
-- Filtros para la tabla `direcciones`
--
ALTER TABLE `direcciones`
  ADD CONSTRAINT `direcciones_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`);

--
-- Filtros para la tabla `favoritos`
--
ALTER TABLE `favoritos`
  ADD CONSTRAINT `favoritos_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `favoritos_ibfk_2` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`);

--
-- Filtros para la tabla `pedido_productos`
--
ALTER TABLE `pedido_productos`
  ADD CONSTRAINT `pedido_productos_ibfk_1` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `productos`
--
ALTER TABLE `productos`
  ADD CONSTRAINT `productos_ibfk_1` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`);

--
-- Filtros para la tabla `servicios_tecnicos`
--
ALTER TABLE `servicios_tecnicos`
  ADD CONSTRAINT `servicios_tecnicos_ibfk_1` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
