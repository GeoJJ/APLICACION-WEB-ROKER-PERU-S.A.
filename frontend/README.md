# Frontend: nuevas vistas para backend-cambios

Este cambio traslada únicamente el frontend desde implementacion-nuevas-vistas. No incluye cambios de Java, entidades, controladores, Hibernate, usuarios iniciales ni configuración del backend.

## Ejecutar

Desde frontend:

    npm ci
    npm run dev

Abre http://localhost:5173. Vite reenvía /api al backend en http://localhost:8080 y elimina el prefijo /api. Para otro servidor, configura API_PROXY_TARGET antes de iniciar Vite. VITE_API_URL puede contener la URL directa de la API sin el prefijo /api; en ese caso el servidor debe admitir CORS.

## Roles y vistas

El panel principal es /manager y muestra GERENTE_GENERAL (Gerente general). Las direcciones antiguas /admin redirigen a ese panel. Se conservan los paneles /warehouse, /seller, /delivery y /staff.

El backend actual no tiene endpoints de login ni sesión. Los paneles se muestran como vistas previas explícitas; no se inventan usuarios, contraseñas, cookies ni permisos. La etiqueta del rol describe el panel y no una sesión autenticada. El login, registro, perfil y operaciones de edición están deshabilitados hasta que se integre la autenticación real.

GERENTE_GENERAL dispone de reportes e inventario. La API mantiene la gestión administrativa con ADMINISTRADOR; sus formularios se conservan como vistas previas, sin enviar cambios bajo un rol distinto.

## Consultas adaptadas al backend existente

| Vista | Ruta real del backend |
|---|---|
| Catálogo de productos | GET /cliente/productos |
| Catálogo de servicios | GET /cliente/servicios |
| Reporte Excel de Gerente General | GET /gerente/reportes/productos/excel |
| Inventario gerencial | GET /gerente/inventario/movimientos |
| Productos de almacén | GET /almacenista/productos |
| Proveedores de almacén | GET /almacenista/proveedores |
| Movimientos de almacén | GET /almacenista/inventario/movimientos |
| Productos, servicios y pedidos de ventas | GET /vendedor/productos, /vendedor/servicios, /vendedor/pedidos, /vendedor/ordenes-servicio |
| Repartidores disponibles | GET /vendedor/repartidores |
| Pedidos asignados | GET /repartidor/pedidos?idRepartidor=... |

El DTO público de producto informa disponibilidad, pero no cantidad de stock. El carrito muestra stock por confirmar, sin inventar existencias. El DTO público de servicios no incluye ID; el nombre identifica únicamente la vista del formulario y no se envía como un ID de base de datos.

Los pedidos de vendedor incluyen solo pendientes. Las cifras mostradas representan los registros disponibles, no un historial completo de ventas. La consulta de repartidor requiere introducir un ID; no se deduce de un login simulado. El backend solo devuelve entregas activas y no informa la fecha del pedido, fotografías ni firmas. La consulta de servicios asignados al personal todavía no existe.

## Carrito y solicitudes

Los botones Agregar muestran un aviso inferior izquierdo; Ver carrito abre una sidebar derecha. Su único botón principal, Continuar con la compra, abre /carrito. Cantidades, eliminación y subtotal funcionan en el navegador. La confirmación del pedido está deshabilitada: no se utiliza nuestro endpoint /api/checkout ni se reemplaza por otro que requiera inventar una identidad de cliente.

Los servicios públicos no muestran precios ni filtros de precio. Solicitar abre una página con ambiente, área, dirección, fecha/hora, frecuencia, necesidades y contacto. El envío, seguimiento y cotización se conservan como vistas pendientes, sin llamar a endpoints inexistentes ni registrar confirmaciones ficticias.

No se usan /api/auth, /api/panel, /api/public, /api/checkout ni /api/solicitudes del backend de la otra rama. El adaptador usa las rutas nativas anteriores y transforma sus DTO únicamente en el frontend.

## Validación

    npm run build
    npm run lint

Las pruebas de navegador verifican vistas y contratos mediante respuestas representativas de los DTO actuales. No sustituyen una prueba de autenticación o permisos del servidor cuando esos módulos se implementen.
