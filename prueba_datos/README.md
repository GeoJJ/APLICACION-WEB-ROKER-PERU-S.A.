# Datos de Andrea para la aplicación local

## Procedencia

`RokerPeru Datos de Prueba.json` conserva la colección original de Andrea, publicada en `develop/prueba-jasper-report`, commit `c9c7797`, el 22 de noviembre de 2026. Contiene solicitudes para crear usuarios, proveedores, productos, servicios, empleados, movimientos, ventas, pedidos y asignaciones. No es un respaldo de PostgreSQL ni un script SQL.

`RokerPeru Datos Tomcat.postman_collection.json` adapta la misma colección al despliegue en `http://localhost:8080/api`. El login del administrador usa variables vacías que debe configurar quien la ejecuta. No contiene las credenciales particulares del equipo de integración.

Se retiran las comprobaciones que exigían que los códigos empezaran exactamente en PRD-0001 y que todos los trabajadores existentes tuvieran un dominio de correo determinado: una base ya poblada puede contener otros códigos y cuentas. Se conserva la verificación del formato de los códigos nuevos y de la presencia de los productos creados por esta colección.

## Carga con reanudación

1. Respaldar la base PostgreSQL antes de cargar datos.
2. Iniciar Roker en Tomcat y confirmar que su API está disponible.
3. Desde la raíz del repositorio, ejecutar:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/cargar-datos-andrea.ps1
```

Introducir el correo y la contraseña del administrador local. También se pueden definir `ROKER_ADMIN_EMAIL`, `ROKER_ADMIN_PASSWORD` y `ROKER_API_URL` en el entorno.

El cargador utiliza Node.js y sus bibliotecas estándar; no necesita paquetes adicionales. Ejecuta las peticiones en el orden de la colección, resuelve sus variables y verifica las expectativas utilizadas por sus scripts. No implementa un cliente Postman general ni ejecuta una nueva batería de seguridad.

Antes de crear datos maestros consulta la API y reutiliza coincidencias por correo, RUC o nombre, según el tipo de registro. No modifica los registros encontrados ni borra los anteriores. Productos con distinto nombre o presentación se conservan por separado.

Los resultados de cada operación se guardan en `.local/carga-andrea/estado.json`, excluido de Git. Al repetir la carga sobre la misma base, el cargador no repite las ventas, ingresos, pedidos, asignaciones ni cambios de estado completados. Mantener ese archivo para poder continuar; no reutilizarlo después de restaurar, reemplazar o cambiar la base del servidor.

Si una petición de escritura queda sin respuesta o recibe un error interno, la operación queda marcada como pendiente. El cargador se detiene y no la reenvía automáticamente: primero hay que comprobar si se guardó en PostgreSQL. El archivo local puede contener las respuestas de los registros cargados, pero no guarda los tokens ni las contraseñas de acceso.

El archivo original puede importarse en Postman, al igual que la versión adaptada. Ejecutar la colección directamente en Collection Runner no incorpora el control de reanudación del cargador: repetirla puede duplicar movimientos y ventas.

## Cuentas incluidas

Las cuentas nuevas de trabajadores y clientes mantienen la contraseña inicial de la colección de Andrea, `Clave12345`. Por ejemplo, la cuenta gerencial es `patricia.rivera@rokerperu.com`. Las cuentas locales que ya existían conservan sus contraseñas y sus roles.

Los empleados de servicio de esta colección son registros operativos de personal; no equivalen a cuentas con login.

## Compatibilidad con bases anteriores

La venta presencial utiliza `id_cliente_presencial`, mientras que la venta en línea utiliza `id_cliente`. `backend/src/main/resources/schema.sql` retira el antiguo `NOT NULL` de `pedidos.id_cliente`, de acuerdo con la entidad actual de Andrea. No elimina filas ni tablas; permite que una venta presencial no necesite inventar un cliente registrado.

## Resultado local del 22 de noviembre de 2026

La carga preservó los registros existentes y dejó 19 productos, 10 servicios, 21 usuarios, 8 proveedores, 9 empleados de servicio, 8 movimientos de inventario, 8 pedidos (4 presenciales y 4 en línea), 5 órdenes de servicio y 6 asignaciones. Se repitió el cargador y se verificó que esos conteos permanecieran iguales.

El respaldo, el estado del cargador y las credenciales permanecen en el equipo local. El repositorio incluye la colección, los scripts y un resumen de verificación sin tokens ni credenciales particulares.
