# Carga opcional mediante API

Se conserva el cargador de la integración anterior. No forma parte del despliegue de Tomcat y no se ejecuta automáticamente.

Jasper ya inicializa datos mediante backend/src/main/resources/data.sql. Antes de usar esta colección, revisar los registros existentes: el cargador puede añadir movimientos, pedidos y solicitudes.

Ejecutar scripts/cargar-datos-andrea.ps1 indicando -ApiUrl si cambia el contexto. Solicita las credenciales del administrador; guarda su control de reanudación en .local/carga-andrea. No trasladar estados de otro servidor.
