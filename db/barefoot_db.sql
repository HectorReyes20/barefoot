-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 22-10-2025 a las 16:30:37
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
-- Base de datos: `barefoot_db`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `productos`
--

CREATE TABLE `productos` (
  `id` bigint(20) NOT NULL,
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
  `stock` int(11) NOT NULL,
  `talla` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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
(18, b'0', 'Casual', 'Negro', 'Este producto ya no está disponible.', b'0', '2025-10-10 12:33:07.000000', '2025-10-10 12:33:07.000000', 'https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=500', 'Cuero', 'Modelo Descontinuado', 299, NULL, 0, '40');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` bigint(20) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `rol` enum('USUARIO','ADMIN') NOT NULL DEFAULT 'USUARIO',
  `fecha_registro` datetime DEFAULT NULL,
  `activo` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `email`, `password`, `nombre`, `apellido`, `telefono`, `rol`, `fecha_registro`, `activo`) VALUES
(1, 'admin@barefoot.com', '$2a$10$xQn0EjgHWMJE4fLVGFGZ5.rXW7xCHt5TjE8CZMfKbOGW7KPXVVxvy', 'Administrador', 'Principal', '+51 999 888 777', 'ADMIN', '2025-10-09 18:05:20', 1),
(2, 'usuario@ejemplo.com', '$2a$10$8TQN6eQfz7t1K1NvHY.IqORmWl/8jXYGJhGkYYfJPv.KWz6f8mDc6', 'Juan', 'Pérez', '+51 987 654 321', 'USUARIO', '2025-10-09 18:05:20', 1),
(3, 'reyes@gmail.com', '$2a$10$Q5krmRVpKknP48.nEcOnf.YLldF5BY.ORaPI6pjHlzTGjXPJCAS2O', 'Hector', 'Reyes', '997256008', 'ADMIN', '2025-10-09 23:49:52', 1),
(4, 'fatama@gmail.com', '$2a$10$KXepm3/JeIOxkGKvZDemtepMZFKoxyxhSYV2UNyFzbPv8rpuRZN8m', 'Sebastian', 'Fatama', '987456123', 'USUARIO', '2025-10-10 14:01:45', 1),
(5, 'josthindiaz3@gmail.com', '$2a$10$MlbfNXfIaeTQq9WkUUWxseYcYNkIiwXMhkahpFXanCCnKx8t67PM.', 'Josthyn', 'Diaz', '922928818', 'USUARIO', '2025-10-17 00:09:16', 1),
(6, 'pepe@admin.com', '$2a$10$d37D72OVbdwKHmWlAWqNmu/A8Hbg.q6TC/qNysmAzV2X0CfC.YWUS', 'Josthynasd', 'asd', '922928818', 'USUARIO', '2025-10-17 00:20:32', 1),
(7, 'papu@gamer.com', '$2a$10$W6R/NQLt3o1.nENDMUam.eERet./9GRv1b3T24cZSwuDuoo2ptVOW', 'Tachito', 'xd', '987654321', 'USUARIO', '2025-10-17 00:24:03', 1);

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`id`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_rol` (`rol`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `productos`
--
ALTER TABLE `productos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
