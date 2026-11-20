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
