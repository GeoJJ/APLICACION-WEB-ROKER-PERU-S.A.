# Despliegue del WAR en Apache Tomcat 11

El paquete `backend/target/ROOT.war` incluye el backend Spring Boot y el frontend React compilado. Tomcat sirve ambos desde `http://localhost:8080`; no se necesita ejecutar Vite para usar esta versión.

Se utiliza Tomcat 11 porque Spring Boot 4.1 requiere un contenedor Servlet 6.1. La configuración de WAR y `SpringBootServletInitializer` sigue la [documentación de Spring Boot](https://docs.spring.io/spring-boot/4.1/how-to/deployment/traditional-deployment.html).

## Requisitos

- Un JDK compatible: la validación se realizó con Amazon Corretto 22.0.2. El POM conserva Java 24; el script aplica la versión del JDK local mediante un parámetro de Maven. El servidor debe usar una versión de Java igual o superior a la usada para compilar.
- Node.js y npm para compilar React. No son necesarios en el servidor que únicamente recibe el WAR.
- PostgreSQL y la base de datos de la aplicación, `roker_bd`.
- Otra base PostgreSQL vacía para las pruebas: `roker_bd_integracion_tests`.
- Una distribución oficial de Apache Tomcat 11 extraída. La validación utiliza Tomcat 11.0.29.

## 1. Generar el WAR

Detener los procesos de desarrollo del frontend y backend antes de compilar: `npm ci` actualiza dependencias y `clean` reemplaza las clases compiladas.

En VS Code: **Terminal > Ejecutar tarea > Roker: generar WAR con pruebas**. Introducir la contraseña de PostgreSQL para la base de pruebas cuando se solicite.

Equivalente desde PowerShell, en la raíz del repositorio:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/generar-war.ps1
```

El script ejecuta `mvnw.cmd -Djava.version=<JDK local> clean package`. Maven instala las dependencias de React con `npm ci`, compila el frontend, lo incorpora a los recursos del backend, ejecuta las pruebas y genera `backend/target/ROOT.war`. No se omiten las pruebas. También se genera `ROOT.war.sha256` para verificar la integridad del WAR.

Si se ejecuta Maven directamente desde `backend`, configurar previamente `JAVA_HOME`, `SPRING_DATASOURCE_URL` hacia la base de pruebas, `SPRING_DATASOURCE_PASSWORD` y `JWT_SECRET`. Con Java 24 puede usarse `mvnw.cmd clean package`; con el JDK 22 validado, `mvnw.cmd -Djava.version=22 clean package`.

El script usa por defecto `jdbc:postgresql://localhost:5432/roker_bd_integracion_tests`. Para cambiar la base de pruebas, usar `-UrlBasePruebas`. La creación de esa base se realiza en PostgreSQL o pgAdmin antes de compilar. El usuario de PostgreSQL se puede indicar mediante `SPRING_DATASOURCE_USERNAME`.

## 2. Desplegar en Tomcat externo

Detener el backend anterior si está usando el puerto 8080. En VS Code: **Terminal > Ejecutar tarea > Roker: desplegar WAR en Tomcat 11**. Indicar la carpeta extraída de Tomcat, que contiene `bin`, `conf` y `lib`, e introducir la contraseña de la base de la aplicación.

Equivalente desde PowerShell:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File scripts/desplegar-tomcat.ps1 -TomcatHome 'C:\servidores\apache-tomcat-11.0.29'
```

La ruta es un ejemplo: utilizar la ubicación real de Tomcat. También puede definirse `CATALINA_HOME` y omitir `-TomcatHome`.

El script crea una instancia independiente en `.local/tomcat`, copia `ROOT.war` y ejecuta `catalina.bat run`. No sobrescribe las aplicaciones de la distribución instalada ni configura servicios de inicio automático. La carpeta `.local` se excluye de Git. Para detener el servidor, usar `Ctrl+C` en su terminal.

Al desplegarse en Tomcat externo, el inicializador activa automáticamente el perfil `war`. Las credenciales se reciben mediante `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`. Por defecto se conecta a `roker_bd`, nunca a la base de pruebas del script de compilación.

`JWT_SECRET` debe ser una clave hexadecimal válida de al menos 256 bits. Si no está definida, el script genera una clave temporal en memoria; los tokens de ejecuciones anteriores dejan de ser válidos. Para conservar sesiones entre reinicios, proporcionar la misma clave mediante una configuración externa. No publicar claves ni contraseñas en Git.

La configuración está preparada para un servidor local de pruebas. Hibernate conserva `update` y la inicialización SQL existente: puede agregar o modificar estructuras al arrancar. No constituye una configuración de producción pública.

## 3. Acceso y verificación

- Portal y pantallas: `http://localhost:8080/`.
- Productos públicos: `http://localhost:8080/api/cliente/productos`.
- Login: `POST http://localhost:8080/api/auth/login`.
- Administración: la pantalla `/admin/productos` devuelve React; su API `/api/admin/productos` sigue exigiendo autenticación y el rol correspondiente.

El perfil `war` añade `/api` únicamente a los controladores del proyecto. El frontend mantiene ese prefijo, por lo que el despliegue no necesita el proxy de Vite. Se registran las rutas de React para permitir recargas directas sin error 404. Swagger permanece deshabilitado en este perfil.

El nombre `ROOT.war` publica la aplicación en la raíz del servidor. No renombrarlo para desplegarlo bajo un contexto diferente sin adaptar primero la base de React, sus recursos y las direcciones de la API.

Para usar otro puerto, pasar `-Puerto 8081` al script. La instancia se enlaza a `127.0.0.1`; este procedimiento no habilita acceso desde otros equipos.

## Alcance de esta preparación

Se verifica el empaquetado, el arranque en Tomcat externo, la navegación directa, los recursos estáticos, el login y la separación de los permisos de la API. La matriz del 80 % de requisitos funcionales, el ZIP final con SHA-256 y la actualización del informe se preparan por separado.
