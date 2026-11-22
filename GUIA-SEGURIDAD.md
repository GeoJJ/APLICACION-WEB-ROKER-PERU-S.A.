# Guía de seguridad, arranque y continuación

> Documento de apoyo para entender el cambio de seguridad JWT del commit `a4f2c73`
> y saber cómo levantar y continuar el proyecto.
> Última revisión: 13/11/2026.

---

## Índice

1. [Resumen](#1-resumen)
2. [Arquitectura actual](#2-arquitectura-actual)
3. [Cómo levantar el proyecto](#3-cómo-levantar-el-proyecto)
4. [Qué quedó a medias](#4-qué-quedó-a-medias)
5. [Plan para terminarlo](#5-plan-para-terminarlo)
6. [Cómo probar la seguridad](#6-cómo-probar-la-seguridad)
7. [Problemas frecuentes](#7-problemas-frecuentes)
8. [Notas de seguridad](#8-notas-de-seguridad)
9. [Mapa de archivos](#9-mapa-de-archivos)

---

## 1. Resumen

Antes del commit de seguridad, el frontend funcionaba como **vista previa**: las rutas
no comprobaban sesión y el backend no pedía autenticación. El commit
`a4f2c73` agregó un login real con **JWT (JSON Web Token)**:

- El usuario envía correo y contraseña a `POST /auth/login`.
- Spring valida las credenciales con BCrypt y devuelve un token firmado.
- El frontend guarda el token y lo envía en cada petición:
  `Authorization: Bearer <token>`.
- Spring valida el token, carga el usuario y sus roles, y aplica
  `@PreAuthorize("hasRole('...')")` en cada controlador.

La idea es correcta, pero **la integración quedó a medias**: el login existe, pero el
resto del frontend sigue usando un cliente HTTP que no envía el token y el token no
incluye el rol. Por eso el proyecto "ya no se entiende" y varias pantallas fallan.

---

## 2. Arquitectura actual

### 2.1 Flujo de login

```
LoginForm.jsx
   │  correo + contraseña
   ▼
AuthProvider.login()
   │  POST /auth/login
   ▼
AuthController  →  AuthService  →  AuthenticationManager
   │                                  │
   │                                  ├─ UsuarioRepository (tabla usuario)
   │                                  └─ BCryptPasswordEncoder
   ▼
JwtService.generateToken(usuario)
   │  token firmado (24 h)
   ▼
React: localStorage.setItem('token', token)
   │
   ├─ jwtDecode(token) → correo y (debería) rol
   └─ axios interceptor → Authorization: Bearer <token>
            │
            ▼
JwtAuthenticationFilter
   │  extrae el correo, carga el Usuario y valida el token
   ▼
SecurityContextHolder → @PreAuthorize("hasRole('ADMINISTRADOR')")
```

### 2.2 Archivos clave del backend

| Archivo | Responsabilidad |
|---|---|
| `controller/AuthController.java` | Expone `POST /auth/login`. Es la única ruta pública. |
| `services/AuthService.java` | Autentica y genera el token. |
| `config/ApplicationConfig.java` | Define `UserDetailsService`, `AuthenticationProvider` (BCrypt) y `AuthenticationManager`. |
| `security/JwtService.java` | Firma, genera y valida el token. |
| `security/JwtAuthenticationFilter.java` | Lee el header `Authorization` y autentica la petición. |
| `config/SecurityConfig.java` | Regla global: `/auth/**` público, todo lo demás con token. |
| `entities/Usuario.java` | Implementa `UserDetails`; el rol se expone como `ROLE_<NOMBRE>`. |
| `repository/UsuarioRepository.java` | Busca por `correoUsuario`. |
| `resources/data.sql` | Siembra roles y el usuario administrador. |
| `resources/application.properties` | URL de la BD y secreto/expiración del JWT. |

### 2.3 Archivos clave del frontend

| Archivo | Responsabilidad |
|---|---|
| `src/components/auth/AuthProvider.jsx` | Estado de sesión: `user`, `role`, `login()`, `logout()`. |
| `src/lib/axiosConfig.js` | Axios con token y redirección en 401. **Solo lo usa AuthProvider.** |
| `src/lib/api.js` | Cliente `fetch` que usa casi toda la app. **No envía token.** |
| `src/components/auth/RequireRole.jsx` | Debería proteger rutas. Hoy no comprueba nada. |
| `src/components/auth/AccountMenu.jsx` | Muestra el rol y el acceso al panel. |
| `src/components/features/auth/LoginForm.jsx` | Formulario de login. |
| `src/components/layout/backoffice/BackOfficeProvider.jsx` | Carga los datos del panel según el rol. |
| `src/data/roles.js` | Traduce el rol del backend (`ADMINISTRADOR`) al id del frontend (`admin`). |

### 2.4 Cómo se protege cada endpoint

- `SecurityConfig` exige token para **todas** las rutas salvo `/auth/**`.
- Además, cada controlador tiene `@PreAuthorize` con el rol permitido:

| Prefijo | Rol exigido |
|---|---|
| `/admin/**` | `ADMINISTRADOR` |
| `/gerente/**` | `GERENTE_GENERAL` |
| `/almacenista/**` | `ALMACENISTA` |
| `/vendedor/**` | `VENDEDOR` |
| `/repartidor/**` | `REPARTIDOR` |
| `/cliente/**` | `CLIENTE` |
| `/auth/**` | público |

`hasRole('X')` internamente busca la autoridad `ROLE_X`. Por eso `Usuario.getAuthorities()`
devuelve `"ROLE_" + rol.getNombreRol()`.

---

## 3. Cómo levantar el proyecto

### 3.1 Requisitos

- **PostgreSQL 18** (en este equipo está en `D:\ANDREA\PostgreSQL`).
- **JDK 24 o superior** para el backend (hay JDK 21 y JDK 25 instalados).
- **Node.js 20+** y npm.

### 3.2 PostgreSQL

El servicio `postgresql-x64-18` estaba **detenido** y la última vez no se apagó de forma
limpia. Para levantarlo:

1. `Win + R` → `services.msc`.
2. Busca **PostgreSQL Server 18** → clic derecho → **Iniciar**.
3. Si pide permisos, ejecuta `services.msc` como administrador.
4. Si dice que "otro servidor puede estar en ejecución", reinicia la PC o termina los
   procesos `postgres.exe` desde el Administrador de tareas.

La configuración esperada está en `backend/src/main/resources/application.properties`:

```
base de datos : roker_bd
host          : localhost:5432
usuario       : postgres
contraseña    : 123456
```

Si la base no existe, créala con pgAdmin o:

```powershell
& 'D:\ANDREA\PostgreSQL\bin\createdb.exe' -U postgres -h localhost roker_bd
```

> Hibernate crea/actualiza las tablas solo (`ddl-auto=update`) y `data.sql` inserta
> los roles y el admin en cada arranque.

### 3.3 Backend

Problema actual: `pom.xml` declara `<java.version>24</java.version>`, pero el
`JAVA_HOME` de la máquina apunta a **JDK 21**. Maven falla con:

```
error: release version 24 not supported
```

Tienes dos opciones.

**Opción A — usar JDK 25 (sin tocar código, verificada):**

```powershell
cd backend
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.4"
.\mvnw.cmd spring-boot:run
```

**Opción B — bajar el proyecto a Java 21:**

Cambia en `backend/pom.xml`:

```xml
<java.version>21</java.version>
```

y luego:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

> También verifiqué que el código compila con `release 21`, así que la opción B es
> segura. El backend queda en `http://localhost:8080`.

### 3.4 Frontend

```powershell
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`.

El `vite.config.js` ya tiene un proxy: todo lo que empiece con `/api` se redirige a
`http://localhost:8080` y se le quita el prefijo. Ese proxy es la forma recomendada de
llamar al backend porque **evita problemas de CORS**.

### 3.5 Credenciales de prueba

`data.sql` crea:

| Correo | Contraseña | Rol |
|---|---|---|
| `admin@rokerperu.com` | `admin123` | ADMINISTRADOR |

> La contraseña fue verificada contra el hash BCrypt guardado en `data.sql`.

---

## 4. Qué quedó a medias

| # | Problema | Dónde | Efecto |
|---|---|---|---|
| 1 | El JWT no incluye el rol | `JwtService.generateToken()` + `AuthService` | `AuthProvider` asume `ADMINISTRADOR`; todos entran como admin |
| 2 | `api.js` no envía el token | `frontend/src/lib/api.js` | Catálogo, carrito y paneles reciben 401 |
| 3 | El catálogo exige rol CLIENTE | `ClienteProductoController`, `ClienteServicioController` | Un visitante anónimo no puede ver productos/servicios |
| 4 | `RequireRole` no protege nada | `frontend/src/components/auth/RequireRole.jsx` | Cualquiera abre `/admin` escribiendo la URL |
| 5 | `role` es string, pero se usa como objeto | `BackOfficeProvider.jsx`, `NotificationMenu.jsx` | `roleId` queda vacío y los paneles no cargan datos |
| 6 | Navegación de login incorrecta | `LoginForm.jsx` | Busca `r.code === user.role` cuando `user.role` ya es el id |
| 7 | Faltan roles | `data.sql` | `GERENTE_GENERAL`, `ALMACENISTA` y `PERSONAL` no existen |
| 8 | Registro no implementado | `RegisterForm.jsx` | `/auth/register` no existe; el botón está deshabilitado |
| 9 | Avisos de "pendiente" | `LoginForm.jsx`, `BackOfficeLayout.jsx` | La UI dice que el login no está integrado cuando ya lo está |
| 10 | CORS | `axiosConfig.js` + `SecurityConfig` | Axios llama directo a `:8080`; el preflight puede fallar |
| 11 | 401 sin manejo global | `api.js` | Al vencer el token no se cierra sesión automáticamente |

> Nota: `frontend/src/src.rar` parece un respaldo del código. No lo borres hasta
> confirmar con tu compañero qué contiene.

---

## 5. Plan para terminarlo

Los pasos están en orden de dependencia. Después de cada bloque, reinicia el backend
o el frontend si corresponde.

### Paso 0 — Confirmar que arranca

1. PostgreSQL iniciado.
2. Backend en `:8080`.
3. Frontend en `:5173`.
4. `POST /auth/login` responde con un token.

Si algo falla aquí, revisa la sección 7 antes de seguir.

### Paso 1 — Incluir el rol en el token

En `backend/src/main/java/RokerPeru/demo/services/AuthService.java`, reemplaza:

```java
String token = jwtService.generateToken(usuario);
```

por:

```java
Map<String, Object> claims = Map.of(
        "role", usuario.getRol().getNombreRol()
);
String token = jwtService.generateToken(claims, usuario);
```

y agrega `import java.util.Map;` arriba del archivo. El claim queda como
`"role": "ADMINISTRADOR"` (sin `ROLE_`), que es justo lo que `AuthProvider` espera
después de su `replace('ROLE_', '')`.

También puedes hacerlo dentro de `JwtService.generateToken(UserDetails)`:

```java
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
    return generateToken(claims, userDetails);
}
```

Así el claim queda `"role": "ROLE_ADMINISTRADOR"`.

**Verificación:** pega el token en <https://jwt.io> y comprueba que aparece `role`.

### Paso 2 — Un solo cliente HTTP que envíe el token

Hoy conviven dos clientes: `axiosConfig.js` (con token) y `api.js` (sin token).
Lo más simple es que `api.js` también envíe el token.

En `frontend/src/lib/api.js`, agrega la cabecera:

```js
function authHeaders(options) {
  const token = localStorage.getItem('token');
  return {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };
}

export async function request(path, options = {}) {
  const response = await fetch(base + path, {
    ...options,
    credentials: 'include',
    headers: authHeaders(options),
  });

  if (response.status === 401 && !path.startsWith('/auth/') && localStorage.getItem('token')) {
    localStorage.removeItem('token');
    window.location.href = '/login';
    throw new Error('Tu sesión expiró. Vuelve a iniciar sesión.');
  }

  // ...resto igual
}
```

Luego haz que `AuthProvider` use el mismo cliente:

1. En `frontend/src/lib/api.js`, si vas a usar `api()` para el registro, elimina la
   línea que bloquea `/auth/`:

   ```js
   if (path.startsWith('/auth/')) throw pending('Autenticación y perfil');
   ```

2. En `frontend/src/components/auth/AuthProvider.jsx`, cambia el import:

   ```js
   import { request } from '@/lib/api';
   ```

   y el login:

   ```js
   const { token } = await request('/auth/login', {
     method: 'POST',
     body: JSON.stringify({
       correoUsuario: email,
       passwordUsuario: password,
     }),
   });
   ```

3. Ajusta el `catch` de `login()` porque ahora el error no es de axios:

   ```js
   } catch (error) {
     if (error.status === 401 || error.status === 403) {
       throw new Error('Correo o contraseña incorrectos');
     }
     throw new Error(error.message || 'Error de conexión con el servidor');
   }
   ```

Con esto **todas** las peticiones usan el proxy `/api` y llevan el token. Ya no hace
falta CORS ni `@CrossOrigin` en los controladores (puedes dejarlos, no estorban).

> Alternativa mínima si no quieres tocar `AuthProvider`: en `axiosConfig.js` cambia
> `baseURL: 'http://localhost:8080'` por `baseURL: '/api'`. Eso elimina el CORS del
> login, pero el resto del frontend seguiría sin token.

### Paso 3 — Hacer público el catálogo

En `backend/src/main/java/RokerPeru/demo/config/SecurityConfig.java`:

```java
import org.springframework.http.HttpMethod;

.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // preflight
        .requestMatchers("/auth/**").permitAll()
        .requestMatchers(HttpMethod.GET,
                "/cliente/productos/**",
                "/cliente/servicios/**").permitAll()
        .anyRequest().authenticated()
)
```

Además, quita el `@PreAuthorize("hasRole('CLIENTE')")` de la clase y su import en:

- `controller/ClienteProductoController.java`
- `controller/ClienteServicioController.java`

Son solo lecturas públicas del catálogo; el rol CLIENTE se debe exigir para
**crear pedidos o solicitudes**, no para mirar productos.

### Paso 4 — Proteger las rutas de verdad

Reemplaza `frontend/src/components/auth/RequireRole.jsx` por:

```jsx
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthProvider';
import { ROLES } from '@/data/roles';

export default function RequireRole({ role }) {
  const { user, role: currentRole } = useAuth();
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (role && role !== currentRole) {
    const home = ROLES.find((r) => r.id === currentRole)?.home || '/';
    return <Navigate to={home} replace />;
  }

  return <Outlet />;
}
```

Con esto:

- Sin sesión → `/login`.
- Con sesión pero sin el rol → redirige a su propio panel.
- `<RequireRole />` sin `role` sigue sirviendo para rutas que solo piden estar logueado
  (por ejemplo `/perfil`).

### Paso 5 — Corregir el manejo del rol en el frontend

`AuthProvider` guarda `role` como **string** (`'admin'`, `'seller'`, etc.). Hay tres
lugares que todavía lo tratan como objeto:

**a) `frontend/src/components/layout/backoffice/BackOfficeProvider.jsx`**

```js
// antes
const roleId = role?.id || '', key = roleId + ':' + actorId;
// después
const roleId = role || '', key = roleId + ':' + actorId;
```

**b) `frontend/src/components/auth/NotificationMenu.jsx`**

```js
const messages =
  role === 'delivery' ? ...
  : role === 'staff' ? ...
  : role === 'seller' ? ...
  : ...
```

**c) `frontend/src/components/features/auth/LoginForm.jsx`**

```js
const home = ROLES.find((r) => r.id === user.role)?.home || '/';
navigate(fromValido ? from : home, { replace: true });
```

(`user.role` ya es `'admin'`, no `'ADMINISTRADOR'`).

### Paso 6 — Crear los roles y usuarios que faltan

En `backend/src/main/resources/data.sql`, antes del `setval` de `rol`:

```sql
INSERT INTO rol (id_rol, nombre_rol) VALUES (5, 'GERENTE_GENERAL') ON CONFLICT DO NOTHING;
INSERT INTO rol (id_rol, nombre_rol) VALUES (6, 'ALMACENISTA')     ON CONFLICT DO NOTHING;
INSERT INTO rol (id_rol, nombre_rol) VALUES (7, 'PERSONAL')        ON CONFLICT DO NOTHING;
```

> Importante: el `nombre_rol` debe coincidir **exactamente** con el usado en
> `hasRole(...)`, en mayúsculas y sin tildes.
>
> `PERSONAL` todavía no tiene controlador en el backend; se crea para que el usuario
> exista y el frontend pueda mostrarlo, pero su panel (`/staff`) aún no consulta nada.

Usuarios de prueba (la misma contraseña `admin123`, hash ya usado en el archivo):

```sql
INSERT INTO usuario (
    nombre_usuario, correo_usuario, tipopersona_usuario,
    numero_documento_usuario, telefono_usuario, direccion_usuario,
    password_usuario, id_rol
) VALUES (
    'Gerente General', 'gerente@rokerperu.com', 'NATURAL',
    '11111111111', '999999999', 'Sede Central',
    '$2a$10$ZytCKnIzFncajSLiyE.VQ.v8hvsLoJoKjqfiKe8nda.mJl4/bYdQK', 5
) ON CONFLICT (correo_usuario) DO NOTHING;
```

Repite el patrón para `ALMACENISTA` (rol 6), `VENDEDOR` (rol 2), `REPARTIDOR` (rol 3)
y `PERSONAL` (rol 7), cambiando correo y documento.

> También puedes crear usuarios desde el panel de admin (`/admin/trabajadores`), que
> ya encripta la contraseña con BCrypt en `UsuarioService`.

### Paso 7 — Limpiar los avisos de "pendiente"

- En `LoginForm.jsx`, elimina el bloque que empieza con
  `<div role="status" ...>El inicio de sesión está pendiente...` y los enlaces rápidos
  a los paneles.
- En `BackOfficeLayout.jsx`, elimina el `<div role="status" ...>Vista previa del panel...`.
- Opcional: implementar `/auth/register` o dejar el registro oculto. Hoy el botón está
  deshabilitado y el backend no tiene ese endpoint.

### Paso 8 — Probar el flujo completo

1. Sin token, `GET /cliente/productos` → **200**.
2. Sin token, `GET /admin/productos` → **401/403**.
3. Login como admin → token con `role`.
4. Con token de admin, `GET /admin/productos` → **200**.
5. Login como vendedor → no debe poder abrir `/admin` ni llamar `/admin/**`.
6. Refrescar la página (F5) → la sesión se mantiene.
7. Esperar/forzar un 401 → debe volver a `/login`.

---

## 6. Cómo probar la seguridad

### 6.1 Login con PowerShell

```powershell
$body = @{
  correoUsuario    = 'admin@rokerperu.com'
  passwordUsuario  = 'admin123'
} | ConvertTo-Json

$token = (Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/auth/login `
  -ContentType 'application/json' `
  -Body $body).token

$token
```

### 6.2 Llamar a un endpoint protegido

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/admin/productos `
  -Headers @{ Authorization = "Bearer $token" }
```

### 6.3 Comprobar que sin token falla

```powershell
Invoke-RestMethod -Uri http://localhost:8080/admin/productos
# Debe devolver 401 o 403
```

### 6.4 Leer el contenido del token

```powershell
$p = $token.Split('.')[1].Replace('-', '+').Replace('_', '/')
$p = $p.PadRight($p.Length + (4 - $p.Length % 4) % 4, '=')
[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($p))
```

Debe aparecer algo como:

```json
{ "sub": "admin@rokerperu.com", "role": "ADMINISTRADOR", "iat": 0, "exp": 0 }
```

### 6.5 Postman

La colección `pruebas_api/ROKER_PERU_Pruebas_API.postman_collection.json` **no tiene
un request de login**, así que después del commit de seguridad todas sus pruebas
fallarán con 401. Para usarla:

1. Crea un request `POST {{baseUrl}}/auth/login` con el body del punto 6.1.
2. En la pestaña **Tests** del request, guarda el token:

   ```js
   const json = pm.response.json();
   pm.collectionVariables.set('token', json.token);
   ```

3. En la colección, pestaña **Authorization**:
   - Type: **Bearer Token**
   - Token: `{{token}}`

---

## 7. Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| `release version 24 not supported` | `JAVA_HOME` en JDK 21 | Usar JDK 25 o cambiar `pom.xml` a 21 |
| `Connection refused` al arrancar el backend | PostgreSQL detenido | Iniciar el servicio |
| `database "roker_bd" does not exist` | Falta la base | Crearla con `createdb` o pgAdmin |
| `password authentication failed` | Credenciales distintas | Ajustar `application.properties` |
| El catálogo no carga (401) | `api.js` no envía token | Paso 2 |
| El catálogo no carga (403) | `@PreAuthorize` CLIENTE | Paso 3 |
| Todos entran como admin | Token sin claim `role` | Paso 1 |
| Paneles vacíos | `role?.id` sobre un string | Paso 5 |
| `/admin` abre sin login | `RequireRole` vacío | Paso 4 |
| Error de CORS en el navegador | Axios directo a `:8080` | Usar `/api` (proxy de Vite) |
| `403 Forbidden` con token válido | El rol de la BD no coincide con `hasRole` | Revisar `nombre_rol` |
| Login correcto pero redirige a `/` | `LoginForm` compara `code` con un id | Paso 5c |
| El token vence y no redirige | `api.js` no maneja 401 | Paso 2 |

---

## 8. Notas de seguridad

1. **El secreto JWT está en el repositorio.** En `application.properties`:

   ```properties
   jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
   ```

   Para producción, muévelo a una variable de entorno:

   ```properties
   jwt.secret=${JWT_SECRET}
   jwt.expiration=${JWT_EXPIRATION:86400000}
   ```

2. **La contraseña de PostgreSQL también está en el repositorio.** Igual: usar
   variables de entorno o un `application-local.properties` que no se suba.

3. **La contraseña `admin123` es solo para desarrollo.** Cámbiala antes de cualquier
   entrega o despliegue.

4. **`localStorage` es accesible desde JavaScript.** Si algún día hay XSS, el token se
   puede robar. Para un proyecto académico es aceptable; para producción se suele usar
   una cookie `HttpOnly`.

5. **`ddl-auto=update` + `sql.init.mode=always`** es cómodo en desarrollo, pero en
   producción se usan migraciones (Flyway/Liquibase) y `ddl-auto=validate`.

6. **No subas tokens ni logs con credenciales.** Revisa `logs/roker-peru.log` antes de
   hacer commit.

7. **CORS:** si en el futuro el frontend se despliega en otro dominio, configura CORS
   de forma centralizada (`CorsConfigurationSource`) en lugar de repetir
   `@CrossOrigin` en cada controlador.

8. **`@PreAuthorize` es la seguridad real.** Ocultar un botón en React no protege nada:
   el backend siempre debe validar el rol.

---

## 9. Mapa de archivos

```
APLICACION-WEB-ROKER-PERU-SA/
├── backend/
│   ├── pom.xml                        # Java 24, Spring Boot 4.1.1, JJWT 0.11.5
│   └── src/main/
│       ├── java/RokerPeru/demo/
│       │   ├── config/
│       │   │   ├── ApplicationConfig.java   # BCrypt + UserDetailsService
│       │   │   └── SecurityConfig.java      # reglas de acceso
│       │   ├── controller/
│       │   │   ├── AuthController.java      # POST /auth/login
│       │   │   └── ...                      # resto con @PreAuthorize
│       │   ├── dto/AuthDTO.java             # LoginRequest / Response(token)
│       │   ├── entities/Usuario.java        # implements UserDetails
│       │   ├── repository/UsuarioRepository.java
│       │   ├── security/
│       │   │   ├── JwtService.java
│       │   │   └── JwtAuthenticationFilter.java
│       │   └── services/AuthService.java
│       └── resources/
│           ├── application.properties       # BD + jwt.secret/expiration
│           └── data.sql                     # roles + admin
│
├── frontend/
│   ├── vite.config.js                 # proxy /api → :8080
│   └── src/
│       ├── App.jsx                    # AuthProvider envuelve la app
│       ├── routes/AppRouter.jsx       # rutas por rol
│       ├── lib/
│       │   ├── api.js                 # fetch (sin token todavía)
│       │   └── axiosConfig.js         # axios con token
│       ├── components/
│       │   ├── auth/
│       │   │   ├── AuthProvider.jsx
│       │   │   ├── RequireRole.jsx
│       │   │   ├── AccountMenu.jsx
│       │   │   └── NotificationMenu.jsx
│       │   ├── features/auth/LoginForm.jsx
│       │   └── layout/backoffice/BackOfficeProvider.jsx
│       └── data/roles.js              # code backend → id frontend
│
└── pruebas_api/
    └── ROKER_PERU_Pruebas_API.postman_collection.json
```

---

### Checklist rápido para retomar

- [ ] PostgreSQL iniciado y base `roker_bd` disponible.
- [ ] Backend arranca (`:8080`) con JDK correcto.
- [ ] Frontend arranca (`:5173`).
- [ ] Login con `admin@rokerperu.com` / `admin123` devuelve token.
- [ ] Token incluye `role` (Paso 1).
- [ ] `api.js` envía `Authorization` (Paso 2).
- [ ] Catálogo público sin token (Paso 3).
- [ ] `RequireRole` real (Paso 4).
- [ ] Rol como string corregido (Paso 5).
- [ ] Roles/usuario de prueba creados (Paso 6).
- [ ] Avisos de "pendiente" eliminados (Paso 7).
- [ ] Flujo completo probado (Paso 8).
