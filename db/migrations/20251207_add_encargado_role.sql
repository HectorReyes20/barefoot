-- Migration: Add 'ENCARGADO' to usuarios.rol enum
-- Date: 2025-12-07
-- IMPORTANT: HAZ BACKUP ANTES DE EJECUTAR
-- Este script modifica la columna `rol` para incluir el valor 'ENCARGADO'.
-- Ejecutar en MySQL/MariaDB como usuario con privilegios ALTER TABLE.

ALTER TABLE `usuarios`
  MODIFY COLUMN `rol` ENUM('USUARIO','ENCARGADO','ADMIN')
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci
  NOT NULL
  DEFAULT 'USUARIO';

-- Nota: si tu tabla usa otra collation/charset, ajusta la línea CHARACTER SET / COLLATE
-- Este cambio hará que futuros updates con rol='ENCARGADO' funcionen correctamente.

