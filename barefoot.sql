-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: barefoot_db
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                            `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                            `nombre` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                            `apellido` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                            `telefono` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `rol` enum('USUARIO','ADMIN','ENCARGADO') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'USUARIO',
                            `fecha_registro` datetime DEFAULT NULL,
                            `activo` tinyint(1) DEFAULT '1',
                            `reset_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                            `token_expiration` datetime(6) DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'admin@barefoot.com','$2a$10$k.03wzL3b.2Y5bZ5c3j3X.6C3F.0A5g7A6g3E.3C2B1A4g5H7I9J','Administrador','Principal','+51 999 888 777','ADMIN','2025-10-09 18:05:20',1,NULL,NULL),(2,'usuario@ejemplo.com','$2a$10$8TQN6eQfz7t1K1NvHY.IqORmWl/8jXYGJhGkYYfJPv.KWz6f8mDc6','Juan','Pérez','+51 987 654 321','USUARIO','2025-10-09 18:05:20',1,NULL,NULL),(3,'reyes@gmail.com','$2a$10$2Fa9l1KEzMh4qh1ejILLTeADT6BPVHzPOOb99hj3VU8CWui8rqa1e','Hector','Reyes','997256008','ADMIN','2025-10-09 23:49:52',1,NULL,NULL),(4,'fatama@gmail.com','$2a$10$KXepm3/JeIOxkGKvZDemtepMZFKoxyxhSYV2UNyFzbPv8rpuRZN8m','Sebastian','Fatama','987456123','USUARIO','2025-10-10 14:01:45',1,NULL,NULL),(5,'meza@gmail.com','$2a$10$JfQUilop7d1g06b5k5jZKuc4l4SfRF9IbQxsY8Aclgh7UVpw15Fxm','Luis','Meza','987412356','USUARIO','2025-10-22 21:00:18',1,NULL,NULL),(6,'joaumoises@gmail.com','$2a$10$2LaKKUoSLVBalybsx/XtVu1arqMfZCzN4hu4hC51Hg6Svq.JwbqAO','Joao','Inga','902644490','USUARIO','2025-12-06 23:21:37',1,NULL,NULL),(7,'jaime@gmail.com','$2a$10$./fuvFLG/.pU1K/BJcVsceSxmc.OZhl6vm74oUT9UgvDFlVFTvfqK','Pablo','Jaime','963258741','USUARIO','2025-12-07 00:40:58',1,NULL,NULL),(8,'perez@gmail.com','$2a$10$YhCPY3QICMD0pR0OxBVJQ.0nZ5uHE9r/kA7KnKPA5DtClFIX7G6/i','juan','perez','980234567','USUARIO','2025-12-07 01:38:39',1,NULL,NULL),(9,'edu@gmail.com','$2a$10$xSh7/eWkZoU2XTrKgjwPW.MqrvF7GqCl2yEnyfpiJS8zI3rvMC99e','Eduardo','Inga','985632147','USUARIO','2025-12-07 03:43:12',1,NULL,NULL),(10,'juan@gmail.com','$2a$10$/xnF09L/EtboH5OfW3gCmuCuZP4QdV6HW.UykPC28svcPM4nPkjzi','Juan','assadsa',NULL,'USUARIO','2025-12-07 06:15:00',1,NULL,NULL),(11,'paloma@gmail.com','$2a$10$8mAZqZvGZKq84mOVwNRh4uAK/CcLlLYUWAccYnXCdmAiCZPB0BuWa','Palomita','Inga','985632147','USUARIO','2025-12-07 06:18:45',1,NULL,NULL);
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
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
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES (1,_binary '','Casual','Negro','Calzado barefoot casual perfecto para el día a día. Diseño elegante y minimalista que se adapta a cualquier ocasión.',_binary '','2025-12-07 05:46:14.249423','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=500','Cuero','Urban Barefoot Negro',299,NULL,13,'40'),(2,_binary '','Casual','Marrón','Zapato barefoot casual con suela flexible. Ideal para largas caminatas urbanas con máximo confort.',_binary '','2025-12-07 03:51:03.005972','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1520256862855-398228c41684?w=500','Cuero','City Walker Marrón',279,249,29,'41'),(3,_binary '','Casual','Beige','Tu compañero diario. Ligero, transpirable y con diseño versátil para combinar con todo tu guardarropa.',_binary '\0','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1514989940723-e8e51635b782?w=500','Lona','Everyday Comfort Beige',259,NULL,40,'39'),(4,_binary '','Deportivo','Negro','Calzado deportivo barefoot con máxima flexibilidad. Perfecto para entrenamientos funcionales y gimnasio.',_binary '','2025-12-06 15:08:56.926648','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500','Malla','Sport Flex Pro',349,299,34,'42'),(5,_binary '','Deportivo','Azul','Diseñado para movimiento natural en tus entrenamientos. Suela de contacto total con el suelo.',_binary '\0','2025-12-07 03:52:44.406731','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=500','Sintético','Active Motion Azul',329,NULL,27,'41'),(6,_binary '','Deportivo','Gris','Lo esencial para tus sesiones de gym. Ligero, resistente y con excelente agarre.',_binary '\0','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1600185365926-3a2ce3cdb9eb?w=500','Malla','Gym Essential Gris',289,NULL,32,'40'),(7,_binary '','Formal','Negro','Elegancia profesional sin sacrificar la salud de tus pies. Para la oficina o eventos formales.',_binary '','2025-12-06 15:02:56.638809','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500','Cuero','Classic Barefoot Negro',399,NULL,19,'42'),(8,_binary '','Formal','Marrón','Estilo ejecutivo con filosofía barefoot. Comodidad durante toda la jornada laboral.',_binary '\0','2025-12-07 03:52:44.406731','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1614252369475-531eba835eb1?w=500','Cuero','Business Elite Marrón',389,349,17,'41'),(9,_binary '','Formal','Negro','Diseño sobrio y profesional. Ideal para presentaciones y reuniones importantes.',_binary '\0','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=500','Gamuza','Professional Touch Negro',379,NULL,15,'40'),(10,_binary '','Running','Rojo','Libertad total para tus pies mientras corres. Suela minimalista con máximo feedback.',_binary '','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1554068865-24cecd4e34b8?w=500','Malla','Runner Freedom Rojo',369,329,45,'42'),(11,_binary '','Running','Verde','Para corredores que buscan conexión natural con el terreno. Ultra ligero y flexible.',_binary '\0','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1552346154-21d32810aba3?w=500','Sintético','Trail Master Verde',379,NULL,38,'41'),(12,_binary '','Running','Blanco','Minimalismo y velocidad. Diseñado para corredores de medio fondo y largas distancias.',_binary '\0','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1608231387042-66d1773070a5?w=500','Malla','Speed Natural Blanco',359,NULL,42,'40'),(13,_binary '','Senderismo','Marrón','Para aventureros que aman sentir el terreno. Resistente pero flexible para cualquier camino.',_binary '','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1551107696-a4b0c5a0d9a2?w=500','Cuero','Mountain Trek Pro',419,379,22,'43'),(14,_binary '','Senderismo','Verde','Explora nuevos senderos con máxima libertad. Suela antideslizante y protección reforzada.',_binary '\0','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1520256788229-d4640c632e4b?w=500','Sintético','Trail Explorer Verde',399,NULL,26,'42'),(15,_binary '','Senderismo','Gris','Tu compañero perfecto para rutas de montaña. Transpirable y resistente al agua.',_binary '\0','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1605348532760-6753d2c43329?w=500','Sintético','Adventure Seeker Gris',389,349,20,'41'),(16,_binary '','Casual','Beige','Edición limitada con detalles dorados. Solo quedan pocas unidades.',_binary '','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=500','Cuero','Limited Edition Oro',459,399,5,'40'),(17,_binary '','Casual','Marrón','Estilo retro con tecnología moderna. Stock limitado por alta demanda.',_binary '\0','2025-12-07 05:46:14.249423','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1603808033192-082d6919d3e1?w=500','Gamuza','Vintage Collection Marrón',329,NULL,7,'41'),(18,_binary '\0','Casual','Negro','Este producto ya no está disponible.',_binary '\0','2025-10-10 12:33:07.000000','2025-10-10 12:33:07.000000','https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=500','Cuero','Modelo Descontinuado',299,NULL,0,'40'),(19,_binary '','Deportivo','Negro','Zapatillas Saguaro Vigor II: Todos los modelos de saguaro que encontraras en nuestra pagina, son productos veganos, respetuosos con el medio ambiente y los animales, de producción sostenible. Técnicamente ofrecen las 3 características bases de zapatos minimalistas/ergonómicos para tus pies.',_binary '','2025-12-07 03:51:03.004950','2025-11-08 21:01:12.266744','https://saguarobarefoot.pe/cdn/shop/files/0a81fd_8a6f40383ece40e9b0fffca46c672b23_mv2.webp?v=1718385844&width=533','Sintético','Saguaro - Zapatillas Vigor II - Sport Barefoot - Negro',199,179,0,'36'),(20,_binary '','Senderismo','Verde','Proporciona amortiguación y retorno de energía, adecuada para terrenos técnicos. ',_binary '','2025-12-07 13:29:36.655623','2025-12-07 13:11:20.241731','https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTvIefzcGugKxUr8ooEFYtgIw63SRnqLKm8-w&s','Gamuza','Altra Lone Peak 9 Waterproof Low',450,350,8,'45'),(21,_binary '','Casual','Rojo','Nuestra zapatilla Made US 992 para hombre tiene un estilo tradicional, materiales de primera calidad y características de comodidad para elevar tu look informal. Estas zapatillas de moda para hombre tienen una parte superior de piel de cerdo y algodón Supima® suave sobre la amortiguación del talón SBS ABZORB y una suela de goma para mayor durabilidad y un ajuste realmente cómodo. ',_binary '','2025-12-07 13:29:36.655623','2025-12-07 13:20:50.817024','https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcSfkOlvx5-AuayW8FOSMn4uToLw5OPGQZgIv21id0fsDgGLIxO-','Gamuza','Zapatillas New Balance 990 Unisex',1000,900,9,'44'),(22,_binary '','Deportivo','Azul','La zapatilla UPCOURT™ 5 es una opción de pista ligera que está diseñada para ofrecer una mayor flexibilidad y un ajuste más cómodo. Cuenta con una sección más amplia de paneles de malla que ayuda a crear un ajuste más suave y adaptable. Además, sus revestimientos de sujeción en el mediopié ofrecen una mejor estabilidad durante los movimientos multidireccionales. Por último, la puntera y la talonera están reforzadas con paneles duraderos que ayudan a que tus zapatillas duren más.',_binary '\0','2025-12-07 13:26:14.215637','2025-12-07 13:26:14.215637','https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ6vXbVtN-P6WKr75iTQVqcPcZZNETwbS5-eg&s','Sintético','Asics Upcourt 5',349.9,NULL,20,'42');
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `numero_pedido` varchar(50) NOT NULL,
  `subtotal` double NOT NULL,
  `descuento` double DEFAULT NULL,
  `costo_envio` double NOT NULL,
  `total` double NOT NULL,
  `estado` enum('CONFIRMADO','PREPARANDO','EN_CAMINO','ENTREGADO','CANCELADO') NOT NULL,
  `metodo_pago` enum('TARJETA_CREDITO','TARJETA_DEBITO','YAPE','PLIN','TRANSFERENCIA','PAYPAL') NOT NULL,
  `direccion_envio` varchar(255) NOT NULL,
  `notas` varchar(500) DEFAULT NULL,
  `fecha_pedido` datetime DEFAULT NULL,
  `fecha_actualizacion` datetime DEFAULT NULL,
  `fecha_entrega` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero_pedido` (`numero_pedido`),
  KEY `idx_usuario` (`usuario_id`),
  CONSTRAINT `FK_pedidos_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidos`
--

LOCK TABLES `pedidos` WRITE;
/*!40000 ALTER TABLE `pedidos` DISABLE KEYS */;
INSERT INTO `pedidos` VALUES (1,2,'PED-0000000001',453.16,0,15,433.03,'CONFIRMADO','TARJETA_CREDITO','Av. Principal 734, Lima, Perú',NULL,'2025-10-11 16:23:46','2025-11-08 16:23:46',NULL),(2,4,'PED-0000000002',810.47,0,0,1190.27,'CONFIRMADO','TARJETA_CREDITO','Av. Principal 229, Lima, Perú',NULL,'2025-10-22 16:23:46','2025-11-08 16:23:46',NULL),(3,5,'PED-0000000003',495.37,0,15,762.83,'PREPARANDO','YAPE','Av. Principal 872, Lima, Perú',NULL,'2025-10-11 16:23:46','2025-11-08 16:23:46',NULL),(4,4,'PED-1764983332041',478,0,0,478,'CONFIRMADO','TARJETA_CREDITO','asdasdasd','sadasdas','2025-12-06 01:08:52','2025-12-06 01:24:42','2025-12-06 01:24:36'),(5,4,'PED-1764983806323',179,0,15,194,'CONFIRMADO','TARJETA_CREDITO','dsadasd','sadasd','2025-12-06 01:16:46','2025-12-06 01:16:46',NULL),(6,4,'PED-1764993235808',598,0,0,598,'CONFIRMADO','TARJETA_CREDITO','sdasd','xasd','2025-12-06 03:53:56','2025-12-06 03:53:56',NULL),(7,4,'PED-1765029725874',299,0,15,314,'CONFIRMADO','TARJETA_CREDITO','ddasdasdas','sadadasd','2025-12-06 14:02:06','2025-12-06 14:02:06',NULL),(8,4,'PED-1765033376617',698,0,0,698,'CONFIRMADO','TARJETA_CREDITO','wrfwew','fddfs','2025-12-06 15:02:57','2025-12-06 15:02:57',NULL),(9,4,'PED-1765033736915',299,0,15,314,'CONFIRMADO','TARJETA_CREDITO','dasdasdas','qasd','2025-12-06 15:08:57','2025-12-06 15:08:57',NULL),(10,4,'PED-1765034899018',598,0,0,598,'CONFIRMADO','TARJETA_CREDITO','ewawdeasw','qwddasdas','2025-12-06 15:28:19','2025-12-06 15:28:19',NULL),(11,4,'PED-1765059113149',299,0,15,314,'CONFIRMADO','YAPE','saddsads','asdasd','2025-12-06 22:11:53','2025-12-06 22:11:53',NULL),(12,4,'PED-1765060268792',598,0,0,598,'CONFIRMADO','YAPE','sadasd','dada','2025-12-06 22:31:09','2025-12-06 22:31:09',NULL),(13,6,'PED-1765077307871',179,0,15,194,'CONFIRMADO','YAPE','dfsfds','fdds','2025-12-07 03:15:08','2025-12-07 03:15:08',NULL),(14,6,'PED-1765079462988',428,0,0,428,'CONFIRMADO','YAPE','asdasdsa','asdasd','2025-12-07 03:51:03','2025-12-07 03:51:03',NULL),(15,6,'PED-1765079564395',977,0,0,977,'CONFIRMADO','YAPE','xfsd','gdfd','2025-12-07 03:52:44','2025-12-07 03:52:44',NULL),(16,6,'PED-1765086374232',628,0,0,628,'CONFIRMADO','YAPE','dasdas','adsdsa','2025-12-07 05:46:14','2025-12-07 05:46:14',NULL),(17,6,'PED-1765114176642',1600,0,0,1600,'CONFIRMADO','YAPE','asdasd','dasdasda','2025-12-07 13:29:37','2025-12-07 13:29:37',NULL);
/*!40000 ALTER TABLE `pedidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalle_pedidos`
--

DROP TABLE IF EXISTS `detalle_pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
                                   KEY `idx_producto` (`producto_id`),
                                   CONSTRAINT `FK_detalle_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`),
                                   CONSTRAINT `FK_detalle_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalle_pedidos`
