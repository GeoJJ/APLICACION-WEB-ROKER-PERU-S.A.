# ROKER PERU - Pruebas API (Bruno)

Coleccion hecha para el backend: 437 requests, con tests en cada una.

## Como correrla
1. Levanta el backend (`http://localhost:8080`) con la base de datos con el `data.sql` por defecto (rol ADMINISTRADOR + `admin@rokerperu.com` / `admin123`). No hace falta que existan los demas roles: la coleccion los crea.
2. En Bruno: abre la carpeta, elige el ambiente **Local** (si no lo eliges, `collection.bru` ya trae las mismas variables por defecto) y corre toda la coleccion con el Runner **en orden** (las carpetas van numeradas).
   - CLI: `bru run --env Local`

## Reglas del diseno
- **Se puede correr varias veces**: la carpeta `00` genera un sufijo unico (`run`) para correos, DNI, RUC y codigos.
- Los roles `VENDEDOR`, `REPARTIDOR`, `CLIENTE`, `GERENTE_GENERAL` y `ALMACENISTA` se crean si no existen (la carpeta 02 acepta 201 o 409).
- Los datos usan nombres de personas, empresas, productos y servicios realistas (sin tildes, para evitar problemas de codificacion en PDF/consola). Los correos llevan el sufijo `run` para no repetirse.
- Cada carpeta depende de las anteriores (ids y tokens se guardan como variables de la corrida). No corras una carpeta suelta sin haber corrido la `00` a la `04`.
- Los totales esperados dependen del flujo (por ejemplo, el stock de la mascarilla baja de 50 a 45 con los 5 pedidos).

## Orden
00 Preparacion · 01 Auth · 02 Roles · 03 Usuarios · 04 Login de todos los roles · 05 Proveedores · 06 Empleados · 07 Servicios · 08 Productos · 09 Inventario admin · 10 Almacenista · 11 Vendedor consultas · 12 Catalogo publico · 13 Pedidos del cliente · 14 Ordenes de servicio · 15 Vendedor y pedidos · 16 Repartidor · 17 Ordenes y asignaciones · 18 Comprobantes PDF · 19 Gerente · 20 Seguridad por rol · 21 Errores generales · 22 Pedidos y servicios con los datos nuevos · 23 Vendedor: ventas presenciales y edicion de pedidos · 24 Cliente: perfil y cierre de sesion

## Comprobantes (carpeta 18)
Prueba la matriz completa: quien puede descargar (200 + PDF), quien recibe 403 (comprobante ajeno o rol sin acceso), 409 (aun sin emitir), 404 (inexistente) y 400 (id invalido).


## Estados
- Pedido de productos: PENDIENTE -> EN_RUTA -> ENTREGADO (o CANCELADO). La venta presencial nace directamente en ENTREGADO.
- Orden de servicio: PENDIENTE -> CONFIRMADO -> COMPLETADO (o CANCELADO). Se completa llamando una segunda vez a `PUT /vendedor/ordenes-servicio/{id}/confirmar` (la primera confirma, la segunda marca el servicio como realizado).
