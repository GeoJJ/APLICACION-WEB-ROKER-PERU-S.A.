# Ejecución local con Tomcat integrado

Para generar el WAR y servir React desde Tomcat externo, consultar [la guía de despliegue](despliegue-war-tomcat.md). Esta página conserva el modo de desarrollo con Vite.

Esta versión reúne los avances de Andrea y las pruebas y configuración de Marco disponibles al 22 de noviembre de 2026, e incorpora una forma reproducible de ejecutar la aplicación con el Tomcat integrado de Spring Boot desde VS Code.

## Requisitos

- PostgreSQL en ejecución y una base `roker_bd` disponible.
- Un JDK compatible con Spring Boot, como el JDK 22 utilizado en la validación local.
- Node.js y npm compatibles con la versión de Vite del proyecto.
- Abrir en VS Code la carpeta que contiene `backend`, `frontend` y `scripts`.

El POM mantiene Java 24 como versión predeterminada de Andrea. El script local usa `JAVA_HOME` o busca un JDK en `%USERPROFILE%\.jdks` y `%ProgramFiles%\Java`; aplica su versión mediante `-Djava.version` sin modificar el POM. La comprobación realizada corresponde a Java 22.0.2, Node 26.2.0 y PostgreSQL 18.

## Iniciar desde VS Code

1. Abrir **Terminal > Ejecutar tarea**.
2. Elegir **Roker: iniciar aplicación local**.
3. Introducir en la terminal del backend la contraseña de PostgreSQL cuando se solicite. No se guarda en archivos.
4. Esperar el mensaje `Tomcat started on port 8080`.
5. Acceder a <http://127.0.0.1:5173/>.

También pueden iniciarse las tareas del backend y frontend por separado. Para detenerlas, usar **Terminal > Terminar tarea** o `Ctrl+C` en cada terminal.

No se instala Tomcat externo, no se genera WAR y no se configura inicio automático. La base de datos debe estar en ejecución antes de iniciar el backend.

## Iniciar desde terminales PowerShell

Desde la raíz del repositorio, en una terminal:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/iniciar-local.ps1 -Componente Backend
```

En otra terminal:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/iniciar-local.ps1 -Componente Frontend
```

El backend activa el perfil `local`, limpia las clases antiguas y arranca Spring Boot. El frontend instala sus dependencias si no existe `node_modules` y arranca Vite. Después de cambiar de rama o actualizar `package-lock.json`, ejecutar `npm.cmd ci` en `frontend`.

## Conexión y configuración

- Frontend: `http://127.0.0.1:5173` o `http://localhost:5173`.
- Backend con Tomcat integrado: `http://127.0.0.1:8080`.
- Proxy: el frontend envía `/api/...` al backend y elimina el prefijo `/api`.
- CORS local: admite únicamente los dos orígenes anteriores y conserva la descarga de comprobantes y reportes.
- OpenAPI local: `http://127.0.0.1:8080/v3/api-docs`.
- Swagger UI local: `http://127.0.0.1:8080/swagger-ui/index.html`.

Se pueden establecer `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD` en el entorno. Por defecto se utiliza `jdbc:postgresql://localhost:5432/roker_bd` y el usuario `postgres`; la contraseña es obligatoria y no se incorpora al repositorio.

`SERVER_PORT` permite cambiar el puerto del backend. Si se cambia, definir `API_PROXY_TARGET` con esa misma dirección antes de iniciar el frontend.

Hibernate conserva `ddl-auto=update` y la inicialización SQL de Andrea: al arrancar puede actualizar las tablas y ejecutar los datos iniciales existentes. No se sustituye ni se vacía la base local.

La documentación OpenAPI se habilita en `local` y `zap`; permanece deshabilitada sin esos perfiles. El perfil `zap` usa por defecto **otra base**, `roker_bd_zap`, y requiere `ZAP_DATASOURCE_PASSWORD`. Los escaneos activos deben hacerse sobre esa base separada.

## Verificación de la integración

La suite de pruebas está documentada en `backend/src/test/README.md`. Para incluir `DemoApplicationTests`, usar una base separada, por ejemplo `roker_bd_integracion_tests`; no apuntar las pruebas de contexto a una base con información de trabajo.

La aplicación ejecutada aquí es de desarrollo local. El empaquetado y la configuración para una entrega desplegada se prepararán por separado.
