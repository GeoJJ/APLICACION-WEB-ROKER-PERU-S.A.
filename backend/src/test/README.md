# Pruebas de Marco integradas con la versión de Andrea

Se recuperan las 20 clases originales de `development-marco` (`9fdf3a5`) desde su adaptación previa a Jasper (`c3636c6`) y se actualizan al contrato de `1ec8ead`, del 22 de noviembre de 2026. Se conserva además `DemoApplicationTests`, que ya estaba en la base de Andrea.

## Ejecución sin base de datos

Desde `backend`, con `JAVA_HOME` configurado para el JDK instalado:

```powershell
.\mvnw.cmd '-Djava.version=22' '-Dtest=!*DemoApplicationTests' test
```

Se validaron **266 pruebas**, sin fallos, errores ni omisiones. Son pruebas unitarias con Mockito y pruebas MVC con colaboradores simulados. Las pruebas MVC desactivan los filtros: verifican el contrato y la identidad que recibe el controlador; no certifican la validación criptográfica del JWT ni toda la autorización de Spring Security.

La configuración por defecto del POM conserva Java 24. El parámetro de Java 22 corresponde al JDK usado para esta verificación local; debe coincidir con el JDK elegido al ejecutar Maven.

## Suite completa con PostgreSQL separado

Crear una base vacía `roker_bd_integracion_tests` y configurar las variables antes de ejecutar todas las pruebas:

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/roker_bd_integracion_tests'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
$claveRoker = Read-Host 'Contraseña de la base de pruebas' -AsSecureString
$env:SPRING_DATASOURCE_PASSWORD = [System.Net.NetworkCredential]::new('', $claveRoker).Password
.\mvnw.cmd '-Djava.version=22' test
Remove-Item Env:SPRING_DATASOURCE_PASSWORD
Remove-Item Env:SPRING_DATASOURCE_URL
Remove-Item Env:SPRING_DATASOURCE_USERNAME
```

Se validaron **267 pruebas**, incluidas las anteriores y el arranque completo del contexto Spring Boot, sin fallos, errores ni omisiones. `DemoApplicationTests` ejecuta la inicialización de Hibernate y SQL: usar una base separada, nunca la base con información de trabajo.

Los resultados por clase se guardan en `pruebas_api/resultados-integracion-2026-11-22.json`.

## Cambios respecto de la adaptación anterior

Las pruebas comprueban los códigos automáticos, los DTO actuales, la cancelación con vendedor autenticado, las declaraciones del JWT del login y el reporte de inventario de Andrea. La numeración de comprobantes ya no utiliza una tabla de secuencias: la prueba solicita el bloqueo de PostgreSQL y consulta el último comprobante mediante repositorios simulados; no es una prueba de concurrencia real.

## OpenAPI y OWASP ZAP

Se recuperan la configuración OpenAPI y el perfil `zap` de Marco. Swagger se habilita en `local` y `zap`; queda deshabilitado en el perfil predeterminado. El perfil `zap` usa `roker_bd_zap` y requiere `ZAP_DATASOURCE_PASSWORD`, sin credenciales incrustadas. La integración no ejecuta un nuevo escaneo ZAP; no se reutilizan resultados antiguos como evidencia de un escaneo actual.

Consultar `docs/ejecucion-local-tomcat.md` para iniciar la aplicación.
