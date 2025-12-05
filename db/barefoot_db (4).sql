-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1:3306
-- Tiempo de generación: 19-11-2025 a las 22:31:24
-- Versión del servidor: 9.1.0
-- Versión de PHP: 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;


CREATE DATABASE  IF NOT EXISTS `barefoot_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;;
USE `barefoot_db`;

--
-- Base de datos: `barefoot_db`
--

DELIMITER $$
--
-- Procedimientos
--
DROP PROCEDURE IF EXISTS `sp_registrar_movimiento`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_movimiento` (IN `p_producto_id` BIGINT, IN `p_tipo` VARCHAR(30), IN `p_cantidad` INT, IN `p_motivo` VARCHAR(500), IN `p_usuario_id` BIGINT, IN `p_numero_documento` VARCHAR(100))   BEGIN
    DECLARE v_stock_anterior INT;
    DECLARE v_stock_nuevo INT;
    
    -- Obtener stock actual
    SELECT stock INTO v_stock_anterior FROM productos WHERE id = p_producto_id;
    
    -- Calcular nuevo stock
    IF p_tipo IN ('ENTRADA', 'AJUSTE_ENTRADA', 'DEVOLUCION') THEN
        SET v_stock_nuevo = v_stock_anterior + p_cantidad;
    ELSE
        SET v_stock_nuevo = v_stock_anterior - p_cantidad;
    END IF;
    
    -- Validar stock suficiente
    IF v_stock_nuevo < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuficiente';
    END IF;
    
    -- Registrar movimiento
    INSERT INTO movimientos_inventario 
        (producto_id, tipo, cantidad, stock_anterior, stock_nuevo, motivo, usuario_id, numero_documento, fecha_movimiento)
    VALUES 
        (p_producto_id, p_tipo, p_cantidad, v_stock_anterior, v_stock_nuevo, p_motivo, p_usuario_id, p_numero_documento, NOW());
    
    -- Actualizar stock del producto
    UPDATE productos SET stock = v_stock_nuevo WHERE id = p_producto_id;
    
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `alertas_inventario`
--

