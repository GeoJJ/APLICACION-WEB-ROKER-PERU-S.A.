# Roker Perú S.A. — Aplicación web

Aplicación para la comercialización de productos de bioseguridad y la gestión de servicios de limpieza, desinfección y atención especializada. Integra un portal público con catálogos, carrito y solicitudes, junto con módulos de administración, ventas, almacén, reparto y gerencia.

El sistema utiliza React para la interfaz, una API REST con Spring Boot para las operaciones y PostgreSQL para la persistencia. Este documento reúne la descripción del proyecto y las instrucciones de configuración, ejecución y mantenimiento.

## Contenido

- [Evolución del proyecto](#evolución-del-proyecto)
- [Funciones y roles](#funciones-y-roles)
- [Tecnologías y arquitectura](#tecnologías-y-arquitectura)
- [Estructura del repositorio](#estructura-del-repositorio)
- [Configuración y ejecución local](#configuración-y-ejecución-local)
- [Base de datos y carga inicial](#base-de-datos-y-carga-inicial)
- [Seguridad y sesiones](#seguridad-y-sesiones)
- [Mensajes y logs](#mensajes-y-logs)
- [Pruebas y verificaciones](#pruebas-y-verificaciones)
- [Compilación y despliegue](#compilación-y-despliegue)
- [Alcance actual](#alcance-actual)

## Evolución del proyecto

El desarrollo se ha organizado en las siguientes etapas:

1. **Portal e interfaces iniciales.** Construcción de Inicio, Productos, Servicios, Nosotros y Contacto, con navegación, cabecera, pie de página, tipografía y colores compartidos.
2. **Pantallas de trabajo.** Incorporación de paneles y vistas para administración, ventas, almacén y reparto, tomando los reportes como referencia de diseño. Separación de las funciones del administrador y del gerente general para ajustarlas al backend.
3. **Integración con la API.** Conexión progresiva de los catálogos, gestión administrativa, inventario, pedidos, solicitudes, perfiles y reportes con datos persistidos en PostgreSQL.
4. **Autenticación por rol.** Implementación del login con JWT, rutas protegidas, identificación de la cuenta en las cabeceras y acceso al perfil y al panel correspondiente.
5. **Compra y contratación.** Desarrollo del aviso de producto agregado, carrito lateral, página del carrito y registro de pedidos. Incorporación del formulario de solicitud de servicios con información del ambiente y la programación.
6. **Gestión operativa.** Conexión de ingresos y ajustes de almacén, atención de solicitudes, asignación de personal, confirmación de pedidos, ventas presenciales, reparto e historial.
7. **Reportes y comprobantes.** Incorporación de reportes de inventario en Excel, reportes de ventas en PDF y comprobantes para pedidos y órdenes de servicio.
8. **Mejora del portal público.** Presentación de servicios en lista, categorías mediante Select UI, fotografías de catálogo, contacto según el motivo de consulta e información institucional con certificaciones y clientes.
9. **Despliegue, verificación y mantenimiento.** Preparación del WAR, integración de pruebas, configuración de CORS y proxy, protección de credenciales mediante variables de entorno y registro de operaciones. Limpieza de elementos sin uso y unificación de búsquedas repetidas.

Durante el desarrollo también se preparó una modalidad de WAR que reunía frontend y backend. La configuración vigente conserva el despliegue separado: frontend en `ROOT/` y backend en `api.war`.

## Funciones y roles

### Portal público y cuenta del cliente

- Inicio con acceso a productos, categorías y servicios.
- Catálogo de productos con filtros, precios, imágenes y disponibilidad.
- Carrito persistente en el navegador, aviso al agregar productos, panel lateral, edición de cantidades y resumen de compra.
- Registro de pedidos desde una cuenta de cliente con datos de entrega y método de pago.
- Catálogo de servicios en lista con búsqueda, categorías, descripción, duración estimada, personal requerido e imagen.
- Solicitud de servicios con datos del ambiente, ubicación y programación. La atención requiere revisión y confirmación del vendedor.
- Consulta de solicitudes, edición del perfil y descarga de comprobantes cuando corresponda.
- Página Nosotros con contenido institucional, valores, certificaciones y clientes.
- Contacto con ubicación referencial en Arequipa y campos que cambian según el motivo, incluidos servicios de mayor alcance y contratos por un periodo definido.

El formulario de Contacto muestra una confirmación de envío **simulado**; no envía correos. Los métodos de pago registrados en pedidos tampoco representan un cobro mediante una pasarela bancaria.

### Módulos de trabajo

| Rol | Funciones principales |
|---|---|
| Administrador | Gestión de trabajadores y roles, productos, servicios, proveedores, personal de servicio e inventario. |
| Gerente general | Resumen de gestión, movimientos de inventario y reportes de inventario y ventas. |
| Vendedor | Atención de pedidos y solicitudes, asignación de personal y repartidores, programación, confirmación, ventas y atención en mostrador. |
| Almacenista | Consulta de stock, productos y proveedores, registro de ingresos y ajustes, consulta de movimientos. |
| Repartidor | Consulta de entregas asignadas, actualización del estado e incidencias e historial. |
| Cliente | Registro de compras y solicitudes, consulta de sus registros, perfil y comprobantes autorizados. |

### Flujo de operaciones

Los pedidos de productos se crean pendientes, se confirman y asignan para reparto y pueden marcarse como entregados. El repartidor solo puede actualizar pedidos asignados que estén en ruta; no puede cancelar pedidos ni modificar entregas ya finalizadas.

Las ventas presenciales se registran directamente como entregadas. Los comprobantes distinguen productos y servicios y, según el tipo de cliente, boleta o factura. La numeración utiliza bloqueos de PostgreSQL y restricciones de unicidad para evitar duplicados.

## Tecnologías y arquitectura

| Componente | Tecnologías |
|---|---|
| Frontend | React 19, Vite 8, React Router 7, Tailwind CSS 4, Axios, Lucide y fuente Poppins. |
| Backend | Java, Spring Boot 4.1.1, Spring MVC, Spring Security, Bean Validation, JPA/Hibernate y Lombok. |
| Persistencia | PostgreSQL. |
| Autenticación | JWT y contraseñas protegidas con BCrypt. |
| Documentos | JasperReports 7.0.8 para PDF y Apache POI para Excel. |
| Verificación | JUnit, Mockito, pruebas MVC, ESLint, colección Bruno y perfil para OWASP ZAP. |
| Ejecución | Maven Wrapper, Tomcat integrado de Spring Boot y configuración para Tomcat externo. |

Las pantallas llaman a las funciones de API del frontend. Axios centraliza la conexión y el envío del token; los controladores verifican permisos y delegan las operaciones en servicios compartidos. Los repositorios gestionan el acceso a PostgreSQL y los DTO delimitan las entradas y respuestas.

Los controladores de distintos roles se mantienen separados aunque compartan un servicio: esa separación permite conservar sus rutas y restricciones de acceso.

## Estructura del repositorio

```text
backend/
  src/main/java/RokerPeru/demo/
    config/ controller/ dto/ entities/ enums/
    exception/ logging/ repository/ security/ services/
  src/main/resources/
    application*.properties
    data.sql
    schema.sql
    reports/
  src/test/
frontend/
  src/
    assets/ components/ data/ hooks/ lib/ pages/ routes/
  public/
deployment/ROOT/
prueba_datos/
pruebas_api/ROKER PERU Pruebas API/
scripts/
README.md
```

`deployment/ROOT/` contiene el frontend compilado para Tomcat. `prueba_datos/` y `scripts/` conservan la colección y el cargador opcional mediante API. `pruebas_api/` contiene las peticiones Bruno; las pruebas automatizadas del backend están en `backend/src/test/`.

## Configuración y ejecución local

### Requisitos

- JDK 24 para utilizar la versión de Java declarada en el POM.
- Node.js compatible con las dependencias: `^20.19.0` o `>=22.12.0`, junto con npm.
- PostgreSQL en ejecución; en esta configuración se utiliza el puerto `5432`.
- Git para obtener el repositorio. Maven se ejecuta mediante el Wrapper incluido.

### Base y credenciales locales

Crear la base desde pgAdmin o una consola SQL:

```sql
CREATE DATABASE roker_bd;
```

Desde `backend`, copiar el ejemplo de configuración y completar el archivo privado:

```powershell
Copy-Item .env.example .env
```

```dotenv
ROKER_DB_PASSWORD=CONTRASEÑA_LOCAL_DE_POSTGRESQL
JWT_SECRET=SECRETO_BASE64_PROPIO_DE_AL_MENOS_32_BYTES
```

Para generar un secreto en PowerShell:

```powershell
$bytes = New-Object byte[] 32
$generador = [Security.Cryptography.RandomNumberGenerator]::Create()
$generador.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$generador.Dispose()
```

La conexión predeterminada es `jdbc:postgresql://localhost:5432/roker_bd`, con usuario `postgres`. Se puede sustituir mediante `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`. El archivo `.env` está ignorado por Git y se busca respecto del directorio de ejecución; iniciar el backend desde su propia carpeta.

### Backend

Con `JAVA_HOME` y el JDK configurados, ejecutar desde `backend`:

```powershell
.\mvnw.cmd -Dspring-boot.run.profiles=local spring-boot:run
```

El backend integrado se publica bajo `http://localhost:8080/roker-backend`. Una consulta pública para comprobarlo es `/roker-backend/cliente/productos`.

### Frontend

En otra terminal, desde `frontend`:

```powershell
npm ci
npm run dev -- --host 127.0.0.1
```

Abrir `http://127.0.0.1:5173`. El frontend llama a `/api`; el proxy de Vite retira ese prefijo y reenvía la petición a `http://localhost:8080/roker-backend`. Si cambia el destino, definir `API_PROXY_TARGET` en el entorno de ejecución de Vite. `VITE_API_URL` controla la base utilizada por Axios.

El perfil `local` permite CORS desde `http://localhost:5173` y `http://127.0.0.1:5173`. Detener cada proceso con `Ctrl+C`.

## Base de datos y carga inicial

Hibernate utiliza `ddl-auto=update`. Después de inicializar las entidades, Spring ejecuta los scripts SQL. `schema.sql` adapta la relación de cliente en pedidos para permitir ventas presenciales en bases anteriores.

`data.sql` incorpora roles, usuarios, proveedores, productos, servicios, personal, clientes presenciales, movimientos, pedidos, detalles, órdenes y asignaciones. El archivo contiene actualmente inserciones para **6 roles, 15 usuarios, 5 proveedores, 17 productos y 8 servicios**, además de los registros operativos. Son cantidades del script inicial, no límites ni totales garantizados de una base ya utilizada.

El script emplea identificadores explícitos, `ON CONFLICT DO NOTHING` y ajuste de secuencias. Puede conservar registros existentes; no reemplaza una migración ni actualiza automáticamente todos los valores que ya estén guardados.

Para acceder con los datos iniciales, la cuenta administradora del entorno de demostración es `admin@rokerperu.com`, con contraseña `admin123`. La colección Bruno utiliza `Clave12345` para los usuarios que crea. Los usuarios iniciales y sus roles pueden consultarse en el script y en la gestión de trabajadores. Estas cuentas pertenecen al entorno de demostración y deben sustituirse en una instalación con datos reales.

## Seguridad y sesiones

- El login autentica contra el backend y devuelve un JWT con identidad, rol y versión de sesión.
- Axios adjunta el token a las peticiones autenticadas y comunica la pérdida de sesión a la interfaz.
- El frontend limita las rutas por rol; el backend aplica sus propias comprobaciones y valida la propiedad de pedidos, solicitudes y comprobantes cuando corresponde.
- El registro público asigna el rol de cliente desde el servidor.
- El cierre de sesión incrementa la versión de sesión del usuario e invalida sus tokens anteriores.
- Las contraseñas se almacenan con BCrypt. El secreto JWT y las contraseñas locales se configuran mediante variables de entorno.

La configuración común está en `application.properties`. Los perfiles `local` y `zap` la complementan; no necesitan repetir todas las propiedades. Swagger/OpenAPI está deshabilitado por defecto y habilitado en esos perfiles.

## Mensajes y logs

La aplicación registra mensajes en consola y en `backend/logs/roker-peru.log` cuando se ejecuta desde `backend`. El perfil ZAP escribe en `logs/roker-peru-zap.log`, relativo a su directorio de ejecución. La configuración conserva rotación de archivos, tamaño máximo de `10 MB`, historial de `30` y límite total de `1 GB`.

| Nivel | Uso |
|---|---|
| `INFO` | Login y cierre de sesión, operaciones guardadas, pedidos, inventario, reportes y comprobantes. |
| `WARN` | Credenciales incorrectas, accesos rechazados y situaciones previstas que requieren atención. |
| `ERROR` | Errores internos inesperados, gestionados desde el manejador central. |
| `DEBUG` | Diagnóstico; incluye generación de códigos en el perfil local. |

`RegistroOperacion` incorpora la identidad disponible, limpia saltos de línea de los argumentos y aplaza los mensajes de éxito hasta confirmar la transacción. Si se revierte, esos mensajes no se publican. Los rechazos de seguridad no se registran de nuevo por el despacho interno de error.

## Pruebas y verificaciones

### Backend

Crear una base independiente, por ejemplo `roker_jasper_verificacion`. Desde `backend`, con el secreto JWT configurado y las credenciales correspondientes a esa base:

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/roker_jasper_verificacion'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
$clave = Read-Host 'Contraseña de la base de verificación' -AsSecureString
$env:SPRING_DATASOURCE_PASSWORD = [Net.NetworkCredential]::new('', $clave).Password
.\mvnw.cmd test
```

La última suite completa verificada el **26 de noviembre de 2026** ejecutó **278 pruebas sin fallos, errores ni omisiones**. Tras el ajuste final de registros de rechazo, se verificaron adicionalmente **14 pruebas** de autenticación, contexto y logging.

La cobertura incluye servicios, contratos MVC, arranque, CORS, protección de rutas y mensajes después del commit o rollback. Las pruebas MVC que desactivan filtros comprueban contratos, no toda la seguridad; también se verificaron peticiones reales con tokens y roles.

### Frontend y API

Desde `frontend`:

```powershell
npm run lint
npm run build
```

Se verificaron el login de los seis roles, rechazo de accesos no autorizados, catálogos, filtro de servicios, contacto simulado, carrito, navegación, cabeceras y footer. También se comprobaron mensajes reales de creación y eliminación de un producto temporal, generación de PDF y cierre de sesión en la base separada.

La colección `pruebas_api/ROKER PERU Pruebas API/` contiene **446 peticiones HTTP en archivos Bruno**. Abrirla en Bruno, seleccionar el entorno **Local** y ejecutar las carpetas numeradas en orden; comparten identificadores y tokens y modifican datos. Su entorno Local apunta a `/roker-backend`; las variables predeterminadas de la colección sin seleccionar ese entorno no incluyen ese contexto.

## Compilación y despliegue

### Generación de archivos

Compilar el frontend mediante `npm run build` desde `frontend`. El resultado se genera en `frontend/dist/`.

Desde `backend`, en una terminal configurada con la base de verificación indicada anteriormente, ejecutar:

```powershell
.\mvnw.cmd clean package
```

### Tomcat externo

El despliegue separado está preparado para Apache Tomcat 11:

1. Configurar un JDK compatible con la versión usada para compilar y la conexión a PostgreSQL.
2. Copiar el contenido actualizado de `frontend/dist/` a `webapps/ROOT/`, conservando el archivo `WEB-INF/web.xml` de `deployment/ROOT/` para las rutas del frontend.
3. Copiar el WAR generado como `webapps/api.war`.
4. Configurar las variables del proceso de Tomcat, incluido `ROKER_DB_PASSWORD` y `JWT_SECRET`. Se pueden sustituir URL, usuario y contraseña con las variables `SPRING_DATASOURCE_*`.
5. Ejecutar `catalina.bat run` desde el directorio `bin` de Tomcat.
6. Comprobar el portal en `http://localhost:8080/` y un endpoint público como `http://localhost:8080/api/cliente/productos`.

En este despliegue, el frontend utiliza `/api` y el nombre `api.war` define el contexto del backend. En la ejecución integrada de Spring Boot se utiliza `/roker-backend`. PostgreSQL y las variables locales deben prepararse también en la máquina receptora: enviar solo el WAR no incluye la base de datos.

## Alcance actual

El proyecto cuenta con integración funcional para los módulos descritos y mantiene estas delimitaciones:

- El envío de Contacto es simulado y no tiene un backend de correo.
- Los pagos se registran como información del pedido; no se ejecutan cobros en línea.
- La API de reparto no proporciona fotografía de entrega ni firma.
- El perfil ZAP prepara el entorno de análisis; no sustituye un escaneo actualizado.
- El despliegue actual mantiene frontend y backend separados y requiere preparar la base y las variables en cada entorno.

Las configuraciones privadas, logs, dependencias instaladas, resultados compilados y estado local del cargador se mantienen fuera del control de versiones según `.gitignore`.
