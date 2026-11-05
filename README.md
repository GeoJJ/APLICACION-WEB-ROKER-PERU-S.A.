# APLICACI-N-WEB-ROKER-PERU-S.A.

## Ejecutar el proyecto

Primero prepara PostgreSQL y ejecuta el backend según [backend/README.md](backend/README.md).
En otra terminal, desde `frontend`:

```powershell
npm.cmd ci
npm.cmd run dev
```

Abre `http://localhost:5173`. El proxy de Vite envía `/api` al backend en el puerto 8080.
Si había un Vite abierto antes de estos cambios, reinícialo para que tome la configuración del proxy.

El login, catálogo, perfiles y paneles usan el backend. El header muestra el rol real
y permite editar perfil, abrir el Panel de gestión y cerrar sesión. Los permisos se
validan tanto en las rutas del frontend como en la API.

Validación del frontend:

```powershell
cd frontend
npm.cmd run build
npm.cmd run lint
```
