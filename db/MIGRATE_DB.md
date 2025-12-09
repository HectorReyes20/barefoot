Instrucciones para aplicar migración - Incluir rol ENCARGADO

Resumen
------
Este documento contiene los pasos seguros para aplicar la migración que añade el valor 'ENCARGADO' al enum `usuarios.rol`.

Antes de ejecutar
-----------------
- Haz un backup de la tabla `usuarios` (o de toda la base de datos).
- Asegúrate de contar con credenciales con permisos ALTER TABLE.

Backup recomendado (PowerShell)
-------------------------------
Reemplaza usuario/host/db según tu entorno.

```powershell
# Exportar solo la tabla usuarios
mysqldump -u root -p barefoot_db usuarios > usuarios_backup.sql
# O exportar toda la base de datos
mysqldump -u root -p barefoot_db > barefoot_db_backup.sql
```

Ejecutar la migración
---------------------
Desde cliente MySQL (ejemplo usando mysql.exe):

```powershell
# Ejecutar el script de migración
mysql -u root -p barefoot_db < db\migrations\20251207_add_encargado_role.sql
```

o conectarte y ejecutar manualmente:

```sql
ALTER TABLE `usuarios`
  MODIFY COLUMN `rol` ENUM('USUARIO','ENCARGADO','ADMIN')
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci
  NOT NULL
  DEFAULT 'USUARIO';
```

Verificar
---------
Conéctate a la BD y ejecuta:

```sql
SHOW COLUMNS FROM usuarios LIKE 'rol';
SELECT DISTINCT rol FROM usuarios;
```

Si ves 'ENCARGADO' en la definición ENUM y no hay errores, la migración fue exitosa.

Probar la app
-------------
1. Reinicia la app.
2. Loguea como admin y prueba cambiar el rol de un usuario a 'ENCARGADO'.

Rollback (si algo sale mal)
---------------------------
- Restaura el backup creado anteriormente:

```powershell
mysql -u root -p barefoot_db < usuarios_backup.sql
```

Notas
-----
- En entornos productivos usa una herramienta de migraciones (Flyway/Liquibase) para registrar cambios.
- Si tu collation/charset es distinto, ajusta el MODIFY COLUMN con los valores correctos.

