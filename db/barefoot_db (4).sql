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

CREATE DATABASE IF NOT EXISTS `barefoot_db`
    /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */
    /*!80016 DEFAULT ENCRYPTION='N' */;
USE `barefoot_db`;

-- =========================================================
-- PROCEDIMIENTO sp_registrar_movimiento
-- =========================================================
DELIMITER $$
DROP PROCEDURE IF EXISTS `sp_registrar_movimiento` $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_registrar_movimiento` (
    IN `p_producto_id` BIGINT,
    IN `p_tipo` VARCHAR(30),
    IN `p_cantidad` INT,
    IN `p_motivo` VARCHAR(500),
    IN `p_usuario_id` BIGINT,
    IN `p_numero_documento` VARCHAR(100)
)
BEGIN
    DECLARE v_stock_anterior INT;
    DECLARE v_stock_nuevo INT;

    SELECT stock INTO v_stock_anterior FROM productos WHERE id = p_producto_id;

    IF p_tipo IN ('ENTRADA', 'AJUSTE_ENTRADA', 'DEVOLUCION') THEN
        SET v_stock_nuevo = v_stock_anterior + p_cantidad;
    ELSE
        SET v_stock_nuevo = v_stock_anterior - p_cantidad;
    END IF;

    IF v_stock_nuevo < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuficiente';
    END IF;

    INSERT INTO movimientos_inventario
        (producto_id, tipo, cantidad, stock_anterior, stock_nuevo, motivo, usuario_id, numero_documento, fecha_movimiento)
    VALUES
        (p_producto_id, p_tipo, p_cantidad, v_stock_anterior, v_stock_nuevo,
         p_motivo, p_usuario_id, p_numero_documento, NOW());

    UPDATE productos SET stock = v_stock_nuevo WHERE id = p_producto_id;
END$$
DELIMITER ;

-- =========================================================
-- TABLA alertas_inventario
-- =========================================================
DROP TABLE IF EXISTS `alertas_inventario`;
CREATE TABLE `alertas_inventario` (
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
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `alertas_inventario`
(`id`, `producto_id`, `tipo`, `mensaje`, `stock_actual`, `stock_minimo`, `leida`, `activa`, `fecha_creacion`)
VALUES
(1, 16, 'STOCK_BAJO', 'El producto Limited Edition Oro tiene stock bajo: 5 unidades', 5, 10, 0, 1, '2025-11-08 21:23:01'),
(2, 17, 'STOCK_BAJO', 'El producto Vintage Collection Marrón tiene stock bajo: 8 unidades', 8, 10, 0, 1, '2025-11-08 21:23:01'),
(3, 19, 'STOCK_BAJO', 'El producto Saguaro - Zapatillas Vigor II - Sport Barefoot - Negro tiene stock bajo: 4 unidades', 4, 10, 0, 1, '2025-11-08 21:23:01');

-- =========================================================
-- TABLA detalle_pedidos
-- =========================================================
DROP TABLE IF EXISTS `detalle_pedidos`;
CREATE TABLE `detalle_pedidos` (
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
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `detalle_pedidos` VALUES
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

-- =========================================================
-- TABLA movimientos_inventario
-- =========================================================
DROP TABLE IF EXISTS `movimientos_inventario`;
CREATE TABLE `movimientos_inventario` (
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
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- (Tus 33 inserts aquí sin cambios)

-- =========================================================
-- TABLA pedidos
-- =========================================================
DROP TABLE IF EXISTS `pedidos`;
CREATE TABLE `pedidos` (
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
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `pedidos` VALUES
(1, 2, 'PED-0000000001', 453.16, 0, 15, 433.03, 'PENDIENTE', 'TARJETA_CREDITO', 'Av. Principal 734, Lima, Perú', NULL, '2025-10-11 16:23:46', '2025-11-08 16:23:46', NULL),
(2, 4, 'PED-0000000002', 810.47, 0, 0, 1190.27, 'CONFIRMADO', 'TARJETA_CREDITO', 'Av. Principal 229, Lima, Perú', NULL, '2025-10-22 16:23:46', '2025-11-08 16:23:46', NULL),
(3, 5, 'PED-0000000003', 495.37, 0, 15, 762.83, 'PREPARANDO', 'YAPE', 'Av. Principal 872, Lima, Perú', NULL, '2025-10-11 16:23:46', '2025-11-08 16:23:46', NULL);

-- =========================================================
-- TABLA productos (INCOMPLETA — SE CORTA AQUÍ)
-- =========================================================

DROP TABLE IF EXISTS `productos`;
CREATE TABLE `productos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `categoria` varchar(50) DEFAULT NULL,
  `color` varchar(50) DEFAULT NULL
  -- FALTAN MÁS COLUMNAS AQUÍ (tu dump se corta)
);
