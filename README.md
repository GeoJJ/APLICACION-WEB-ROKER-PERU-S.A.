# APLICACI-N-WEB-ROKER-PERU-S.A.

## Integración y ejecución local

Esta rama consolida los avances de Andrea y Marco disponibles al 22 de noviembre de 2026 e incorpora el inicio local con Apache Tomcat integrado en Spring Boot.

- [Cambios y procedencia de la integración](docs/integracion-2026-11-22.md).
- [Iniciar frontend y Tomcat desde VS Code](docs/ejecucion-local-tomcat.md).
- [Ejecutar y consultar las pruebas](backend/src/test/README.md).

En VS Code: **Terminal > Ejecutar tarea > Roker: iniciar aplicación local**. PostgreSQL debe estar disponible; la contraseña se solicita en la terminal del backend.

## Despliegue completo en Tomcat externo

La configuración permite generar `backend/target/ROOT.war` con React incluido y publicarlo desde una sola dirección, sin Vite. Consultar [la guía de despliegue WAR en Tomcat 11](docs/despliegue-war-tomcat.md).

## Datos de productos, servicios y operaciones

Se incorporan los datos publicados por Andrea y un cargador para la API de Tomcat que conserva registros y permite reanudar sin repetir operaciones. Consultar [la guía de carga de datos](prueba_datos/README.md).
