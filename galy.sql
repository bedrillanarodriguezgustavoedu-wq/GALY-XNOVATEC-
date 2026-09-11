-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 12-09-2026 a las 01:46:27
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
(1, 'GAMA ALTA APPLE', 'Nuevas MacBook M3 Max', 'macbook.jpg', '#000000', 0),
(2, 'ESTILO GAMER', 'Todo en periféricos RGB', 'Captura de pantalla 2026-09-09 095351.png', '#FF0000', 0),
(3, 'SAMSUNG AI', 'El futuro está aquí', 'samsung_s24.jpg', '#1A1A1A', 0);

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
(1, 'Laptops'),
(2, 'Mouses'),
(3, 'Audífonos'),
(4, 'Monitores');

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

--
-- Volcado de datos para la tabla `comentarios`
--

INSERT INTO `comentarios` (`id`, `producto_id`, `usuario_id`, `comentario`, `estrellas`, `fecha`) VALUES
(1, 5, 1, 'HOLA', 5, '2026-09-11 20:31:42');

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
(1, 'BIENVENIDA', 10, '2026-12-31', 1);

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
  `metodo_pago` varchar(50) DEFAULT 'YAPE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Volcado de datos para la tabla `pedidos`
--

INSERT INTO `pedidos` (`id`, `usuario_id`, `total`, `fecha`, `estado`, `metodo_pago`) VALUES
(1, 999, 25000.00, '2026-09-11 03:12:34', 'PAGADO', 'YAPE'),
(2, 999, 12500.00, '2026-09-11 03:15:32', 'PAGADO', 'YAPE'),
(3, 999, 12500.00, '2026-09-11 03:17:44', 'PAGADO', 'YAPE'),
(4, 1, 450.00, '2026-09-11 19:53:14', 'PAGADO', 'YAPE'),
(5, 1, 13280.00, '2026-09-11 20:19:21', 'PAGADO', 'YAPE');

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
(6, 5, 1, 1, 12500.00);

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
  `stock` int(11) DEFAULT 10
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `productos`
--

