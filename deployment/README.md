# Despliegue de ROKER PERU S.A. – Apache Tomcat 11

## 1. Descripción

Este directorio contiene los archivos necesarios para desplegar la aplicación web **ROKER PERU S.A.** utilizando Apache Tomcat 11.

La implementación integra los dos componentes principales del sistema:

- **Frontend:** aplicación desarrollada con React y Vite, compilada y desplegada bajo el contexto raíz (`ROOT`).
- **Backend:** API REST desarrollada con Java y Spring Boot, empaquetada en formato WAR y desplegada como `api.war`.

La aplicación utiliza PostgreSQL como sistema gestor de base de datos.

## 2. Estructura de los archivos

```text
deployment/
├── README.md
├── api.war
└── ROOT/
    ├── index.html
    └── assets/
```

- `api.war`: archivo empaquetado del backend.
- `ROOT/`: archivos compilados del frontend.
- `README.md`: instrucciones de instalación y despliegue.

## 3. Requisitos previos

Antes de iniciar el despliegue, se requiere:

- Java JDK 24.
- Apache Tomcat 11 (versión ZIP de 64 bits).
- PostgreSQL instalado y configurado.
- Base de datos `roker_bd` previamente creada y preparada.
- Archivos `ROOT/` y `api.war` incluidos en este directorio.

## 4. Procedimiento de despliegue

### 4.1. Descargar e instalar Apache Tomcat

1. Descargar Apache Tomcat 11 desde su [página oficial](https://tomcat.apache.org/download-11.cgi).
2. Seleccionar la distribución ZIP de 64 bits para Windows.
3. Descomprimir el archivo en una ubicación de fácil acceso, por ejemplo:

```text
C:\tomcat11
```

Se recomienda utilizar la distribución ZIP para gestionar el servidor mediante sus scripts de ejecución.

### 4.2. Preparar el directorio de aplicaciones

Ingresar al directorio:

```text
C:\tomcat11\webapps
```

Eliminar la aplicación ROOT predeterminada de Tomcat y las versiones anteriores de la aplicación, si existen.

Copiar los siguientes elementos desde este directorio de despliegue:

```text
ROOT/
api.war
```

La estructura final debe quedar así:

```text
C:\tomcat11\webapps\
├── ROOT\
└── api.war
```

Tomcat desplegará el frontend en el contexto raíz y el backend bajo el contexto `/api`.

### 4.3. Configurar las variables de entorno

Ingresar al directorio:

```text
C:\tomcat11\bin
```

Crear un archivo denominado `setenv.bat` y configurar las variables de entorno necesarias.

Ejemplo de configuración:

```bat
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.4"
set "DB_PASSWORD=TU_CONTRASEÑA"
set "JWT_SECRET=CLAVE_JWT"
```

**Importante:**

- Verificar que `JAVA_HOME` corresponda a la ubicación real del JDK instalado.
- Reemplazar `TU_CONTRASEÑA` por la contraseña correspondiente al usuario de PostgreSQL.
- JWT_SECRET: clave secreta utilizada para la generación y validación de tokens JWT.
- Configurar las demás variables requeridas por el backend, si corresponde.
- No compartir ni subir el archivo `setenv.bat` con credenciales reales al repositorio.

### 4.4. Iniciar el servidor Apache Tomcat

Para iniciar el servidor y visualizar los registros de ejecución, se utiliza el script `catalina.bat` en modo consola.

1. Abrir el Símbolo del sistema (CMD).
2. Acceder al directorio `bin` de Apache Tomcat mediante el siguiente comando:

```cmd
cd "C:\Program Files\Apache Software Foundation\Tomcat 11.0\bin"
```

3. Ejecutar el servidor mediante:

```cmd
catalina.bat run
```

4. Esperar a que Tomcat complete el proceso de inicio y despliegue de las aplicaciones.

**Importante:** mantener abierta la ventana de CMD mientras se utiliza el sistema. Al ejecutar `catalina.bat run`, los registros del servidor se muestran directamente en la consola. Si se cierra la ventana o se detiene el proceso, el servidor dejará de funcionar.

Para detener el servidor, utilizar la combinación `Ctrl + C` en la consola.

### 4.5. Verificar el inicio del servidor

Durante el inicio, se deben visualizar los datos de configuración de Tomcat, incluyendo las rutas `CATALINA_BASE`, `CATALINA_HOME` y `JRE_HOME`.

También se debe comprobar que las aplicaciones `ROOT` y `api` se hayan desplegado correctamente y que Spring Boot complete su inicialización.

### 4.6. Acceder al sistema

Una vez iniciado correctamente el servidor, abrir el navegador e ingresar a:

**Aplicación web (Frontend):**

```text
http://localhost:8080/
```

Para verificar el backend, utilizar un endpoint existente de la API, ya que la ruta raíz `/api/` no necesariamente devuelve una respuesta válida.

## 5. Arquitectura del despliegue

La implementación utiliza Apache Tomcat como servidor de aplicaciones, alojando tanto el frontend compilado como el backend empaquetado en formato WAR.

```text
              USUARIO
                 |
                 v
          NAVEGADOR WEB
                 |
                 v
       APACHE TOMCAT 11
           Puerto 8080
                 |
          +------+------+
          |             |
          v             v
      FRONTEND       BACKEND
        ROOT/         api.war
      React/Vite    Spring Boot
          |             |
          |             v
          |         PostgreSQL
          |          roker_bd
          |             |
          +-------------+
```

## 6. Verificación del despliegue

Para comprobar la correcta implementación se realizaron las siguientes verificaciones:

- Inicio del servidor Apache Tomcat 11.
- Despliegue del frontend mediante el contexto raíz `ROOT`.
- Despliegue del backend mediante el archivo `api.war`.
- Acceso a la aplicación desde el navegador.
- Verificación del funcionamiento de los servicios del backend.
- Comprobación de la conexión con la base de datos PostgreSQL.