DROP TABLE IF EXISTS `alertas_inventario`;
CREATE TABLE IF NOT EXISTS `alertas_inventario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `producto_id` bigint NOT NULL,
  `tipo` varchar(30) NOT NULL,
  `mensaje` varchar(255) NOT NULL,
  `stock_actual` int DEFAULT NULL,
  `stock_minimo` int DEFAULT NULL,
  `leida` tinyint(1) DEFAULT '0',
  `activa` tinyint(1) DEFAULT '1',
  `fecha_creacion` datetime DEFAULT NULL,
  `fecha_leida` datetime DEFAULT NULL,
  `leida_por` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_producto` (`producto_id`),
  KEY `idx_activa` (`activa`),
  KEY `idx_leida` (`leida`),
  KEY `FKauilfyb0jj84dym5ushn8ebie` (`leida_por`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `alertas_inventario`
--

INSERT INTO `alertas_inventario` (`id`, `producto_id`, `tipo`, `mensaje`, `stock_actual`, `stock_minimo`, `leida`, `activa`, `fecha_creacion`, `fecha_leida`, `leida_por`) VALUES
(1, 16, 'STOCK_BAJO', 'El producto Limited Edition Oro tiene stock bajo: 5 unidades', 5, 10, 0, 1, '2025-11-08 21:23:01', NULL, NULL),
(2, 17, 'STOCK_BAJO', 'El producto Vintage Collection Marrón tiene stock bajo: 8 unidades', 8, 10, 0, 1, '2025-11-08 21:23:01', NULL, NULL),
(3, 19, 'STOCK_BAJO', 'El producto Saguaro - Zapatillas Vigor II - Sport Barefoot - Negro tiene stock bajo: 4 unidades', 4, 10, 0, 1, '2025-11-08 21:23:01', NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `detalle_pedidos`
--

DROP TABLE IF EXISTS `detalle_pedidos`;
CREATE TABLE IF NOT EXISTS `detalle_pedidos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `pedido_id` bigint NOT NULL,
  `producto_id` bigint NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` double NOT NULL,
  `subtotal` double NOT NULL,
  `personalizacion` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_pedido` (`pedido_id`),
  KEY `idx_producto` (`producto_id`)
) ENGINE=MyISAM AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `detalle_pedidos`
--

INSERT INTO `detalle_pedidos` (`id`, `pedido_id`, `producto_id`, `cantidad`, `precio_unitario`, `subtotal`, `personalizacion`) VALUES
(1, 3, 1, 2, 299, 299, NULL),
(2, 2, 2, 2, 279, 558, NULL),
(3, 3, 3, 2, 259, 518, NULL),
(4, 2, 6, 1, 289, 578, NULL),
(5, 1, 6, 2, 289, 578, NULL),
(6, 1, 7, 2, 399, 798, NULL),
(7, 2, 9, 1, 379, 379, NULL),
(8, 1, 9, 2, 379, 379, NULL),
(9, 3, 10, 1, 369, 369, NULL),
(10, 1, 13, 2, 419, 419, NULL),
(11, 1, 14, 2, 399, 399, NULL),
(12, 3, 15, 2, 389, 778, NULL),
(13, 2, 17, 1, 329, 329, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `movimientos_inventario`
--

DROP TABLE IF EXISTS `movimientos_inventario`;
CREATE TABLE IF NOT EXISTS `movimientos_inventario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `producto_id` bigint NOT NULL,
  `tipo` varchar(30) NOT NULL,
  `cantidad` int NOT NULL,
  `stock_anterior` int NOT NULL,
  `stock_nuevo` int NOT NULL,
  `motivo` varchar(500) DEFAULT NULL,
  `referencia` varchar(500) DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `numero_documento` varchar(100) DEFAULT NULL,
  `fecha_movimiento` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_producto` (`producto_id`),
  KEY `idx_fecha` (`fecha_movimiento`),
  KEY `idx_tipo` (`tipo`),
  KEY `FKakkxu577al9vljuur2ahk0lo` (`usuario_id`)
) ENGINE=MyISAM AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `movimientos_inventario`
--

INSERT INTO `movimientos_inventario` (`id`, `producto_id`, `tipo`, `cantidad`, `stock_anterior`, `stock_nuevo`, `motivo`, `referencia`, `usuario_id`, `numero_documento`, `fecha_movimiento`) VALUES
(1, 1, 'ENTRADA', 50, 0, 50, 'Stock inicial - Compra inicial de inventario', NULL, 1, NULL, '2025-09-09 21:23:00'),
(2, 2, 'ENTRADA', 50, 0, 50, 'Stock inicial - Compra inicial de inventario', NULL, 1, NULL, '2025-09-09 21:23:00'),
(3, 3, 'ENTRADA', 50, 0, 50, 'Stock inicial - Compra inicial de inventario', NULL, 1, NULL, '2025-09-09 21:23:00'),
(4, 4, 'ENTRADA', 50, 0, 50, 'Stock inicial - Compra inicial de inventario', NULL, 1, NULL, '2025-09-09 21:23:00'),
(5, 5, 'ENTRADA', 50, 0, 50, 'Stock inicial - Compra inicial de inventario', NULL, 1, NULL, '2025-09-09 21:23:00'),
(6, 6, 'ENTRADA', 50, 0, 50, 'Stock inicial - Compra inicial de inventario', NULL, 1, NULL, '2025-09-09 21:23:00'),
(7, 7, 'ENTRADA', 50, 0, 50, 'Stock inicial - Compra inicial de inventario', NULL, 1, NULL, '2025-09-09 21:23:00'),
(8, 8, 'ENTRADA', 50, 0, 50, 'Stock inicial - Compra inicial de inventario', NULL, 1, NULL, '2025-09-09 21:23:00'),
(9, 9, 'ENTRADA', 50, 0, 50, 'Stock inicial - Compra inicial de inventario', NULL, 1, NULL, '2025-09-09 21:23:00'),
(10, 10, 'ENTRADA', 50, 0, 50, 'Stock inicial - Compra inicial de inventario', NULL, 1, NULL, '2025-09-09 21:23:00'),
(11, 1, 'SALIDA', 26, 50, 25, 'Venta del 01/11/2025', NULL, 1, NULL, '2025-10-30 21:23:00'),
(12, 2, 'SALIDA', 26, 50, 26, 'Venta del 06/11/2025', NULL, 1, NULL, '2025-10-25 21:23:00'),
(13, 3, 'SALIDA', 8, 50, 39, 'Venta del 18/10/2025', NULL, 1, NULL, '2025-10-10 21:23:00'),
(14, 4, 'SALIDA', 27, 50, 23, 'Venta del 24/10/2025', NULL, 1, NULL, '2025-10-27 21:23:00'),
(15, 5, 'SALIDA', 17, 50, 19, 'Venta del 04/11/2025', NULL, 1, NULL, '2025-11-06 21:23:00'),
(16, 6, 'SALIDA', 33, 50, 30, 'Venta del 16/10/2025', NULL, 1, NULL, '2025-11-01 21:23:00'),
(17, 7, 'SALIDA', 5, 50, 35, 'Venta del 19/10/2025', NULL, 1, NULL, '2025-10-31 21:23:00'),
(18, 8, 'SALIDA', 18, 50, 33, 'Venta del 18/10/2025', NULL, 1, NULL, '2025-10-31 21:23:00'),
(19, 9, 'SALIDA', 12, 50, 31, 'Venta del 20/10/2025', NULL, 1, NULL, '2025-10-16 21:23:00'),
(20, 10, 'SALIDA', 33, 50, 33, 'Venta del 31/10/2025', NULL, 1, NULL, '2025-11-03 21:23:00'),
(21, 11, 'SALIDA', 5, 50, 29, 'Venta del 20/10/2025', NULL, 1, NULL, '2025-10-19 21:23:00'),
(22, 12, 'SALIDA', 17, 50, 44, 'Venta del 07/11/2025', NULL, 1, NULL, '2025-11-07 21:23:00'),
(23, 13, 'SALIDA', 7, 50, 37, 'Venta del 04/11/2025', NULL, 1, NULL, '2025-10-11 21:23:00'),
(24, 14, 'SALIDA', 11, 50, 35, 'Venta del 10/10/2025', NULL, 1, NULL, '2025-10-12 21:23:00'),
(25, 15, 'SALIDA', 22, 50, 39, 'Venta del 29/10/2025', NULL, 1, NULL, '2025-11-06 21:23:00'),
(26, 16, 'SALIDA', 17, 50, 23, 'Venta del 22/10/2025', NULL, 1, NULL, '2025-10-20 21:23:00'),
(27, 17, 'SALIDA', 19, 50, 33, 'Venta del 21/10/2025', NULL, 1, NULL, '2025-10-13 21:23:00'),
(28, 19, 'SALIDA', 23, 50, 32, 'Venta del 30/10/2025', NULL, 1, NULL, '2025-10-31 21:23:00'),
(29, 8, 'AJUSTE_ENTRADA', 5, 18, 23, 'Ajuste por inventario físico - Stock encontrado no registrado', NULL, 1, NULL, '2025-11-01 21:23:01'),
(30, 9, 'AJUSTE_ENTRADA', 5, 15, 20, 'Ajuste por inventario físico - Stock encontrado no registrado', NULL, 1, NULL, '2025-11-01 21:23:01'),
(31, 16, 'AJUSTE_ENTRADA', 5, 5, 10, 'Ajuste por inventario físico - Stock encontrado no registrado', NULL, 1, NULL, '2025-11-01 21:23:01'),
(32, 17, 'AJUSTE_ENTRADA', 5, 8, 13, 'Ajuste por inventario físico - Stock encontrado no registrado', NULL, 1, NULL, '2025-11-01 21:23:01'),
(33, 19, 'AJUSTE_ENTRADA', 5, 4, 9, 'Ajuste por inventario físico - Stock encontrado no registrado', NULL, 1, NULL, '2025-11-01 21:23:01');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
CREATE TABLE IF NOT EXISTS `pedidos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `numero_pedido` varchar(50) NOT NULL,
  `subtotal` double NOT NULL,
  `descuento` double DEFAULT '0',
  `costo_envio` double DEFAULT '0',
  `total` double NOT NULL,
  `estado` varchar(20) NOT NULL,
  `metodo_pago` varchar(30) DEFAULT NULL,
  `direccion_envio` varchar(500) DEFAULT NULL,
  `notas` varchar(500) DEFAULT NULL,
  `fecha_pedido` datetime DEFAULT NULL,
  `fecha_actualizacion` datetime DEFAULT NULL,
  `fecha_entrega` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero_pedido` (`numero_pedido`),
  KEY `idx_usuario` (`usuario_id`),
  KEY `idx_estado` (`estado`),
  KEY `idx_fecha` (`fecha_pedido`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `pedidos`
--

INSERT INTO `pedidos` (`id`, `usuario_id`, `numero_pedido`, `subtotal`, `descuento`, `costo_envio`, `total`, `estado`, `metodo_pago`, `direccion_envio`, `notas`, `fecha_pedido`, `fecha_actualizacion`, `fecha_entrega`) VALUES
(1, 2, 'PED-0000000001', 453.16, 0, 15, 433.03, 'PENDIENTE', 'TARJETA_CREDITO', 'Av. Principal 734, Lima, Perú', NULL, '2025-10-11 16:23:46', '2025-11-08 16:23:46', NULL),
(2, 4, 'PED-0000000002', 810.47, 0, 0, 1190.27, 'CONFIRMADO', 'TARJETA_CREDITO', 'Av. Principal 229, Lima, Perú', NULL, '2025-10-22 16:23:46', '2025-11-08 16:23:46', NULL),
(3, 5, 'PED-0000000003', 495.37, 0, 15, 762.83, 'PREPARANDO', 'YAPE', 'Av. Principal 872, Lima, Perú', NULL, '2025-10-11 16:23:46', '2025-11-08 16:23:46', NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `productos`
--

DROP TABLE IF EXISTS `productos`;
CREATE TABLE IF NOT EXISTS `productos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `categoria` varchar(50) DEFAULT NULL,
  `color` varchar(50) DEFAULT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `destacado` bit(1) NOT NULL,
  `fecha_actualizacion` datetime(6) DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `imagen_url` varchar(500) DEFAULT NULL,
  `material` varchar(100) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `precio` double NOT NULL,
  `precio_descuento` double DEFAULT NULL,
  `stock` int NOT NULL,
  `talla` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Volcado de datos para la tabla `productos`
--

INSERT INTO `productos` (`id`, `activo`, `categoria`, `color`, `descripcion`, `destacado`, `fecha_actualizacion`, `fecha_creacion`, `imagen_url`, `material`, `nombre`, `precio`, `precio_descuento`, `stock`, `talla`) VALUES
(1, b'1', 'Casual', 'Negro', 'Calzado barefoot casual perfecto para el día a día. Diseño elegante y minimalista que se adapta a cualquier ocasión.', b'1', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=500', 'Cuero', 'Urban Barefoot Negro', 299, NULL, 25, '40'),
(2, b'1', 'Casual', 'Marrón', 'Zapato barefoot casual con suela flexible. Ideal para largas caminatas urbanas con máximo confort.', b'1', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1520256862855-398228c41684?w=500', 'Cuero', 'City Walker Marrón', 279, 249, 30, '41'),
(3, b'1', 'Casual', 'Beige', 'Tu compañero diario. Ligero, transpirable y con diseño versátil para combinar con todo tu guardarropa.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1514989940723-e8e51635b782?w=500', 'Lona', 'Everyday Comfort Beige', 259, NULL, 40, '39'),
(4, b'1', 'Deportivo', 'Negro', 'Calzado deportivo barefoot con máxima flexibilidad. Perfecto para entrenamientos funcionales y gimnasio.', b'1', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500', 'Malla', 'Sport Flex Pro', 349, 299, 35, '42'),
(5, b'1', 'Deportivo', 'Azul', 'Diseñado para movimiento natural en tus entrenamientos. Suela de contacto total con el suelo.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=500', 'Sintético', 'Active Motion Azul', 329, NULL, 28, '41'),
(6, b'1', 'Deportivo', 'Gris', 'Lo esencial para tus sesiones de gym. Ligero, resistente y con excelente agarre.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=500', 'Malla', 'Gym Essential Gris', 289, NULL, 32, '40'),
(7, b'1', 'Formal', 'Negro', 'Elegancia profesional sin sacrificar la salud de tus pies. Para la oficina o eventos formales.', b'1', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500', 'Cuero', 'Classic Barefoot Negro', 399, NULL, 20, '42'),
(8, b'1', 'Formal', 'Marrón', 'Estilo ejecutivo con filosofía barefoot. Comodidad durante toda la jornada laboral.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1614252369475-531eba835eb1?w=500', 'Cuero', 'Business Elite Marrón', 389, 349, 18, '41'),
(9, b'1', 'Formal', 'Negro', 'Diseño sobrio y profesional. Ideal para presentaciones y reuniones importantes.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=500', 'Gamuza', 'Professional Touch Negro', 379, NULL, 15, '40'),
(10, b'1', 'Running', 'Rojo', 'Libertad total para tus pies mientras corres. Suela minimalista con máximo feedback.', b'1', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=500', 'Malla', 'Runner Freedom Rojo', 369, 329, 45, '42'),
(11, b'1', 'Running', 'Verde', 'Para corredores que buscan conexión natural con el terreno. Ultra ligero y flexible.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1552346154-21d32810aba3?w=500', 'Sintético', 'Trail Master Verde', 379, NULL, 38, '41'),
(12, b'1', 'Running', 'Blanco', 'Minimalismo y velocidad. Diseñado para corredores de medio fondo y largas distancias.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=500', 'Malla', 'Speed Natural Blanco', 359, NULL, 42, '40'),
(13, b'1', 'Senderismo', 'Marrón', 'Para aventureros que aman sentir el terreno. Resistente pero flexible para cualquier camino.', b'1', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?w=500', 'Cuero', 'Mountain Trek Pro', 419, 379, 22, '43'),
(14, b'1', 'Senderismo', 'Verde', 'Explora nuevos senderos con máxima libertad. Suela antideslizante y protección reforzada.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1520256788229-d4640c632e4b?w=500', 'Sintético', 'Trail Explorer Verde', 399, NULL, 26, '42'),
(15, b'1', 'Senderismo', 'Gris', 'Tu compañero perfecto para rutas de montaña. Transpirable y resistente al agua.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1605348532760-6753d2c43329?w=500', 'Sintético', 'Adventure Seeker Gris', 389, 349, 20, '41'),
(16, b'1', 'Casual', 'Beige', 'Edición limitada con detalles dorados. Solo quedan pocas unidades.', b'1', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=500', 'Cuero', 'Limited Edition Oro', 459, 399, 5, '40'),
(17, b'1', 'Casual', 'Marrón', 'Estilo retro con tecnología moderna. Stock limitado por alta demanda.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1603808033192-082d6919d3e1?w=500', 'Gamuza', 'Vintage Collection Marrón', 329, NULL, 8, '41'),
(18, b'0', 'Casual', 'Negro', 'Este producto ya no está disponible.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=500', 'Cuero', 'Modelo Descontinuado', 299, NULL, 0, '40'),
(19, b'1', 'Deportivo', 'Negro', 'Zapatillas Saguaro Vigor II: Todos los modelos de saguaro que encontraras en nuestra pagina, son productos veganos, respetuosos con el medio ambiente y los animales, de producción sostenible. Técnicamente ofrecen las 3 características bases de zapatos minimalistas/ergonómicos para tus pies.', b'1', '2025-11-08 21:01:12.267890', '2025-11-08 21:01:12.266744', 'https://saguarobarefoot.pe/cdn/shop/files/0a81fd_8a6f40383ece40e9b0fffca46c672b23_mv2.webp?v=1718385844&width=533', 'Sintético', 'Saguaro - Zapatillas Vigor II - Sport Barefoot - Negro', 199, 179, 4, '36');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE IF NOT EXISTS `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `apellido` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `telefono` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rol` enum('USUARIO','ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USUARIO',
  `fecha_registro` datetime DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_email` (`email`),
  KEY `idx_rol` (`rol`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `email`, `password`, `nombre`, `apellido`, `telefono`, `rol`, `fecha_registro`, `activo`) VALUES
(1, 'admin@barefoot.com', '$2a$10$xQn0EjgHWMJE4fLVGFGZ5.rXW7xCHt5TjE8CZMfKbOGW7KPXVVxvy', 'Administrador', 'Principal', '+51 999 888 777', 'ADMIN', '2025-10-09 18:05:20', 1),
(2, 'usuario@ejemplo.com', '$2a$10$8TQN6eQfz7t1K1NvHY.IqORmWl/8jXYGJhGkYYfJPv.KWz6f8mDc6', 'Juan', 'Pérez', '+51 987 654 321', 'USUARIO', '2025-10-09 18:05:20', 1),
(3, 'reyes@gmail.com', '$2a$10$Q5krmRVpKknP48.nEcOnf.YLldF5BY.ORaPI6pjHlzTGjXPJCAS2O', 'Hector', 'Reyes', '997256008', 'ADMIN', '2025-10-09 23:49:52', 1),
(4, 'fatama@gmail.com', '$2a$10$KXepm3/JeIOxkGKvZDemtepMZFKoxyxhSYV2UNyFzbPv8rpuRZN8m', 'Sebastian', 'Fatama', '987456123', 'USUARIO', '2025-10-10 14:01:45', 1),
(5, 'meza@gmail.com', '$2a$10$JfQUilop7d1g06b5k5jZKuc4l4SfRF9IbQxsY8Aclgh7UVpw15Fxm', 'Luis', 'Meza', '987412356', 'USUARIO', '2025-10-22 21:00:18', 1);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_inventario_resumen`
-- (Véase abajo para la vista actual)
--
DROP VIEW IF EXISTS `vista_inventario_resumen`;
CREATE TABLE IF NOT EXISTS `vista_inventario_resumen` (
`id` bigint
,`nombre` varchar(100)
,`categoria` varchar(50)
,`stock_actual` int
,`precio` double
,`total_entradas` decimal(32,0)
,`total_salidas` decimal(32,0)
,`total_movimientos` bigint
,`valor_stock` double
,`estado_stock` varchar(7)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_movimientos_recientes`
-- (Véase abajo para la vista actual)
--
DROP VIEW IF EXISTS `vista_movimientos_recientes`;
CREATE TABLE IF NOT EXISTS `vista_movimientos_recientes` (
`id` bigint
,`fecha_movimiento` datetime
,`tipo` varchar(30)
,`cantidad` int
,`stock_anterior` int
,`stock_nuevo` int
,`motivo` varchar(500)
,`producto_nombre` varchar(100)
,`producto_categoria` varchar(50)
,`usuario_nombre` varchar(100)
,`numero_documento` varchar(100)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_pedidos_resumen`
-- (Véase abajo para la vista actual)
--
DROP VIEW IF EXISTS `vista_pedidos_resumen`;
CREATE TABLE IF NOT EXISTS `vista_pedidos_resumen` (
`id` bigint
,`numero_pedido` varchar(50)
,`estado` varchar(20)
,`total` double
,`fecha_pedido` datetime
,`cliente_nombre` varchar(100)
,`cliente_apellido` varchar(100)
,`cliente_email` varchar(100)
,`cantidad_productos` bigint
,`total_items` decimal(32,0)
);

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_inventario_resumen`
--
DROP TABLE IF EXISTS `vista_inventario_resumen`;

DROP VIEW IF EXISTS `vista_inventario_resumen`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_inventario_resumen`  AS SELECT `p`.`id` AS `id`, `p`.`nombre` AS `nombre`, `p`.`categoria` AS `categoria`, `p`.`stock` AS `stock_actual`, `p`.`precio` AS `precio`, coalesce(sum((case when (`m`.`tipo` in ('ENTRADA','AJUSTE_ENTRADA','DEVOLUCION')) then `m`.`cantidad` else 0 end)),0) AS `total_entradas`, coalesce(sum((case when (`m`.`tipo` in ('SALIDA','AJUSTE_SALIDA','MERMA')) then `m`.`cantidad` else 0 end)),0) AS `total_salidas`, count(distinct `m`.`id`) AS `total_movimientos`, (`p`.`precio` * `p`.`stock`) AS `valor_stock`, (case when (`p`.`stock` = 0) then 'AGOTADO' when (`p`.`stock` <= 3) then 'CRITICO' when (`p`.`stock` < 10) then 'BAJO' else 'NORMAL' end) AS `estado_stock` FROM (`productos` `p` left join `movimientos_inventario` `m` on((`p`.`id` = `m`.`producto_id`))) WHERE (`p`.`activo` = true) GROUP BY `p`.`id`, `p`.`nombre`, `p`.`categoria`, `p`.`stock`, `p`.`precio` ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_movimientos_recientes`
--
DROP TABLE IF EXISTS `vista_movimientos_recientes`;

DROP VIEW IF EXISTS `vista_movimientos_recientes`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_movimientos_recientes`  AS SELECT `m`.`id` AS `id`, `m`.`fecha_movimiento` AS `fecha_movimiento`, `m`.`tipo` AS `tipo`, `m`.`cantidad` AS `cantidad`, `m`.`stock_anterior` AS `stock_anterior`, `m`.`stock_nuevo` AS `stock_nuevo`, `m`.`motivo` AS `motivo`, `p`.`nombre` AS `producto_nombre`, `p`.`categoria` AS `producto_categoria`, `u`.`nombre` AS `usuario_nombre`, `m`.`numero_documento` AS `numero_documento` FROM ((`movimientos_inventario` `m` join `productos` `p` on((`m`.`producto_id` = `p`.`id`))) left join `usuarios` `u` on((`m`.`usuario_id` = `u`.`id`))) ORDER BY `m`.`fecha_movimiento` DESC LIMIT 0, 50 ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_pedidos_resumen`
--
DROP TABLE IF EXISTS `vista_pedidos_resumen`;

DROP VIEW IF EXISTS `vista_pedidos_resumen`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_pedidos_resumen`  AS SELECT `p`.`id` AS `id`, `p`.`numero_pedido` AS `numero_pedido`, `p`.`estado` AS `estado`, `p`.`total` AS `total`, `p`.`fecha_pedido` AS `fecha_pedido`, `u`.`nombre` AS `cliente_nombre`, `u`.`apellido` AS `cliente_apellido`, `u`.`email` AS `cliente_email`, count(`dp`.`id`) AS `cantidad_productos`, sum(`dp`.`cantidad`) AS `total_items` FROM ((`pedidos` `p` join `usuarios` `u` on((`p`.`usuario_id` = `u`.`id`))) left join `detalle_pedidos` `dp` on((`p`.`id` = `dp`.`pedido_id`))) GROUP BY `p`.`id`, `p`.`numero_pedido`, `p`.`estado`, `p`.`total`, `p`.`fecha_pedido`, `u`.`nombre`, `u`.`apellido`, `u`.`email` ;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

	


