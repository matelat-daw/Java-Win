-- phpMyAdmin SQL Dump
-- version 5.2.3deb1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost:3306
-- Tiempo de generación: 31-05-2026 a las 08:45:58
-- Versión del servidor: 11.8.6-MariaDB-5 from Ubuntu
-- Versión de PHP: 8.5.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `techstore`
--
CREATE DATABASE IF NOT EXISTS `techstore` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `techstore`;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `categoria`
--

CREATE TABLE `categoria` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Volcado de datos para la tabla `categoria`
--

INSERT INTO `categoria` (`id`, `nombre`, `descripcion`) VALUES
(1, 'Periféricos', 'Accesorios y dispositivos periféricos para computadora'),
(2, 'Pantallas', 'Monitores y pantallas para computadora'),
(3, 'Audio', 'Dispositivos de audio y sonido'),
(4, 'Cables y Adaptadores', 'Cables, adaptadores y conectores diversos'),
(5, 'Componentes', 'Hardware');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `producto`
--

CREATE TABLE `producto` (
  `id` bigint(20) NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  `categoria` bigint(20) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `stock` int(11) NOT NULL,
  `activo` tinyint(4) NOT NULL,
  `imagen` varchar(255) DEFAULT NULL,
  `fecha_creacion` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Volcado de datos para la tabla `producto`
--

INSERT INTO `producto` (`id`, `nombre`, `precio`, `categoria`, `descripcion`, `stock`, `activo`, `imagen`, `fecha_creacion`) VALUES
(1, 'Monitor 27 pulgadas 144Hz', 349.00, 2, 'Monitor grande y fluido para trabajar y jugar.', 5, 1, 'monitor.webp', '2026-05-14 19:37:52'),
(2, 'Ratón Gaming 12000 DPI', 45.50, 1, 'Ratón Preciso con Varios Botones Configurables.', 2, 0, 'mouse.webp', '2026-05-14 19:37:52'),
(3, 'Auriculares con Micrófono', 69.99, 3, 'Auriculares Adecuados para Videollamadas y Juegos.', 2, 1, 'headset.webp', '2026-05-14 19:37:52'),
(4, 'Altavoces de 200 Watts', 20.20, 3, 'Parlantes Pioneer', 1, 1, 'speakers.webp', '2026-05-17 15:12:43'),
(5, 'Juego Teclado y Ratón Económicos', 15.50, 1, 'Teclado Extendido Español 108 Teclas, Ratón con 2 Botones y Rueda Central.', 0, 0, 'combo.webp', '2026-05-14 19:57:51'),
(6, 'NVME2 1 TeraByte Transcend', 450.55, 5, 'Unidad de Almacenamiento NVME2 de 1 TeraByte Transcend la Mejor del Mercado.', 5, 1, 'nvm2.webp', '2026-05-16 19:30:48');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `categoria`
--
ALTER TABLE `categoria`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nombre` (`nombre`);

--
-- Indices de la tabla `producto`
--
ALTER TABLE `producto`
  ADD PRIMARY KEY (`id`),
  ADD KEY `categoria` (`categoria`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `categoria`
--
ALTER TABLE `categoria`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `producto`
--
ALTER TABLE `producto`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `producto`
--
ALTER TABLE `producto`
  ADD CONSTRAINT `producto_ibfk_1` FOREIGN KEY (`categoria`) REFERENCES `categoria` (`id`) ON DELETE NO ACTION ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