--

LOCK TABLES `detalle_pedidos` WRITE;
/*!40000 ALTER TABLE `detalle_pedidos` DISABLE KEYS */;
INSERT INTO `detalle_pedidos` VALUES (1,3,1,2,299,299,NULL),(2,2,2,2,279,558,NULL),(3,3,3,2,259,518,NULL),(4,2,6,1,289,578,NULL),(5,1,6,2,289,578,NULL),(6,1,7,2,399,798,NULL),(7,2,9,1,379,379,NULL),(8,1,9,2,379,379,NULL),(9,3,10,1,369,369,NULL),(10,1,13,2,419,419,NULL),(11,1,14,2,399,399,NULL),(12,3,15,2,389,778,NULL),(13,2,17,1,329,329,NULL),(14,4,1,1,299,299,NULL),(15,4,19,1,179,179,NULL),(16,5,19,1,179,179,NULL),(17,6,1,2,299,598,'Modelo: Urban Custom, Talla: 36, Material: Cuero, Diseño: Cuello: Rojo, Contrafuerte: Amarillo, Color Principal: Gris, Estampado: Azul, Cordones: Negro'),(18,7,1,1,299,299,'Modelo: Urban Custom, Talla: 36, Material: Cuero, Diseño: '),(19,8,1,1,299,299,'Modelo: Urban Custom, Talla: 36, Material: Cuero, Diseño: Estándar'),(20,8,7,1,399,399,'Modelo: Classic Custom, Talla: 36, Material: Cuero, Diseño: Estándar'),(21,9,4,1,299,299,'Modelo: Sport Custom, Talla: 36, Material: Cuero, Diseño: Suela Principal: Amarillo, Cuerpo Principal: Rojo, Diseño 2: Marrón, Logo N (Blanco): Azul'),(22,10,1,1,299,299,'Modelo: Urban Custom, Talla: 36, Material: Cuero, Diseño: Color Principal: Blanco, Detalles Secundarios: Blanco, Suela: Blanco, Contrafuerte: Blanco, Cuello: Blanco, Cordones: Blanco, Estampado: Blanco'),(23,10,1,1,299,299,NULL),(24,11,1,1,299,299,'Modelo: Urban Custom, Talla: 36, Material: Cuero, Diseño: Color Principal: Rojo, Detalles Secundarios: Rojo'),(25,12,1,1,299,299,'Modelo: Urban Custom, Talla: 36, Material: Cuero, Diseño: Color Principal: Azul, Detalles Secundarios: Azul, Suela: Azul, Contrafuerte: Rojo, Estampado: Verde'),(26,12,1,1,299,299,'Modelo: Urban Custom, Talla: 36, Material: Cuero, Diseño: Estándar'),(27,13,19,1,179,179,NULL),(28,14,19,1,179,179,NULL),(29,14,2,1,249,249,NULL),(30,15,1,1,299,299,'Modelo: Urban Custom, Talla: 36, Material: Cuero, Diseño: Estándar'),(31,15,5,1,329,329,NULL),(32,15,8,1,349,349,NULL),(33,16,17,1,329,329,NULL),(34,16,1,1,299,299,'Modelo: Urban Custom, Talla: 36, Material: Cuero, Diseño: Color Principal: Azul, Detalles Secundarios: Marrón, Suela: Amarillo, Contrafuerte: Amarillo, Cuello: Amarillo, Cordones: Amarillo, Estampado: Rojo'),(35,17,20,1,350,350,'Modelo: Urban Custom, Talla: 36, Material: Cuero, Diseño: Estándar'),(36,17,21,1,900,900,'Modelo: Sport Custom, Talla: 36, Material: Cuero, Diseño: Suela Secundaria: Rojo, Suela Baja: Amarillo'),(37,17,20,1,350,350,NULL);
/*!40000 ALTER TABLE `detalle_pedidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientos_inventario`
--

DROP TABLE IF EXISTS `movimientos_inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
                                          CONSTRAINT `FK_movimiento_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
                                          CONSTRAINT `FK_movimiento_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos_inventario`
--

LOCK TABLES `movimientos_inventario` WRITE;
/*!40000 ALTER TABLE `movimientos_inventario` DISABLE KEYS */;
INSERT INTO `movimientos_inventario` VALUES (1,1,'ENTRADA',50,0,50,'Stock inicial - Compra inicial de inventario',NULL,1,NULL,'2025-09-09 21:23:00'),(2,2,'ENTRADA',50,0,50,'Stock inicial - Compra inicial de inventario',NULL,1,NULL,'2025-09-09 21:23:00'),(3,3,'ENTRADA',50,0,50,'Stock inicial - Compra inicial de inventario',NULL,1,NULL,'2025-09-09 21:23:00'),(4,4,'ENTRADA',50,0,50,'Stock inicial - Compra inicial de inventario',NULL,1,NULL,'2025-09-09 21:23:00'),(5,5,'ENTRADA',50,0,50,'Stock inicial - Compra inicial de inventario',NULL,1,NULL,'2025-09-09 21:23:00'),(6,6,'ENTRADA',50,0,50,'Stock inicial - Compra inicial de inventario',NULL,1,NULL,'2025-09-09 21:23:00'),(7,7,'ENTRADA',50,0,50,'Stock inicial - Compra inicial de inventario',NULL,1,NULL,'2025-09-09 21:23:00'),(8,8,'ENTRADA',50,0,50,'Stock inicial - Compra inicial de inventario',NULL,1,NULL,'2025-09-09 21:23:00'),(9,9,'ENTRADA',50,0,50,'Stock inicial - Compra inicial de inventario',NULL,1,NULL,'2025-09-09 21:23:00'),(10,10,'ENTRADA',50,0,50,'Stock inicial - Compra inicial de inventario',NULL,1,NULL,'2025-09-09 21:23:00'),(11,1,'SALIDA',26,50,25,'Venta del 01/11/2025',NULL,1,NULL,'2025-10-30 21:23:00'),(12,2,'SALIDA',26,50,26,'Venta del 06/11/2025',NULL,1,NULL,'2025-10-25 21:23:00'),(13,3,'SALIDA',8,50,39,'Venta del 18/10/2025',NULL,1,NULL,'2025-10-10 21:23:00'),(14,4,'SALIDA',27,50,23,'Venta del 24/10/2025',NULL,1,NULL,'2025-10-27 21:23:00'),(15,5,'SALIDA',17,50,19,'Venta del 04/11/2025',NULL,1,NULL,'2025-11-06 21:23:00'),(16,6,'SALIDA',33,50,30,'Venta del 16/10/2025',NULL,1,NULL,'2025-11-01 21:23:00'),(17,7,'SALIDA',5,50,35,'Venta del 19/10/2025',NULL,1,NULL,'2025-10-31 21:23:00'),(18,8,'SALIDA',18,50,33,'Venta del 18/10/2025',NULL,1,NULL,'2025-10-31 21:23:00'),(19,9,'SALIDA',12,50,31,'Venta del 20/10/2025',NULL,1,NULL,'2025-10-16 21:23:00'),(20,10,'SALIDA',33,50,33,'Venta del 31/10/2025',NULL,1,NULL,'2025-11-03 21:23:00'),(21,11,'SALIDA',5,50,29,'Venta del 20/10/2025',NULL,1,NULL,'2025-10-19 21:23:00'),(22,12,'SALIDA',17,50,44,'Venta del 07/11/2025',NULL,1,NULL,'2025-11-07 21:23:00'),(23,13,'SALIDA',7,50,37,'Venta del 04/11/2025',NULL,1,NULL,'2025-10-11 21:23:00'),(24,14,'SALIDA',11,50,35,'Venta del 10/10/2025',NULL,1,NULL,'2025-10-12 21:23:00'),(25,15,'SALIDA',22,50,39,'Venta del 29/10/2025',NULL,1,NULL,'2025-11-06 21:23:00'),(26,16,'SALIDA',17,50,23,'Venta del 22/10/2025',NULL,1,NULL,'2025-10-20 21:23:00'),(27,17,'SALIDA',19,50,33,'Venta del 21/10/2025',NULL,1,NULL,'2025-10-13 21:23:00'),(28,19,'SALIDA',23,50,32,'Venta del 30/10/2025',NULL,1,NULL,'2025-10-31 21:23:00'),(29,8,'AJUSTE_ENTRADA',5,18,23,'Ajuste por inventario físico - Stock encontrado no registrado',NULL,1,NULL,'2025-11-01 21:23:01'),(30,9,'AJUSTE_ENTRADA',5,15,20,'Ajuste por inventario físico - Stock encontrado no registrado',NULL,1,NULL,'2025-11-01 21:23:01'),(31,16,'AJUSTE_ENTRADA',5,5,10,'Ajuste por inventario físico - Stock encontrado no registrado',NULL,1,NULL,'2025-11-01 21:23:01'),(32,17,'AJUSTE_ENTRADA',5,8,13,'Ajuste por inventario físico - Stock encontrado no registrado',NULL,1,NULL,'2025-11-01 21:23:01'),(33,19,'AJUSTE_ENTRADA',5,4,9,'Ajuste por inventario físico - Stock encontrado no registrado',NULL,1,NULL,'2025-11-01 21:23:01');
/*!40000 ALTER TABLE `movimientos_inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transaccion`
--

DROP TABLE IF EXISTS `transaccion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaccion` (
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `estado` enum('COMPLETADO','FALLIDO','PENDIENTE','PROCESANDO','REEMBOLSADO') NOT NULL,
                               `fecha_confirmacion` datetime(6) DEFAULT NULL,
                               `fecha_creacion` datetime(6) NOT NULL,
                               `message_error` varchar(500) DEFAULT NULL,
                               `monto` double NOT NULL,
                               `pasarela` enum('CONTRAENTREGA','IZIPAY','PAGOSEGURO','PAYPAL','PLIN','STRIPE','TRANSFERENCIA','YAPE') NOT NULL,
                               `referencia_externa` varchar(255) DEFAULT NULL,
                               `token_pago` varchar(500) DEFAULT NULL,
                               `pedido_id` bigint NOT NULL,
                               PRIMARY KEY (`id`),
                               CONSTRAINT `FK_transaccion_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedidos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaccion`
--

LOCK TABLES `transaccion` WRITE;
/*!40000 ALTER TABLE `transaccion` DISABLE KEYS */;
INSERT INTO `transaccion` VALUES (1,'PENDIENTE',NULL,'2025-12-06 01:15:15.410851',NULL,478,'STRIPE',NULL,NULL,4),(2,'PENDIENTE',NULL,'2025-12-06 01:15:37.181205',NULL,478,'STRIPE',NULL,NULL,4),(3,'PENDIENTE',NULL,'2025-12-06 01:15:47.008813',NULL,478,'STRIPE',NULL,NULL,4),(4,'PENDIENTE',NULL,'2025-12-06 03:53:59.363232',NULL,598,'STRIPE',NULL,NULL,6),(5,'PENDIENTE',NULL,'2025-12-06 03:54:15.623061',NULL,598,'STRIPE',NULL,NULL,6),(6,'PENDIENTE',NULL,'2025-12-06 03:54:30.248068',NULL,598,'STRIPE',NULL,NULL,6),(7,'PENDIENTE',NULL,'2025-12-06 03:54:30.530251',NULL,598,'STRIPE',NULL,NULL,6),(8,'PENDIENTE',NULL,'2025-12-06 03:54:36.627564',NULL,598,'STRIPE',NULL,NULL,6),(9,'PENDIENTE',NULL,'2025-12-06 22:31:12.614953',NULL,598,'YAPE','csczfsfas',NULL,12),(10,'PENDIENTE',NULL,'2025-12-06 22:31:58.026995',NULL,598,'YAPE','dasddasdas',NULL,12),(11,'PENDIENTE',NULL,'2025-12-07 03:15:13.660378',NULL,194,'YAPE','dfdfdff',NULL,13),(12,'PENDIENTE',NULL,'2025-12-07 03:51:06.511926',NULL,428,'YAPE','csczfsfas',NULL,14),(13,'PENDIENTE',NULL,'2025-12-07 03:52:47.957783',NULL,977,'YAPE','dasdas',NULL,15),(14,'PENDIENTE',NULL,'2025-12-07 05:46:16.807627',NULL,628,'YAPE','dsadas',NULL,16);
/*!40000 ALTER TABLE `transaccion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structures
--

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-07  8:48:59
