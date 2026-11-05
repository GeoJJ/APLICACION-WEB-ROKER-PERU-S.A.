# Backend y PostgreSQL

Requisitos: JDK 17 o posterior, PostgreSQL y una base llamada `roker_bd`.
Maven se ejecuta con el wrapper incluido; no es necesario instalarlo por separado.

## Preparar PostgreSQL con pgAdmin

Conéctate a tu servidor y comprueba si aparece `roker_bd` en Databases. Si no existe,
créala con ese nombre desde el menú Create > Database. También puedes consultar:

```sql
SELECT datname FROM pg_database WHERE datname = 'roker_bd';
```

Si no devuelve filas, ejecuta lo siguiente conectado a la base `postgres`, fuera de una transacción:

```sql
CREATE DATABASE roker_bd;
```

No ejecutes `DROP DATABASE` ni cambies `ddl-auto` a `create`: la configuración `update`
conserva los datos y crea/actualiza las tablas mapeadas, incluidas `rol` y `usuario`.
`InitialData` crea los roles faltantes y las cuentas iniciales después de preparar las tablas.
Si el correo ya existe, no lo duplica ni modifica su contraseña o rol. Los documentos
de las cuentas iniciales son identificadores sintéticos de ocho dígitos.

## Ejecutar en PowerShell

Desde la carpeta `backend`:

```powershell
$env:JAVA_HOME = 'C:\Users\USER\.jdks\ms-17.0.19'
$env:DB_PASSWORD = 'tu contraseña de PostgreSQL'
./mvnw.cmd spring-boot:run
```

La ruta de `JAVA_HOME` anterior corresponde al JDK 17 encontrado en este equipo;
si usas otro equipo, reemplázala por la ubicación de tu JDK. El backend escucha en
`http://localhost:8080`. No guardes la contraseña real en archivos del repositorio.

Variables opcionales:

| Variable | Valor predeterminado |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/roker_bd` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | vacío; debe configurarse si PostgreSQL la requiere |
| `CORS_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` |
| `SEED_ENABLED` | `true`; usa `false` para no crear cuentas iniciales |

## Cuentas iniciales

Contraseña inicial de todas: **Roker123**, almacenada con BCrypt.

| Rol | Correo | Panel |
|---|---|---|
| ADMINISTRADOR | administrador1@roker.com | `/admin` |
| ALMACENISTA | almacenista1@roker.com | `/warehouse` |
| VENDEDOR | vendedor1@roker.com | `/seller` |
| REPARTIDOR | repartidor1@roker.com | `/delivery` |
| PERSONAL | personal1@roker.com | `/staff` |
| CLIENTE | cliente1@roker.com | `/productos` |

Estas son cuentas de desarrollo. Para un despliegue real, configura `SEED_ENABLED=false`
y cambia las contraseñas de las cuentas ya creadas desde su perfil.

## Comunicación y permisos

El frontend usa `/api`; Vite lo reenvía al puerto 8080. El navegador nunca se conecta
directamente a PostgreSQL. La sesión se guarda en una cookie HttpOnly; las escrituras
requieren el token CSRF de `/api/auth/csrf`. CORS acepta únicamente los orígenes
configurados y permite credenciales. No mezcles `localhost` y `127.0.0.1` al usar la aplicación.

`/api/auth/login`, `/api/auth/me`, `/api/auth/profile` y `/api/auth/logout` manejan
la sesión y el perfil. El registro público crea exclusivamente clientes.
`/api/panel` devuelve los datos autorizados para el usuario actual; sus operaciones
comprueban roles en el servidor. Los cambios de rol o desactivaciones se aplican
en la siguiente petición. Solo el administrador cambia los roles.

Los controladores anteriores se conservaron adaptados a los DTO actuales porque
atienden funciones de diferentes roles. Productos, reportes y numeración de
comprobantes reutilizan la lógica existente. El reporte se expone también en
`/api/reportes/productos/excel` para el rol Administrador.

## Flujo de prueba manual

1. Administrador: registra proveedor, producto y servicio; revisa trabajadores y roles.
2. Almacenista: registra un ingreso para que el producto tenga existencias.
3. Cliente: completa teléfono y dirección en Editar perfil; también puedes registrar nuevos clientes.
4. Vendedor: crea un pedido para un cliente, asigna un repartidor y cambia a En camino.
5. Repartidor: adjunta fotografía y firma y confirma Entregado; consulta el historial.
6. Vendedor: crea una orden de servicio, selecciona el personal requerido y confirma.
7. Personal: consulta exclusivamente sus servicios asignados y confirma su finalización.
8. Administrador: consulta indicadores y descarga el reporte Excel de PostgreSQL.

El catálogo y los paneles comienzan vacíos si la base no tiene productos/pedidos.
Los datos de demostración guardados antes en el navegador no se importan al servidor.

## Pruebas automatizadas

```powershell
./mvnw.cmd test
```

Las pruebas usan H2 en memoria, modo PostgreSQL; no modifican `roker_bd`.
Cubren arranque, inicialización sin duplicados, login de seis roles, permisos,
inventario, pedidos, entregas, servicios y reportes. Debe verificarse por separado
la conexión con el PostgreSQL local usando las credenciales de ese servidor.

## Proveedores y servicios iniciales

El inicializador inserta dos proveedores y dos servicios cuando no existen. Los proveedores usan datos referenciales (RUC, teléfono y correo) que debes reemplazar por los datos reales antes de operar con ellos.

- Suministros Integrales Roker.
- Distribuciones de Higiene del Perú.
- Limpieza de Oficinas: S/ 150, 3 horas, una persona.
- Desinfección de Ambientes: S/ 180, 2 horas, una persona.

Administrador y Gerente se unifican en ADMINISTRADOR. La cuenta anterior gerente1@roker.com se retira; sus relaciones históricas se transfieren a administrador1@roker.com. La tabla rol conserva seis roles; las URL anteriores /manager redirigen al panel del administrador.

## Carrito y solicitudes de servicios

El catálogo permite agregar productos al carrito. Un aviso en la esquina inferior izquierda abre la sidebar al pulsar Ver carrito. Su acción Continuar con la compra dirige a /carrito. El carrito conserva cantidades en este navegador; el servidor verifica precio, disponibilidad y stock al confirmar. Se requiere una cuenta Cliente para registrar pedidos. El pago y la entrega se coordinan con ventas. Las direcciones y teléfonos de entrega se conservan en cada pedido.

Los servicios públicos no muestran tarifas. Solicitar abre /servicios/:id/solicitar con ambiente, área, ubicación, fecha/hora preferidas, frecuencia, necesidades y contacto. Las solicitudes se guardan en solicitudes_servicio. El cliente consulta /mis-solicitudes. Administrador y Vendedor cotizan desde Solicitudes de servicios en su panel. Al aceptar la propuesta, se crea una orden con el precio cotizado, sin contabilizar solicitudes pendientes como ventas.

API: POST /api/checkout; GET/POST /api/solicitudes; PUT /api/solicitudes/:id/cotizar, /aceptar y /cancelar. Los precios y permisos se validan en el backend y las acciones de aceptación y compra protegen contra repeticiones del mismo intento. La cuenta administradora única es administrador1@roker.com.
