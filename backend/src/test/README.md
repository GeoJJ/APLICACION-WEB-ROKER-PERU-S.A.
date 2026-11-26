# Verificación del backend

Ejecutar desde backend con un JDK compatible y una base PostgreSQL separada, configurada mediante SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME y SPRING_DATASOURCE_PASSWORD. Definir JWT_SECRET o completar el archivo privado .env.

mvnw.cmd -Djava.version=22 test

El POM conserva Java 24; el parámetro 22 corresponde al JDK de esta máquina. Las pruebas están activas por defecto.

La suite incluye pruebas unitarias y MVC adaptadas de la integración anterior, además del arranque completo, CORS y protección de rutas. Las pruebas MVC con filtros desactivados comprueban contratos; no sustituyen la verificación de autorización con el servidor real.

El perfil local acepta los orígenes localhost:5173 y 127.0.0.1:5173. Las pruebas de CORS también comprueban que un origen no autorizado se rechaza.

No se incorporan pruebas del antiguo WAR conjunto porque ese despliegue está excluido de esta integración.
