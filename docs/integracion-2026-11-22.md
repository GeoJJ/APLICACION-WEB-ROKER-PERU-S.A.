# Integración de avances al 22 de noviembre de 2026

Se consolidan los avances disponibles de Andrea y Marco hasta esta fecha, junto con la preparación de la ejecución local mediante Apache Tomcat integrado en Spring Boot.

## Procedencia

- Base de Andrea: `develop/prueba-jasper-report`, commit `1ec8eadfe02254a060f510a911ffdc0f3d490ea7` (abreviado `1ec8ead`). La referencia abreviada identifica el commit base de esta integración.
- Trabajo original de Marco: `development-marco`, commit `9fdf3a5`.
- Adaptación previa de las pruebas a Jasper: `c3636c62ed45b4f050143f0e72a87a325641c4fb`.

La rama nueva parte del commit actual de Andrea y mantiene su historial, sus pantallas y sus cambios de backend. Se recuperan las 20 clases de pruebas de Marco, junto con OpenAPI y el perfil ZAP, y se adaptan al contrato actual. No se sustituyen los controladores o servicios de Andrea por versiones antiguas.

## Adaptaciones de las pruebas

- Los códigos de productos, pedidos, servicios y órdenes se generan en el servidor.
- Los comprobantes utilizan el último número emitido y el bloqueo de PostgreSQL, en lugar de la antigua tabla de secuencias. La prueba unitaria verifica la solicitud del bloqueo; no simula concurrencia real de PostgreSQL.
- Los DTO incluyen los campos actuales de contacto, dirección y detalle de la solicitud.
- La cancelación de órdenes recibe el identificador del vendedor autenticado.
- El JWT del login incluye el rol y la versión de sesión.
- Las pruebas de Excel leen el reporte de inventario y sus datos del repositorio, en lugar del antiguo archivo de ejemplo.

Los dos casos que esperaban rechazar códigos manuales duplicados en `ProductoServiceTest` se reemplazan por la comprobación de la generación automática. Las tres pruebas MVC que esperaban errores de validación por el código manual ahora comprueban que ese campo no forma parte del contrato de entrada. No se eliminan archivos de producción.

## Ejecución local incorporada

Se añaden un perfil local, un script de inicio y tareas de VS Code para ejecutar el frontend y el backend con Tomcat integrado. El script permite usar el JDK instalado, limpia clases de otras versiones y solicita la contraseña de PostgreSQL sin publicarla. CORS y el proxy admiten las direcciones locales verificadas.

No se incluye instalación de Tomcat externo, servicio de inicio automático ni configuración de un servidor público.

## Validación

Los resultados de Maven por clase se registran en `pruebas_api/resultados-integracion-2026-11-22.json`. La compilación del frontend se verifica con `npm.cmd run build`. Las pruebas de contexto utilizan una base PostgreSQL separada de la aplicación.
