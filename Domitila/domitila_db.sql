-- phpMyAdmin SQL Dump
-- version 5.2.3deb1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost:3306
-- Tiempo de generación: 14-06-2026 a las 10:44:53
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
-- Base de datos: `domitila_db`
--
CREATE DATABASE IF NOT EXISTS `domitila_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_nopad_ai_ci;
USE `domitila_db`;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `incident`
--

CREATE TABLE `incident` (
  `id` bigint(20) NOT NULL,
  `project_id` bigint(20) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `severity` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_nopad_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `project`
--

CREATE TABLE `project` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `start_date` date NOT NULL,
  `end_date` date DEFAULT NULL,
  `status` varchar(50) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `project_manager_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_nopad_ai_ci;

--
-- Volcado de datos para la tabla `project`
--

INSERT INTO `project` (`id`, `name`, `description`, `start_date`, `end_date`, `status`, `type`, `code`, `project_manager_id`) VALUES
(1, 'Formación Laboral', 'Ayuda a domicilio para personas que necesitan inseretarse en el mercado laboral.', '2026-07-06', '2026-08-28', 'PAUSADO', 'Social', NULL, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `project_team`
--

CREATE TABLE `project_team` (
  `project_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_nopad_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `role`
--

CREATE TABLE `role` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci NOT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `role`
--

INSERT INTO `role` (`id`, `name`, `description`, `created_at`) VALUES
(1, 'USER', 'Usuario regular con funcionalidades básicas', '2026-03-30 17:25:15'),
(2, 'PREMIUM', 'Usuario premium con funcionalidades avanzadas', '2026-03-30 17:25:15'),
(3, 'ADMIN', 'Administrador del sistema con acceso total', '2026-03-30 17:25:15');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `task`
--

CREATE TABLE `task` (
  `id` bigint(20) NOT NULL,
  `project_id` bigint(20) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `due_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_nopad_ai_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user`
--

CREATE TABLE `user` (
  `id` bigint(20) NOT NULL,
  `nick` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_nopad_ai_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_nopad_ai_ci NOT NULL,
  `surname1` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_nopad_ai_ci NOT NULL,
  `surname2` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_nopad_ai_ci DEFAULT NULL,
  `bday` date DEFAULT NULL,
  `phone` varchar(255) NOT NULL,
  `gender` enum('MALE','FEMALE','OTHER') NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `profile_img` varchar(255) DEFAULT NULL,
  `active` tinyint(1) DEFAULT 0,
  `email_verified` tinyint(1) DEFAULT 0,
  `verification_token` varchar(255) DEFAULT NULL,
  `verification_token_expiry` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `user`
--

INSERT INTO `user` (`id`, `nick`, `name`, `surname1`, `surname2`, `bday`, `phone`, `gender`, `email`, `password`, `profile_img`, `active`, `email_verified`, `verification_token`, `verification_token_expiry`, `created_at`, `updated_at`) VALUES
(1, 'Admin', 'César', 'Matelat', NULL, '1968-04-05', '664774821', 'MALE', 'cesarmatelat@gmail.com', '$2a$12$XVPo2FFGNpyk3pgudQlwvuK30uflP0jvfSfNY3YBYCEkXQ7IauHgW', '1/profile.jpeg', 1, 1, NULL, NULL, '2026-04-01 10:11:02', '2026-06-12 08:59:24'),
(2, 'Orions68', 'César Osvaldo', 'Matelat', 'Borneo', '1968-04-05', '664774821', 'MALE', 'orions68@gmail.com', '$2a$12$8R3q5bcSE6k6w4g9/PhINu/taDY0x6v1JcoEiOvXTTYwwV.X2tPNG', '2/profile.jpg', 1, 1, NULL, NULL, '2026-04-01 14:22:20', '2026-04-01 14:22:37'),
(3, 'Testing_1', 'Primero', 'First', NULL, '2000-01-01', '664774821', 'MALE', 'matelat@outlook.es', '$2a$12$gsBMth7m2tdNTCstAVZE7uEO4fXROZ1eTZmAyqiQQMe.HbY7.EmFa', 'default/male.png', 1, 1, NULL, NULL, '2026-06-10 22:27:51', '2026-06-10 22:35:51'),
(4, 'Second', 'Segundo', 'Secondy', NULL, '2000-01-01', '664774821', 'MALE', 'pitagoras_3@hotmail.com', '$2a$12$7d/MasoTWvVMF7.qllfsm.eLP1Q8pGv1Vbmyua/mkaqCKpabhGe..', '4/profile.jpg', 1, 1, NULL, NULL, '2026-06-12 08:01:28', '2026-06-12 08:05:48');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `user_role`
--

CREATE TABLE `user_role` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  `assigned_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Volcado de datos para la tabla `user_role`
--

INSERT INTO `user_role` (`id`, `user_id`, `role_id`, `assigned_at`) VALUES
(1, 1, 3, '2026-04-01 11:11:02'),
(2, 2, 1, '2026-04-01 15:22:20'),
(11, 3, 1, '2026-06-10 23:27:51'),
(12, 4, 1, '2026-06-12 09:01:28');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `incident`
--
ALTER TABLE `incident`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_incident_project` (`project_id`);

--
-- Indices de la tabla `project`
--
ALTER TABLE `project`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKfgaomyji0g9jvvg7agt0r8t2q` (`project_manager_id`);

--
-- Indices de la tabla `project_team`
--
ALTER TABLE `project_team`
  ADD KEY `FK15j3f53hfe86nienfi4f1roxr` (`user_id`),
  ADD KEY `FK5fb0uivx2rshuy6386ppp6m8f` (`project_id`);

--
-- Indices de la tabla `role`
--
ALTER TABLE `role`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`),
  ADD KEY `idx_name` (`name`);

--
-- Indices de la tabla `task`
--
ALTER TABLE `task`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_tasks_project` (`project_id`);

--
-- Indices de la tabla `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `nick` (`nick`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_nick` (`nick`),
  ADD KEY `idx_verification_token` (`verification_token`);

--
-- Indices de la tabla `user_role`
--
ALTER TABLE `user_role`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_role` (`user_id`,`role_id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_role_id` (`role_id`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `incident`
--
ALTER TABLE `incident`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `project`
--
ALTER TABLE `project`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT de la tabla `role`
--
ALTER TABLE `role`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `task`
--
ALTER TABLE `task`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `user`
--
ALTER TABLE `user`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de la tabla `user_role`
--
ALTER TABLE `user_role`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `incident`
--
ALTER TABLE `incident`
  ADD CONSTRAINT `fk_incident_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `project`
--
ALTER TABLE `project`
  ADD CONSTRAINT `FKfgaomyji0g9jvvg7agt0r8t2q` FOREIGN KEY (`project_manager_id`) REFERENCES `user` (`id`);

--
-- Filtros para la tabla `project_team`
--
ALTER TABLE `project_team`
  ADD CONSTRAINT `FK15j3f53hfe86nienfi4f1roxr` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK5fb0uivx2rshuy6386ppp6m8f` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`);

--
-- Filtros para la tabla `task`
--
ALTER TABLE `task`
  ADD CONSTRAINT `fk_tasks_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `user_role`
--
ALTER TABLE `user_role`
  ADD CONSTRAINT `1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `user_role_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
