# Copia integrada sobre Jasper — 26 de noviembre de 2026

Base: `develop/prueba-jasper-report`, commit `0e1a0fd`.
Rama local: `integracion-jasper-vistas-2026-11-26`.

## Cambios reunidos

- Lista de servicios con categorías mediante Select UI, búsqueda, datos del servicio, fotografías de Jasper y enlace de contacto para contratos.
- Contacto con campos según el motivo y confirmación de envío simulado; ubicación en Arequipa.
- Nosotros con información institucional, fotografías, certificaciones y clientes guardados localmente.
- Navegación activa y footer de las pantallas de trabajadores.
- Fotografías CDN, datos iniciales y conexiones de almacén/repartidor de Jasper conservados.
- Pruebas de servicios y controladores adaptadas; comprobaciones del historial y de las restricciones del repartidor.
- Compatibilidad del cliente opcional en pedidos presenciales de bases antiguas.
- CORS para localhost y 127.0.0.1 en el perfil local. Proxy de Vite dirigido al contexto /roker-backend.
- OpenAPI habilitado solo en local y zap; configuración ZAP sin contraseñas incrustadas.
- Cargador opcional de datos mediante API, con control de reanudación. No se ejecutó: Jasper ya aporta data.sql; no mezclar las dos cargas sin revisar los datos existentes.

## Tomcat

Se conserva el empaquetado separado de Jasper y su inicializador. No se incorporan el perfil war, WarWebConfig, los scripts de despliegue ni el empaquetado conjunto de la versión anterior. Los archivos compilados de deployment/ROOT se actualizan con las vistas integradas.

En la revisión de la base de Jasper no se encontraron archivos de checksum ni generación de SHA-256. Esta integración no añade esa generación ni una segunda modalidad de WAR.

## Ejecución local

Se utilizó el JDK 22 instalado, mediante `-Djava.version=22`; el POM de Jasper conserva Java 24.
La configuración local privada de PostgreSQL y JWT está en backend/.env, ignorado por Git. Usar backend/.env.example como referencia en otra máquina.

Desde backend:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE/.jdks/corretto-22.0.2"
$env:Path = "$env:JAVA_HOME/bin;$env:Path"
.\mvnw.cmd -Djava.version=22 -Dspring-boot.run.profiles=local spring-boot:run
```

Desde frontend:

```powershell
npm ci
npm run dev -- --host 127.0.0.1
```

El frontend se consulta en http://127.0.0.1:5173 y el backend usa el contexto /roker-backend en el puerto 8080.
Las verificaciones se ejecutan contra roker_jasper_verificacion, una base separada de roker_bd. Para repetirlas, establecer SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME y SPRING_DATASOURCE_PASSWORD con esa base antes de ejecutar Maven. Las pruebas completas inicializan Hibernate y los datos de Jasper.

Los respaldos de las copias anteriores quedan fuera del repositorio activo, en output del directorio contenedor. No se publicaron cambios en GitHub.
## Resultado de la verificación

- `npm run lint`: sin errores ni advertencias.
- `npm run build`: correcto; Vite informa del tamaño del paquete JavaScript, sin impedir la compilación.
- `mvnw.cmd -Djava.version=22 package`: correcto, 273 pruebas, sin fallos, errores ni omisiones.
- Login con las seis cuentas por rol y consulta autorizada de cada panel: HTTP 200. Token con el rol esperado.
- Cliente consultando administración: HTTP 403.
- Catálogos reales a través del proxy: 17 productos y 8 servicios.
- Navegador: filtro de servicios, fotografías, formulario por contrato y confirmación simulada, Nosotros, agregado al carrito, sidebar, página del carrito, login administrativo, footer y rol en el header público comprobados.

El WAR generado queda en backend/target/demo-0.0.1-SNAPSHOT.war y contiene el backend. El frontend independiente actualizado está en deployment/ROOT. No contiene nuestra antigua modalidad de WAR conjunto.