INSERT INTO `productos` (`id`, `nombre`, `marca`, `categoria_id`, `precio_oferta`, `precio_normal`, `descuento`, `imagen_url`, `descripcion`, `etiqueta_especial`, `disponible`, `tiene_envio_gratis`, `calificacion`, `num_revisiones`, `variantes`, `especificaciones`, `categoria_nombre`, `stock`) VALUES
(1, 'MacBook Pro M3 Ultra', 'APPLE', 1, 12500.00, 14000.00, 10, 'macbook.jpg', 'La laptop más potente para creadores.', 'PREMIUM', 1, 1, 5.0, 12, 'Gris, Negro', 'Chip M3 Ultra, 64GB RAM', 'Laptops', 10),
(2, 'Mouse Logitech G Pro', 'LOGITECH', 2, 450.00, 550.00, 18, 'mouse_logitech.jpg', 'Precisión de eSports.', 'PRO', 1, 1, 4.9, 150, 'Negro', '25K DPI, Inalámbrico', 'Mouses', 10),
(3, 'Sony WH-1000XM5 Pro', 'SONY', 3, 1400.00, 1699.00, 17, 'sony_audifonos.jpg', 'Cancelación de ruido líder.', 'TOP', 1, 1, 4.8, 85, 'Negro, Plata', '30h Batería, Hi-Res', 'Audífonos', 10),
(4, 'Samsung Galaxy S24 AI', 'SAMSUNG', 1, 4800.00, 5600.00, 14, 'samsung_s24.jpg', 'El smartphone con inteligencia artificial.', 'OFERTA', 1, 1, 4.7, 30, 'Titanio', 'Cámara 200MP, IA', 'Smartphones', 10),
(5, 'Teclado Keychron RGB', 'KEYCHRON', 2, 390.00, 480.00, 18, 'teclado_keychron.jpg', 'Escritura mecánica fluida.', 'NUEVO', 1, 0, 4.6, 22, 'Brown Switch', 'RGB, Bluetooth', 'Mouses', 10),
(6, 'Monitor LG Ultra 144Hz', 'LG', 4, 1200.00, 1500.00, 20, 'monitor_lg.jpg', 'Fluidez total en tus juegos.', 'GAMING', 1, 1, 4.5, 40, 'Negro', '1ms, IPS, G-Sync', 'Monitores', 10),
(7, 'iPad Air M2 Design', 'APPLE', 1, 2900.00, 3400.00, 14, 'ipad_air.jpg', 'Creatividad sin límites.', NULL, 1, 1, 4.9, 15, 'Azul, Púrpura', 'Chip M2, 11 pulgadas', 'Laptops', 10),
(8, 'Audífonos JBL Bass+', 'JBL', 3, 299.00, 399.00, 25, 'jbl_audifonos.jpg', 'Bajos profundos JBL.', 'DESCUENTO', 1, 0, 4.4, 10, 'Blanco', 'Pure Bass, Inalámbricos', 'Audífonos', 10),
(9, 'GALY X-Gen i9 PRO', 'GALY', 1, 4499.00, 5200.00, 13, 'laptop_1.jpg', 'La bestia de GALY XNOVATEC.', '¡NUEVO!', 1, 1, 5.0, 5, 'Negro', 'i9 13va Gen, RTX 4070', 'Laptops', 10),
(10, 'Sony Alpha A7 IV', 'SONY', 1, 8500.00, 9500.00, 10, 'Captura de pantalla 2026-09-10 095425.png', 'Cámara profesional 4K.', 'FOTO', 1, 1, 4.9, 8, 'Negro', '33MP, Video 10-bit', 'Laptops', 10),
(11, 'MacBook Pro M3 Ultra - Serie 1', 'APPLE', 1, 12375.00, 13860.00, 10, 'macbook.jpg', 'La laptop más potente para creadores.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M3 Ultra, 64GB RAM', 'Laptops', 10),
(12, 'Mouse Logitech G Pro - Serie 1', 'LOGITECH', 2, 445.50, 544.50, 18, 'mouse_logitech.jpg', 'Precisión de eSports.', NULL, 1, 0, 4.5, 0, NULL, '25K DPI, Inalámbrico', 'Mouses', 10),
(13, 'Sony WH-1000XM5 Pro - Serie 1', 'SONY', 3, 1386.00, 1682.01, 17, 'sony_audifonos.jpg', 'Cancelación de ruido líder.', NULL, 1, 0, 4.5, 0, NULL, '30h Batería, Hi-Res', 'Audífonos', 10),
(14, 'Samsung Galaxy S24 AI - Serie 1', 'SAMSUNG', 1, 4752.00, 5544.00, 14, 'samsung_s24.jpg', 'El smartphone con inteligencia artificial.', NULL, 1, 0, 4.5, 0, NULL, 'Cámara 200MP, IA', 'Smartphones', 10),
(15, 'Teclado Keychron RGB - Serie 1', 'KEYCHRON', 2, 386.10, 475.20, 18, 'teclado_keychron.jpg', 'Escritura mecánica fluida.', NULL, 1, 0, 4.5, 0, NULL, 'RGB, Bluetooth', 'Mouses', 10),
(16, 'Monitor LG Ultra 144Hz - Serie 1', 'LG', 4, 1188.00, 1485.00, 20, 'monitor_lg.jpg', 'Fluidez total en tus juegos.', NULL, 1, 0, 4.5, 0, NULL, '1ms, IPS, G-Sync', 'Monitores', 10),
(17, 'iPad Air M2 Design - Serie 1', 'APPLE', 1, 2871.00, 3366.00, 14, 'ipad_air.jpg', 'Creatividad sin límites.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M2, 11 pulgadas', 'Laptops', 10),
(18, 'Audífonos JBL Bass+ - Serie 1', 'JBL', 3, 296.01, 395.01, 25, 'jbl_audifonos.jpg', 'Bajos profundos JBL.', NULL, 1, 0, 4.5, 0, NULL, 'Pure Bass, Inalámbricos', 'Audífonos', 10),
(19, 'GALY X-Gen i9 PRO - Serie 1', 'GALY', 1, 4454.01, 5148.00, 13, 'laptop_1.jpg', 'La bestia de GALY XNOVATEC.', NULL, 1, 0, 4.5, 0, NULL, 'i9 13va Gen, RTX 4070', 'Laptops', 10),
(20, 'Sony Alpha A7 IV - Serie 1', 'SONY', 1, 8415.00, 9405.00, 10, 'Captura de pantalla 2026-09-10 095425.png', 'Cámara profesional 4K.', NULL, 1, 0, 4.5, 0, NULL, '33MP, Video 10-bit', 'Laptops', 10),
(21, 'MacBook Pro M3 Ultra - Serie 2', 'APPLE', 1, 12250.00, 13720.00, 10, 'macbook.jpg', 'La laptop más potente para creadores.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M3 Ultra, 64GB RAM', 'Laptops', 10),
(22, 'Mouse Logitech G Pro - Serie 2', 'LOGITECH', 2, 441.00, 539.00, 18, 'mouse_logitech.jpg', 'Precisión de eSports.', NULL, 1, 0, 4.5, 0, NULL, '25K DPI, Inalámbrico', 'Mouses', 10),
(23, 'Sony WH-1000XM5 Pro - Serie 2', 'SONY', 3, 1372.00, 1665.02, 17, 'sony_audifonos.jpg', 'Cancelación de ruido líder.', NULL, 1, 0, 4.5, 0, NULL, '30h Batería, Hi-Res', 'Audífonos', 10),
(24, 'Samsung Galaxy S24 AI - Serie 2', 'SAMSUNG', 1, 4704.00, 5488.00, 14, 'samsung_s24.jpg', 'El smartphone con inteligencia artificial.', NULL, 1, 0, 4.5, 0, NULL, 'Cámara 200MP, IA', 'Smartphones', 10),
(25, 'Teclado Keychron RGB - Serie 2', 'KEYCHRON', 2, 382.20, 470.40, 18, 'mouse_logitech.jpg', 'Escritura mecánica fluida.', NULL, 1, 0, 4.5, 0, NULL, 'RGB, Bluetooth', 'Mouses', 10),
(26, 'Monitor LG Ultra 144Hz - Serie 2', 'LG', 4, 1176.00, 1470.00, 20, 'monitor_lg.jpg', 'Fluidez total en tus juegos.', NULL, 1, 0, 4.5, 0, NULL, '1ms, IPS, G-Sync', 'Monitores', 10),
(27, 'iPad Air M2 Design - Serie 2', 'APPLE', 1, 2842.00, 3332.00, 14, 'laptop_1.jpg', 'Creatividad sin límites.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M2, 11 pulgadas', 'Laptops', 10),
(28, 'Audífonos JBL Bass+ - Serie 2', 'JBL', 3, 293.02, 391.02, 25, 'jbl_audifonos.jpg', 'Bajos profundos JBL.', NULL, 1, 0, 4.5, 0, NULL, 'Pure Bass, Inalámbricos', 'Audífonos', 10),
(29, 'GALY X-Gen i9 PRO - Serie 2', 'GALY', 1, 4409.02, 5096.00, 13, 'laptop_1.jpg', 'La bestia de GALY XNOVATEC.', NULL, 1, 0, 4.5, 0, NULL, 'i9 13va Gen, RTX 4070', 'Laptops', 10),
(30, 'Sony Alpha A7 IV - Serie 2', 'SONY', 1, 8330.00, 9310.00, 10, 'laptop_1.jpg', 'Cámara profesional 4K.', NULL, 1, 0, 4.5, 0, NULL, '33MP, Video 10-bit', 'Laptops', 10),
(31, 'MacBook Pro M3 Ultra - Serie 3', 'APPLE', 1, 12125.00, 13580.00, 10, 'laptop_1.jpg', 'La laptop más potente para creadores.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M3 Ultra, 64GB RAM', 'Laptops', 10),
(32, 'Mouse Logitech G Pro - Serie 3', 'LOGITECH', 2, 436.50, 533.50, 18, 'mouse_logitech.jpg', 'Precisión de eSports.', NULL, 1, 0, 4.5, 0, NULL, '25K DPI, Inalámbrico', 'Mouses', 10),
(33, 'Sony WH-1000XM5 Pro - Serie 3', 'SONY', 3, 1358.00, 1648.03, 17, 'sony_audifonos.jpg', 'Cancelación de ruido líder.', NULL, 1, 0, 4.5, 0, NULL, '30h Batería, Hi-Res', 'Audífonos', 10),
(34, 'Samsung Galaxy S24 AI - Serie 3', 'SAMSUNG', 1, 4656.00, 5432.00, 14, 'samsung_s24.jpg', 'El smartphone con inteligencia artificial.', NULL, 1, 0, 4.5, 0, NULL, 'Cámara 200MP, IA', 'Smartphones', 10),
(35, 'Teclado Keychron RGB - Serie 3', 'KEYCHRON', 2, 378.30, 465.60, 18, 'mouse_logitech.jpg', 'Escritura mecánica fluida.', NULL, 1, 0, 4.5, 0, NULL, 'RGB, Bluetooth', 'Mouses', 10),
(36, 'Monitor LG Ultra 144Hz - Serie 3', 'LG', 4, 1164.00, 1455.00, 20, 'monitor_lg.jpg', 'Fluidez total en tus juegos.', NULL, 1, 0, 4.5, 0, NULL, '1ms, IPS, G-Sync', 'Monitores', 10),
(37, 'iPad Air M2 Design - Serie 3', 'APPLE', 1, 2813.00, 3298.00, 14, 'laptop_1.jpg', 'Creatividad sin límites.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M2, 11 pulgadas', 'Laptops', 10),
(38, 'Audífonos JBL Bass+ - Serie 3', 'JBL', 3, 290.03, 387.03, 25, 'jbl_audifonos.jpg', 'Bajos profundos JBL.', NULL, 1, 0, 4.5, 0, NULL, 'Pure Bass, Inalámbricos', 'Audífonos', 10),
(39, 'GALY X-Gen i9 PRO - Serie 3', 'GALY', 1, 4364.03, 5044.00, 13, 'laptop_1.jpg', 'La bestia de GALY XNOVATEC.', NULL, 1, 0, 4.5, 0, NULL, 'i9 13va Gen, RTX 4070', 'Laptops', 10),
(40, 'Sony Alpha A7 IV - Serie 3', 'SONY', 1, 8245.00, 9215.00, 10, 'laptop_1.jpg', 'Cámara profesional 4K.', NULL, 1, 0, 4.5, 0, NULL, '33MP, Video 10-bit', 'Laptops', 10),
(41, 'MacBook Pro M3 Ultra - Serie 4', 'APPLE', 1, 12000.00, 13440.00, 10, 'laptop_1.jpg', 'La laptop más potente para creadores.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M3 Ultra, 64GB RAM', 'Laptops', 10),
(42, 'Mouse Logitech G Pro - Serie 4', 'LOGITECH', 2, 432.00, 528.00, 18, 'mouse_logitech.jpg', 'Precisión de eSports.', NULL, 1, 0, 4.5, 0, NULL, '25K DPI, Inalámbrico', 'Mouses', 10),
(43, 'Sony WH-1000XM5 Pro - Serie 4', 'SONY', 3, 1344.00, 1631.04, 17, 'sony_audifonos.jpg', 'Cancelación de ruido líder.', NULL, 1, 0, 4.5, 0, NULL, '30h Batería, Hi-Res', 'Audífonos', 10),
(44, 'Samsung Galaxy S24 AI - Serie 4', 'SAMSUNG', 1, 4608.00, 5376.00, 14, 'samsung_s24.jpg', 'El smartphone con inteligencia artificial.', NULL, 1, 0, 4.5, 0, NULL, 'Cámara 200MP, IA', 'Smartphones', 10),
(45, 'Teclado Keychron RGB - Serie 4', 'KEYCHRON', 2, 374.40, 460.80, 18, 'mouse_logitech.jpg', 'Escritura mecánica fluida.', NULL, 1, 0, 4.5, 0, NULL, 'RGB, Bluetooth', 'Mouses', 10),
(46, 'Monitor LG Ultra 144Hz - Serie 4', 'LG', 4, 1152.00, 1440.00, 20, 'monitor_lg.jpg', 'Fluidez total en tus juegos.', NULL, 1, 0, 4.5, 0, NULL, '1ms, IPS, G-Sync', 'Monitores', 10),
(47, 'iPad Air M2 Design - Serie 4', 'APPLE', 1, 2784.00, 3264.00, 14, 'laptop_1.jpg', 'Creatividad sin límites.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M2, 11 pulgadas', 'Laptops', 10),
(48, 'Audífonos JBL Bass+ - Serie 4', 'JBL', 3, 287.04, 383.04, 25, 'jbl_audifonos.jpg', 'Bajos profundos JBL.', NULL, 1, 0, 4.5, 0, NULL, 'Pure Bass, Inalámbricos', 'Audífonos', 10),
(49, 'GALY X-Gen i9 PRO - Serie 4', 'GALY', 1, 4319.04, 4992.00, 13, 'laptop_1.jpg', 'La bestia de GALY XNOVATEC.', NULL, 1, 0, 4.5, 0, NULL, 'i9 13va Gen, RTX 4070', 'Laptops', 10),
(50, 'Sony Alpha A7 IV - Serie 4', 'SONY', 1, 8160.00, 9120.00, 10, 'laptop_1.jpg', 'Cámara profesional 4K.', NULL, 1, 0, 4.5, 0, NULL, '33MP, Video 10-bit', 'Laptops', 10),
(51, 'MacBook Pro M3 Ultra - Serie 5', 'APPLE', 1, 11875.00, 13300.00, 10, 'laptop_1.jpg', 'La laptop más potente para creadores.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M3 Ultra, 64GB RAM', 'Laptops', 10),
(52, 'Mouse Logitech G Pro - Serie 5', 'LOGITECH', 2, 427.50, 522.50, 18, 'mouse_logitech.jpg', 'Precisión de eSports.', NULL, 1, 0, 4.5, 0, NULL, '25K DPI, Inalámbrico', 'Mouses', 10),
(53, 'Sony WH-1000XM5 Pro - Serie 5', 'SONY', 3, 1330.00, 1614.05, 17, 'sony_audifonos.jpg', 'Cancelación de ruido líder.', NULL, 1, 0, 4.5, 0, NULL, '30h Batería, Hi-Res', 'Audífonos', 10),
(54, 'Samsung Galaxy S24 AI - Serie 5', 'SAMSUNG', 1, 4560.00, 5320.00, 14, 'samsung_s24.jpg', 'El smartphone con inteligencia artificial.', NULL, 1, 0, 4.5, 0, NULL, 'Cámara 200MP, IA', 'Smartphones', 10),
(55, 'Teclado Keychron RGB - Serie 5', 'KEYCHRON', 2, 370.50, 456.00, 18, 'mouse_logitech.jpg', 'Escritura mecánica fluida.', NULL, 1, 0, 4.5, 0, NULL, 'RGB, Bluetooth', 'Mouses', 10),
(56, 'Monitor LG Ultra 144Hz - Serie 5', 'LG', 4, 1140.00, 1425.00, 20, 'monitor_lg.jpg', 'Fluidez total en tus juegos.', NULL, 1, 0, 4.5, 0, NULL, '1ms, IPS, G-Sync', 'Monitores', 10),
(57, 'iPad Air M2 Design - Serie 5', 'APPLE', 1, 2755.00, 3230.00, 14, 'laptop_1.jpg', 'Creatividad sin límites.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M2, 11 pulgadas', 'Laptops', 10),
(58, 'Audífonos JBL Bass+ - Serie 5', 'JBL', 3, 284.05, 379.05, 25, 'jbl_audifonos.jpg', 'Bajos profundos JBL.', NULL, 1, 0, 4.5, 0, NULL, 'Pure Bass, Inalámbricos', 'Audífonos', 10),
(59, 'GALY X-Gen i9 PRO - Serie 5', 'GALY', 1, 4274.05, 4940.00, 13, 'laptop_1.jpg', 'La bestia de GALY XNOVATEC.', NULL, 1, 0, 4.5, 0, NULL, 'i9 13va Gen, RTX 4070', 'Laptops', 10),
(60, 'Sony Alpha A7 IV - Serie 5', 'SONY', 1, 8075.00, 9025.00, 10, 'laptop_1.jpg', 'Cámara profesional 4K.', NULL, 1, 0, 4.5, 0, NULL, '33MP, Video 10-bit', 'Laptops', 10),
(61, 'MacBook Pro M3 Ultra - Serie 6', 'APPLE', 1, 11750.00, 13160.00, 10, 'laptop_1.jpg', 'La laptop más potente para creadores.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M3 Ultra, 64GB RAM', 'Laptops', 10),
(62, 'Mouse Logitech G Pro - Serie 6', 'LOGITECH', 2, 423.00, 517.00, 18, 'mouse_logitech.jpg', 'Precisión de eSports.', NULL, 1, 0, 4.5, 0, NULL, '25K DPI, Inalámbrico', 'Mouses', 10),
(63, 'Sony WH-1000XM5 Pro - Serie 6', 'SONY', 3, 1316.00, 1597.06, 17, 'sony_audifonos.jpg', 'Cancelación de ruido líder.', NULL, 1, 0, 4.5, 0, NULL, '30h Batería, Hi-Res', 'Audífonos', 10),
(64, 'Samsung Galaxy S24 AI - Serie 6', 'SAMSUNG', 1, 4512.00, 5264.00, 14, 'samsung_s24.jpg', 'El smartphone con inteligencia artificial.', NULL, 1, 0, 4.5, 0, NULL, 'Cámara 200MP, IA', 'Smartphones', 10),
(65, 'Teclado Keychron RGB - Serie 6', 'KEYCHRON', 2, 366.60, 451.20, 18, 'mouse_logitech.jpg', 'Escritura mecánica fluida.', NULL, 1, 0, 4.5, 0, NULL, 'RGB, Bluetooth', 'Mouses', 10),
(66, 'Monitor LG Ultra 144Hz - Serie 6', 'LG', 4, 1128.00, 1410.00, 20, 'monitor_lg.jpg', 'Fluidez total en tus juegos.', NULL, 1, 0, 4.5, 0, NULL, '1ms, IPS, G-Sync', 'Monitores', 10),
(67, 'iPad Air M2 Design - Serie 6', 'APPLE', 1, 2726.00, 3196.00, 14, 'laptop_1.jpg', 'Creatividad sin límites.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M2, 11 pulgadas', 'Laptops', 10),
(68, 'Audífonos JBL Bass+ - Serie 6', 'JBL', 3, 281.06, 375.06, 25, 'jbl_audifonos.jpg', 'Bajos profundos JBL.', NULL, 1, 0, 4.5, 0, NULL, 'Pure Bass, Inalámbricos', 'Audífonos', 10),
(69, 'GALY X-Gen i9 PRO - Serie 6', 'GALY', 1, 4229.06, 4888.00, 13, 'laptop_1.jpg', 'La bestia de GALY XNOVATEC.', NULL, 1, 0, 4.5, 0, NULL, 'i9 13va Gen, RTX 4070', 'Laptops', 10),
(70, 'Sony Alpha A7 IV - Serie 6', 'SONY', 1, 7990.00, 8930.00, 10, 'laptop_1.jpg', 'Cámara profesional 4K.', NULL, 1, 0, 4.5, 0, NULL, '33MP, Video 10-bit', 'Laptops', 10),
(71, 'MacBook Pro M3 Ultra - Serie 7', 'APPLE', 1, 11625.00, 13020.00, 10, 'laptop_1.jpg', 'La laptop más potente para creadores.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M3 Ultra, 64GB RAM', 'Laptops', 10),
(72, 'Mouse Logitech G Pro - Serie 7', 'LOGITECH', 2, 418.50, 511.50, 18, 'mouse_logitech.jpg', 'Precisión de eSports.', NULL, 1, 0, 4.5, 0, NULL, '25K DPI, Inalámbrico', 'Mouses', 10),
(73, 'Sony WH-1000XM5 Pro - Serie 7', 'SONY', 3, 1302.00, 1580.07, 17, 'sony_audifonos.jpg', 'Cancelación de ruido líder.', NULL, 1, 0, 4.5, 0, NULL, '30h Batería, Hi-Res', 'Audífonos', 10),
(74, 'Samsung Galaxy S24 AI - Serie 7', 'SAMSUNG', 1, 4464.00, 5208.00, 14, 'samsung_s24.jpg', 'El smartphone con inteligencia artificial.', NULL, 1, 0, 4.5, 0, NULL, 'Cámara 200MP, IA', 'Smartphones', 10),
(75, 'Teclado Keychron RGB - Serie 7', 'KEYCHRON', 2, 362.70, 446.40, 18, 'mouse_logitech.jpg', 'Escritura mecánica fluida.', NULL, 1, 0, 4.5, 0, NULL, 'RGB, Bluetooth', 'Mouses', 10),
(76, 'Monitor LG Ultra 144Hz - Serie 7', 'LG', 4, 1116.00, 1395.00, 20, 'monitor_lg.jpg', 'Fluidez total en tus juegos.', NULL, 1, 0, 4.5, 0, NULL, '1ms, IPS, G-Sync', 'Monitores', 10),
(77, 'iPad Air M2 Design - Serie 7', 'APPLE', 1, 2697.00, 3162.00, 14, 'laptop_1.jpg', 'Creatividad sin límites.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M2, 11 pulgadas', 'Laptops', 10),
(78, 'Audífonos JBL Bass+ - Serie 7', 'JBL', 3, 278.07, 371.07, 25, 'jbl_audifonos.jpg', 'Bajos profundos JBL.', NULL, 1, 0, 4.5, 0, NULL, 'Pure Bass, Inalámbricos', 'Audífonos', 10),
(79, 'GALY X-Gen i9 PRO - Serie 7', 'GALY', 1, 4184.07, 4836.00, 13, 'laptop_1.jpg', 'La bestia de GALY XNOVATEC.', NULL, 1, 0, 4.5, 0, NULL, 'i9 13va Gen, RTX 4070', 'Laptops', 10),
(80, 'Sony Alpha A7 IV - Serie 7', 'SONY', 1, 7905.00, 8835.00, 10, 'laptop_1.jpg', 'Cámara profesional 4K.', NULL, 1, 0, 4.5, 0, NULL, '33MP, Video 10-bit', 'Laptops', 10),
(81, 'MacBook Pro M3 Ultra - Serie 8', 'APPLE', 1, 11500.00, 12880.00, 10, 'laptop_1.jpg', 'La laptop más potente para creadores.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M3 Ultra, 64GB RAM', 'Laptops', 10),
(82, 'Mouse Logitech G Pro - Serie 8', 'LOGITECH', 2, 414.00, 506.00, 18, 'mouse_logitech.jpg', 'Precisión de eSports.', NULL, 1, 0, 4.5, 0, NULL, '25K DPI, Inalámbrico', 'Mouses', 10),
(83, 'Sony WH-1000XM5 Pro - Serie 8', 'SONY', 3, 1288.00, 1563.08, 17, 'sony_audifonos.jpg', 'Cancelación de ruido líder.', NULL, 1, 0, 4.5, 0, NULL, '30h Batería, Hi-Res', 'Audífonos', 10),
(84, 'Samsung Galaxy S24 AI - Serie 8', 'SAMSUNG', 1, 4416.00, 5152.00, 14, 'samsung_s24.jpg', 'El smartphone con inteligencia artificial.', NULL, 1, 0, 4.5, 0, NULL, 'Cámara 200MP, IA', 'Smartphones', 10),
(85, 'Teclado Keychron RGB - Serie 8', 'KEYCHRON', 2, 358.80, 441.60, 18, 'mouse_logitech.jpg', 'Escritura mecánica fluida.', NULL, 1, 0, 4.5, 0, NULL, 'RGB, Bluetooth', 'Mouses', 10),
(86, 'Monitor LG Ultra 144Hz - Serie 8', 'LG', 4, 1104.00, 1380.00, 20, 'monitor_lg.jpg', 'Fluidez total en tus juegos.', NULL, 1, 0, 4.5, 0, NULL, '1ms, IPS, G-Sync', 'Monitores', 10),
(87, 'iPad Air M2 Design - Serie 8', 'APPLE', 1, 2668.00, 3128.00, 14, 'laptop_1.jpg', 'Creatividad sin límites.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M2, 11 pulgadas', 'Laptops', 10),
(88, 'Audífonos JBL Bass+ - Serie 8', 'JBL', 3, 275.08, 367.08, 25, 'jbl_audifonos.jpg', 'Bajos profundos JBL.', NULL, 1, 0, 4.5, 0, NULL, 'Pure Bass, Inalámbricos', 'Audífonos', 10),
(89, 'GALY X-Gen i9 PRO - Serie 8', 'GALY', 1, 4139.08, 4784.00, 13, 'laptop_1.jpg', 'La bestia de GALY XNOVATEC.', NULL, 1, 0, 4.5, 0, NULL, 'i9 13va Gen, RTX 4070', 'Laptops', 10),
(90, 'Sony Alpha A7 IV - Serie 8', 'SONY', 1, 7820.00, 8740.00, 10, 'laptop_1.jpg', 'Cámara profesional 4K.', NULL, 1, 0, 4.5, 0, NULL, '33MP, Video 10-bit', 'Laptops', 10),
(91, 'MacBook Pro M3 Ultra - Serie 9', 'APPLE', 1, 11375.00, 12740.00, 10, 'laptop_1.jpg', 'La laptop más potente para creadores.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M3 Ultra, 64GB RAM', 'Laptops', 10),
(92, 'Mouse Logitech G Pro - Serie 9', 'LOGITECH', 2, 409.50, 500.50, 18, 'mouse_logitech.jpg', 'Precisión de eSports.', NULL, 1, 0, 4.5, 0, NULL, '25K DPI, Inalámbrico', 'Mouses', 10),
(93, 'Sony WH-1000XM5 Pro - Serie 9', 'SONY', 3, 1274.00, 1546.09, 17, 'sony_audifonos.jpg', 'Cancelación de ruido líder.', NULL, 1, 0, 4.5, 0, NULL, '30h Batería, Hi-Res', 'Audífonos', 10),
(94, 'Samsung Galaxy S24 AI - Serie 9', 'SAMSUNG', 1, 4368.00, 5096.00, 14, 'samsung_s24.jpg', 'El smartphone con inteligencia artificial.', NULL, 1, 0, 4.5, 0, NULL, 'Cámara 200MP, IA', 'Smartphones', 10),
(95, 'Teclado Keychron RGB - Serie 9', 'KEYCHRON', 2, 354.90, 436.80, 18, 'mouse_logitech.jpg', 'Escritura mecánica fluida.', NULL, 1, 0, 4.5, 0, NULL, 'RGB, Bluetooth', 'Mouses', 10),
(96, 'Monitor LG Ultra 144Hz - Serie 9', 'LG', 4, 1092.00, 1365.00, 20, 'monitor_lg.jpg', 'Fluidez total en tus juegos.', NULL, 1, 0, 4.5, 0, NULL, '1ms, IPS, G-Sync', 'Monitores', 10),
(97, 'iPad Air M2 Design - Serie 9', 'APPLE', 1, 2639.00, 3094.00, 14, 'laptop_1.jpg', 'Creatividad sin límites.', NULL, 1, 0, 4.5, 0, NULL, 'Chip M2, 11 pulgadas', 'Laptops', 10),
(98, 'Audífonos JBL Bass+ - Serie 9', 'JBL', 3, 272.09, 363.09, 25, 'jbl_audifonos.jpg', 'Bajos profundos JBL.', NULL, 1, 0, 4.5, 0, NULL, 'Pure Bass, Inalámbricos', 'Audífonos', 10),
(99, 'GALY X-Gen i9 PRO - Serie 9', 'GALY', 1, 4094.09, 4732.00, 13, 'laptop_1.jpg', 'La bestia de GALY XNOVATEC.', NULL, 1, 0, 4.5, 0, NULL, 'i9 13va Gen, RTX 4070', 'Laptops', 10),
(100, 'Sony Alpha A7 IV - Serie 9', 'SONY', 1, 7735.00, 8645.00, 10, 'laptop_1.jpg', 'Cámara profesional 4K.', NULL, 1, 0, 4.5, 0, NULL, '33MP, Video 10-bit', 'Laptops', 10);

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
  `fecha_ingreso` timestamp NOT NULL DEFAULT current_timestamp()
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
  `es_admin` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `nombre`, `email`, `password`, `telefono`, `direccion`, `fecha_registro`, `es_admin`) VALUES
(1, 'gusgus ', 'bedrillanarodriguezgustavoedu@gmail.com', '$2y$10$EtUV8XK5jJkjlw5FUJoyqeqXCJltl5FG4BUYH8dOcJQ727mKGhGRG', NULL, '<x<<x', '2026-09-02 16:49:09', 0),
(2, 'Administrador GALY', 'admin@galyxnovatec.com', 'GALY_PRO_2025', '978104136', 'Sede Central - GALY XNOVATEC', '2026-09-08 16:45:40', 1);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `banners_inicio`
--
ALTER TABLE `banners_inicio`
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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `categorias`
--
ALTER TABLE `categorias`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `comentarios`
--
ALTER TABLE `comentarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `cupones`
--
ALTER TABLE `cupones`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `pedido_productos`
--
ALTER TABLE `pedido_productos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `productos`
--
ALTER TABLE `productos`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=101;

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
