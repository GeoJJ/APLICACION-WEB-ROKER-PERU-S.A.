import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const root = path.dirname(fileURLToPath(import.meta.url));

function write(rel, content) {
  const full = path.join(root, rel);
  fs.mkdirSync(path.dirname(full), { recursive: true });
  fs.writeFileSync(full, content.endsWith("\n") ? content : content + "\n", "utf8");
}

function indent(text, spaces = 2) {
  const pad = " ".repeat(spaces);
  return text
    .trim()
    .split("\n")
    .map((line) => (line.length ? pad + line : ""))
    .join("\n");
}

function request(opts) {
  const {
    name,
    seq,
    method,
    url,
    token,
    body,
    pre,
    script,
    status,
    extra,
    docs,
    query,
  } = opts;

  const parts = [
    `meta {
  name: ${name}
  type: http
  seq: ${seq}
}`,
    `${method.toLowerCase()} {
  url: ${url}
  body: ${body ? "json" : "none"}
  auth: ${token ? "bearer" : "none"}
}`,
  ];

  if (token) {
    parts.push(`auth:bearer {
  token: {{${token}}}
}`);
  }

  if (query) {
    const lines = Object.entries(query).map(([key, value]) => `  ${key}: ${value}`);
    parts.push(`params:query {\n${lines.join("\n")}\n}`);
  }

  parts.push(`headers {
  Content-Type: application/json
}`);

  if (body) parts.push(`body:json {\n${indent(body)}\n}`);
  if (pre) parts.push(`script:pre-request {\n${indent(pre)}\n}`);
  if (script) parts.push(`script:post-response {\n${indent(script)}\n}`);

  const tests = [
    `test("código ${status}", function () {`,
    `  expect(res.getStatus()).to.equal(${status});`,
    `});`,
    ``,
    `test("responde en menos de 3 segundos", function () {`,
    `  expect(res.getResponseTime()).to.be.below(3000);`,
    `});`,
  ];
  if (extra) tests.push("", extra.trim());
  parts.push(`tests {\n${indent(tests.join("\n"))}\n}`);
  if (docs) parts.push(`docs {\n${indent(docs)}\n}`);
  parts.push(`settings {
  encodeUrl: true
}`);
  return parts.join("\n\n") + "\n";
}

function folder(name, seq, docs) {
  return `meta {
  name: ${name}
  seq: ${seq}
}

docs {
${indent(docs)}
}
`;
}

function save(field, variable) {
  return `const body = res.getBody();
bru.setVar("${variable}", body.${field});`;
}

function usuario({ nombre, correo, tipo, doc, tel, dir, rol, password = "{{passwordEquipo}}" }) {
  const pass = password
    ? `,
  "passwordUsuario": "${password}"`
    : "";
  return `{
  "nombreUsuario": "${nombre}",
  "correoUsuario": "${correo}",
  "tipoPersonaUsuario": "${tipo}",
  "numeroDocumentoUsuario": "${doc}",
  "telefonoUsuario": "${tel}",
  "direccionUsuario": "${dir}",
  "idRol": {{${rol}}}${pass}
}`;
}

function proveedor({ nombre, ruc, tel, correo }) {
  return `{
  "nombreProveedor": "${nombre}",
  "rucProveedor": "${ruc}",
  "telefonoProveedor": "${tel}",
  "correoProveedor": "${correo}"
}`;
}

function empleado({ nombre, apellido, tel }) {
  return `{
  "nombreEmpleadoServicio": "${nombre}",
  "apellidoEmpleadoServicio": "${apellido}",
  "telefonoEmpleadoServicio": "${tel}"
}`;
}

function servicio({ categoria, nombre, tipo, descripcion, precio, duracion, personal }) {
  return `{
  "categoriaServicio": "${categoria}",
  "nombreServicio": "${nombre}",
  "tipoServicio": "${tipo}",
  "descripcionServicio": "${descripcion}",
  "precioServicio": ${precio},
  "duracionEstimadaServicio": "${duracion}",
  "cantidadPersonalServicio": "${personal}"
}`;
}

function producto({ codigo, nombre, categoria, precio, stock, min, estado = "ACTIVO", prov }) {
  return `{
  "codigoProducto": "${codigo}",
  "nombreProducto": "${nombre}",
  "categoriaProducto": "${categoria}",
  "precioProducto": ${precio},
  "stockProducto": ${stock},
  "stockMinimo": ${min},
  "estadoProducto": "${estado}",
  "idProveedor": {{${prov}}}
}`;
}

const files = [];
function add(rel, content) {
  files.push(rel);
  write(rel, content);
}

add(
  "bruno.json",
  JSON.stringify(
    {
      version: "1",
      name: "ROKER PERU - Pruebas API",
      type: "collection",
      ignore: ["node_modules", ".git"],
    },
    null,
    2
  )
);

add(
  "collection.bru",
  `meta {
  name: ROKER PERU - Pruebas API
}

vars:pre-request {
  baseUrl: http://localhost:8080
  adminCorreo: admin@rokerperu.com
  adminPassword: admin123
  passwordEquipo: Bioseguridad2026
}

docs {
  Colección Bruno del API de ROKER PERU S.A.
  Ejecutar la colección completa, en orden, con el backend en http://localhost:8080.

  Antes de correrla, la base debe tener el seed de data.sql:
  roles ADMINISTRADOR, VENDEDOR, REPARTIDOR y CLIENTE, y el usuario admin@rokerperu.com con clave admin123.
  No debe tener productos, servicios, proveedores, empleados ni usuarios de una corrida anterior.

  El catálogo de prueba es de bioseguridad (EPP, desinfección e higiene) y de limpieza de oficinas.
  Los id se guardan desde las respuestas. Cada usuario creado entra con la clave Bioseguridad2026.
}
`
);

add(
  "environments/Local.bru",
  `vars {
  baseUrl: http://localhost:8080
  adminCorreo: admin@rokerperu.com
  adminPassword: admin123
  passwordEquipo: Bioseguridad2026
}
`
);

add(
  "01-auth/folder.bru",
  folder(
    "01. Auth",
    1,
    "El login es público. El resto de la API, salvo el catálogo de productos y servicios, exige Bearer token."
  )
);

add(
  "01-auth/01-login-admin.bru",
  request({
    name: "AUTH-01 Login del administrador sembrado",
    seq: 1,
    method: "POST",
    url: "{{baseUrl}}/auth/login",
    status: 200,
    body: `{
  "correoUsuario": "{{adminCorreo}}",
  "passwordUsuario": "{{adminPassword}}"
}`,
    script: `const body = res.getBody();
bru.setVar("tokenAdmin", body.token);`,
    extra: `test("devuelve un token", function () {
  expect(res.getBody().token).to.be.a("string").that.is.not.empty;
});`,
    docs: "Usuario creado por data.sql. Sin este token no se puede armar el resto de la data.",
  })
);

add(
  "01-auth/02-login-clave-incorrecta.bru",
  request({
    name: "AUTH-02 Login con clave incorrecta",
    seq: 2,
    method: "POST",
    url: "{{baseUrl}}/auth/login",
    status: 401,
    body: `{
  "correoUsuario": "{{adminCorreo}}",
  "passwordUsuario": "clave-que-no-es"
}`,
    docs: "Spring Security rechaza la autenticación antes de emitir un JWT.",
  })
);

add(
  "01-auth/03-admin-sin-token.bru",
  request({
    name: "AUTH-03 Consultar roles sin token",
    seq: 3,
    method: "GET",
    url: "{{baseUrl}}/admin/roles",
    status: 401,
    docs: "Cualquier ruta que no sea login ni el catálogo público debe responder 401 si no hay Bearer.",
  })
);

add(
  "02-roles/folder.bru",
  folder(
    "02. Administrador · Roles",
    2,
    "Los cuatro roles base vienen del seed. La colección crea ALMACENISTA y GERENTE_GENERAL, que la seguridad del backend también usa."
  )
);

add(
  "02-roles/01-listar.bru",
  request({
    name: "ROL-01 Listar roles y guardar los del seed",
    seq: 1,
    method: "GET",
    url: "{{baseUrl}}/admin/roles",
    token: "tokenAdmin",
    status: 200,
    script: `const roles = res.getBody();
const idDe = (nombre) => {
  const rol = roles.find((item) => item.nombreRol === nombre);
  if (!rol) {
    throw new Error("Falta el rol " + nombre + ". El seed de data.sql no está cargado.");
  }
  return rol.idRol;
};
bru.setVar("idRolAdmin", idDe("ADMINISTRADOR"));
bru.setVar("idRolVendedor", idDe("VENDEDOR"));
bru.setVar("idRolRepartidor", idDe("REPARTIDOR"));
bru.setVar("idRolCliente", idDe("CLIENTE"));`,
    extra: `test("están los cuatro roles del seed", function () {
  const nombres = res.getBody().map((rol) => rol.nombreRol);
  expect(nombres).to.include.members(["ADMINISTRADOR", "VENDEDOR", "REPARTIDOR", "CLIENTE"]);
});`,
  })
);

add(
  "02-roles/02-buscar.bru",
  request({
    name: "ROL-02 Buscar roles por texto",
    seq: 2,
    method: "GET",
    url: "{{baseUrl}}/admin/roles/buscar",
    token: "tokenAdmin",
    query: { texto: "VENDEDOR" },
    status: 200,
    extra: `test("encuentra al vendedor", function () {
  const nombres = res.getBody().map((rol) => rol.nombreRol);
  expect(nombres).to.include("VENDEDOR");
});`,
  })
);

add(
  "02-roles/03-crear-almacenista.bru",
  request({
    name: "ROL-03 Crear rol ALMACENISTA",
    seq: 3,
    method: "POST",
    url: "{{baseUrl}}/admin/roles",
    token: "tokenAdmin",
    status: 201,
    body: `{
  "nombreRol": "ALMACENISTA"
}`,
    script: save("idRol", "idRolAlmacenista"),
  })
);

add(
  "02-roles/04-crear-gerente.bru",
  request({
    name: "ROL-04 Crear rol GERENTE_GENERAL",
    seq: 4,
    method: "POST",
    url: "{{baseUrl}}/admin/roles",
    token: "tokenAdmin",
    status: 201,
    body: `{
  "nombreRol": "GERENTE_GENERAL"
}`,
    script: save("idRol", "idRolGerente"),
  })
);

add(
  "02-roles/05-crear-extra.bru",
  request({
    name: "ROL-05 Crear rol extra para editarlo",
    seq: 5,
    method: "POST",
    url: "{{baseUrl}}/admin/roles",
    token: "tokenAdmin",
    status: 201,
    body: `{
  "nombreRol": "AUDITOR_CALIDAD"
}`,
    script: save("idRol", "idRolExtra"),
  })
);

add(
  "02-roles/06-actualizar-extra.bru",
  request({
    name: "ROL-06 Renombrar el rol extra a SUPERVISOR_HIGIENE",
    seq: 6,
    method: "PUT",
    url: "{{baseUrl}}/admin/roles/{{idRolExtra}}",
    token: "tokenAdmin",
    status: 200,
    body: `{
  "nombreRol": "SUPERVISOR_HIGIENE"
}`,
    extra: `test("el nombre quedó actualizado", function () {
  expect(res.getBody().nombreRol).to.equal("SUPERVISOR_HIGIENE");
});`,
  })
);

add(
  "02-roles/07-eliminar-metodo-inexistente.bru",
  request({
    name: "ROL-07 Intentar eliminar un rol",
    seq: 7,
    method: "DELETE",
    url: "{{baseUrl}}/admin/roles/{{idRolExtra}}",
    token: "tokenAdmin",
    status: 405,
    docs: "AdminRolController no expone DELETE. El rol se conserva.",
  })
);

add(
  "03-usuarios/folder.bru",
  folder(
    "03. Administrador · Usuarios",
    3,
    "Personal de ROKER y dos clientes: una clínica que compra EPP y un centro empresarial que contrata limpieza de oficinas. La clave de todos es Bioseguridad2026."
  )
);

const usuarios = [
  ["01-vendedora", "USR-01 Crear vendedora Camila Ríos Quispe", "idUsuarioVendedora", {
    nombre: "Camila Ríos Quispe",
    correo: "vendedora@rokerperu.com",
    tipo: "NATURAL",
    doc: "73451289",
    tel: "954112230",
    dir: "Av. Ejército 120, Yanahuara, Arequipa",
    rol: "idRolVendedor",
  }],
  ["02-almacenista", "USR-02 Crear almacenista Héctor Mamani Choque", "idUsuarioAlmacenista", {
    nombre: "Héctor Mamani Choque",
    correo: "almacen@rokerperu.com",
    tipo: "NATURAL",
    doc: "45127890",
    tel: "984330112",
    dir: "Parque Industrial de Río Seco, lote 14, Arequipa",
    rol: "idRolAlmacenista",
  }],
  ["03-repartidor", "USR-03 Crear repartidor Diego Salazar Apaza", "idUsuarioRepartidor", {
    nombre: "Diego Salazar Apaza",
    correo: "reparto@rokerperu.com",
    tipo: "NATURAL",
    doc: "46781234",
    tel: "912445678",
    dir: "Urb. Hunter, calle Los Claveles 88, Arequipa",
    rol: "idRolRepartidor",
  }],
  ["04-gerente", "USR-04 Crear gerente Elena Paredes Núñez", "idUsuarioGerente", {
    nombre: "Elena Paredes Núñez",
    correo: "gerencia@rokerperu.com",
    tipo: "NATURAL",
    doc: "29876123",
    tel: "959001234",
    dir: "Calle San Francisco 310, Cercado, Arequipa",
    rol: "idRolGerente",
  }],
  ["05-clinica", "USR-05 Crear cliente Clínica Biosalud Arequipa", "idUsuarioClinica", {
    nombre: "Clínica Biosalud Arequipa S.A.C.",
    correo: "compras@biosalud.pe",
    tipo: "JURIDICA",
    doc: "20611845021",
    tel: "054381200",
    dir: "Av. Goyeneche 450, Cercado, Arequipa",
    rol: "idRolCliente",
  }],
  ["06-oficinas", "USR-06 Crear cliente Centro Empresarial Cayma", "idUsuarioOficinas", {
    nombre: "Centro Empresarial Cayma S.A.C.",
    correo: "administracion@cecayma.pe",
    tipo: "JURIDICA",
    doc: "20509876114",
    tel: "987650432",
    dir: "Av. Ejército 714, Cayma, Arequipa",
    rol: "idRolCliente",
  }],
];

usuarios.forEach((item, index) => {
  const [file, name, variable, data] = item;
  add(
    `03-usuarios/${file}.bru`,
    request({
      name,
      seq: index + 1,
      method: "POST",
      url: "{{baseUrl}}/admin/usuarios",
      token: "tokenAdmin",
      status: 201,
      body: usuario(data),
      script: save("idUsuario", variable),
      extra: `test("guarda el correo en minúsculas", function () {
  expect(res.getBody().correoUsuario).to.equal("${data.correo}");
});`,
    })
  );
});

add(
  "03-usuarios/07-listar.bru",
  request({
    name: "USR-07 Listar usuarios",
    seq: 7,
    method: "GET",
    url: "{{baseUrl}}/admin/usuarios",
    token: "tokenAdmin",
    status: 200,
    extra: `test("incluye a la clínica y al centro empresarial", function () {
  const nombres = res.getBody().map((usuario) => usuario.nombreUsuario);
  expect(nombres).to.include("Clínica Biosalud Arequipa S.A.C.");
  expect(nombres).to.include("Centro Empresarial Cayma S.A.C.");
});`,
  })
);

add(
  "03-usuarios/08-buscar-nombre.bru",
  request({
    name: "USR-08 Buscar por nombre",
    seq: 8,
    method: "GET",
    url: "{{baseUrl}}/admin/usuarios/buscar",
    token: "tokenAdmin",
    query: { texto: "Biosalud" },
    status: 200,
    extra: `test("encuentra a la clínica", function () {
  expect(res.getBody().length).to.be.at.least(1);
  expect(res.getBody()[0].correoUsuario).to.equal("compras@biosalud.pe");
});`,
  })
);

add(
  "03-usuarios/09-buscar-documento.bru",
  request({
    name: "USR-09 Buscar por documento",
    seq: 9,
    method: "GET",
    url: "{{baseUrl}}/admin/usuarios/buscar",
    token: "tokenAdmin",
    query: { texto: "73451289" },
    status: 200,
    extra: `test("encuentra a la vendedora", function () {
  expect(res.getBody()[0].nombreUsuario).to.equal("Camila Ríos Quispe");
});`,
  })
);

add(
  "03-usuarios/10-actualizar-oficinas.bru",
  request({
    name: "USR-10 Actualizar la dirección del centro empresarial",
    seq: 10,
    method: "PUT",
    url: "{{baseUrl}}/admin/usuarios/{{idUsuarioOficinas}}",
    token: "tokenAdmin",
    status: 200,
    body: usuario({
      nombre: "Centro Empresarial Cayma S.A.C.",
      correo: "administracion@cecayma.pe",
      tipo: "JURIDICA",
      doc: "20509876114",
      tel: "987650432",
      dir: "Av. Ejército 714, Cayma, oficina 502",
      rol: "idRolCliente",
      password: null,
    }),
    extra: `test("cambia la dirección y no pide clave nueva", function () {
  expect(res.getBody().direccionUsuario).to.equal("Av. Ejército 714, Cayma, oficina 502");
});`,
    docs: "Si la contraseña no viaja, el backend conserva la anterior.",
  })
);

add(
  "03-usuarios/11-crear-temporal.bru",
  request({
    name: "USR-11 Crear usuario temporal",
    seq: 11,
    method: "POST",
    url: "{{baseUrl}}/admin/usuarios",
    token: "tokenAdmin",
    status: 201,
    body: usuario({
      nombre: "Practicante Temporal",
      correo: "temporal@rokerperu.com",
      tipo: "NATURAL",
      doc: "70112233",
      tel: "900112233",
      dir: "Sede central ROKER, Arequipa",
      rol: "idRolCliente",
    }),
    script: save("idUsuario", "idUsuarioTemporal"),
  })
);

add(
  "03-usuarios/12-eliminar-temporal.bru",
  request({
    name: "USR-12 Eliminar el usuario temporal",
    seq: 12,
    method: "DELETE",
    url: "{{baseUrl}}/admin/usuarios/{{idUsuarioTemporal}}",
    token: "tokenAdmin",
    status: 204,
  })
);

const logins = [
  ["13-login-vendedora", "USR-13 Login vendedora", "vendedora@rokerperu.com", "tokenVendedor"],
  ["14-login-almacenista", "USR-14 Login almacenista", "almacen@rokerperu.com", "tokenAlmacenista"],
  ["15-login-clinica", "USR-15 Login Clínica Biosalud", "compras@biosalud.pe", "tokenClinica"],
  ["16-login-oficinas", "USR-16 Login Centro Empresarial Cayma", "administracion@cecayma.pe", "tokenOficinas"],
  ["17-login-repartidor", "USR-17 Login repartidor", "reparto@rokerperu.com", "tokenRepartidor"],
  ["18-login-gerente", "USR-18 Login gerente general", "gerencia@rokerperu.com", "tokenGerente"],
];

logins.forEach((item, index) => {
  const [file, name, correo, token] = item;
  add(
    `03-usuarios/${file}.bru`,
    request({
      name,
      seq: 13 + index,
      method: "POST",
      url: "{{baseUrl}}/auth/login",
      status: 200,
      body: `{
  "correoUsuario": "${correo}",
  "passwordUsuario": "{{passwordEquipo}}"
}`,
      script: `bru.setVar("${token}", res.getBody().token);`,
      extra: `test("emite token", function () {
  expect(res.getBody().token).to.be.a("string").that.is.not.empty;
});`,
    })
  );
});

add(
  "04-proveedores/folder.bru",
  folder(
    "04. Administrador · Proveedores",
    4,
    "Proveedores de EPP, químicos de desinfección e insumos de limpieza para oficinas."
  )
);

const proveedores = [
  ["01-andina", "PRV-01 Laboratorios Bioseguridad Andina", "idProveedorAndina", {
    nombre: "Laboratorios Bioseguridad Andina S.A.C.",
    ruc: "20450111234",
    tel: "054221100",
    correo: "ventas@bioseguridadandina.pe",
  }],
  ["02-quimicos", "PRV-02 Químicos Higiénicos del Sur", "idProveedorQuimicos", {
    nombre: "Químicos Higiénicos del Sur S.A.C.",
    ruc: "20450222345",
    tel: "054221188",
    correo: "pedidos@higienicosdelsur.pe",
  }],
  ["03-clean-office", "PRV-03 Importadora Clean Office Perú", "idProveedorClean", {
    nombre: "Importadora Clean Office Perú S.R.L.",
    ruc: "20550333456",
    tel: "987100200",
    correo: "hola@cleanoffice.pe",
  }],
  ["04-descartables", "PRV-04 Descartables Médicos del Valle", "idProveedorDescartables", {
    nombre: "Descartables Médicos del Valle S.A.C.",
    ruc: "20610444567",
    tel: "959220145",
    correo: "ventas@descartablesvalle.pe",
  }],
  ["05-epp", "PRV-05 EPP Industrial Arequipa", "idProveedorEpp", {
    nombre: "EPP Industrial Arequipa E.I.R.L.",
    ruc: "20410555678",
    tel: "054280900",
    correo: "contacto@epparequipa.pe",
  }],
];

proveedores.forEach((item, index) => {
  const [file, name, variable, data] = item;
  add(
    `04-proveedores/${file}.bru`,
    request({
      name,
      seq: index + 1,
      method: "POST",
      url: "{{baseUrl}}/admin/proveedores",
      token: "tokenAdmin",
      status: 201,
      body: proveedor(data),
      script: save("idProveedor", variable),
      extra: `test("normaliza el nombre a mayúsculas", function () {
  expect(res.getBody().nombreProveedor).to.equal(${JSON.stringify(data.nombre.toUpperCase())});
});`,
    })
  );
});

add(
  "04-proveedores/06-listar.bru",
  request({
    name: "PRV-06 Listar proveedores",
    seq: 6,
    method: "GET",
    url: "{{baseUrl}}/admin/proveedores",
    token: "tokenAdmin",
    status: 200,
    extra: `test("hay al menos cinco proveedores", function () {
  expect(res.getBody().length).to.be.at.least(5);
});`,
  })
);

add(
  "04-proveedores/07-buscar.bru",
  request({
    name: "PRV-07 Buscar por RUC",
    seq: 7,
    method: "GET",
    url: "{{baseUrl}}/admin/proveedores/buscar",
    token: "tokenAdmin",
    query: { texto: "20450111234" },
    status: 200,
    extra: `test("encuentra a Bioseguridad Andina", function () {
  expect(res.getBody()[0].rucProveedor).to.equal("20450111234");
});`,
  })
);

add(
  "04-proveedores/08-actualizar.bru",
  request({
    name: "PRV-08 Actualizar teléfono de EPP Industrial Arequipa",
    seq: 8,
    method: "PUT",
    url: "{{baseUrl}}/admin/proveedores/{{idProveedorEpp}}",
    token: "tokenAdmin",
    status: 200,
    body: proveedor({
      nombre: "EPP Industrial Arequipa E.I.R.L.",
      ruc: "20410555678",
      tel: "054280901",
      correo: "contacto@epparequipa.pe",
    }),
    extra: `test("el teléfono cambió", function () {
  expect(res.getBody().telefonoProveedor).to.equal("054280901");
});`,
  })
);

add(
  "04-proveedores/09-temporal.bru",
  request({
    name: "PRV-09 Crear proveedor temporal",
    seq: 9,
    method: "POST",
    url: "{{baseUrl}}/admin/proveedores",
    token: "tokenAdmin",
    status: 201,
    body: proveedor({
      nombre: "Insumos Temporales de Prueba S.A.C.",
      ruc: "20999000111",
      tel: "900000111",
      correo: "temporal@insumos-prueba.pe",
    }),
    script: save("idProveedor", "idProveedorTemporal"),
  })
);

add(
  "04-proveedores/10-eliminar-temporal.bru",
  request({
    name: "PRV-10 Eliminar el proveedor temporal",
    seq: 10,
    method: "DELETE",
    url: "{{baseUrl}}/admin/proveedores/{{idProveedorTemporal}}",
    token: "tokenAdmin",
    status: 204,
  })
);

add(
  "05-empleados/folder.bru",
  folder(
    "05. Administrador · Personal de limpieza",
    5,
    "Estos empleados ejecutan los servicios. No inician sesión: no son usuarios del sistema."
  )
);

const empleados = [
  ["01-pedro", "EMP-01 Pedro Ramos Flores", "idEmpleadoPedro", { nombre: "Pedro", apellido: "Ramos Flores", tel: "951100201" }],
  ["02-rosa", "EMP-02 Rosa Mamani Cutipa", "idEmpleadoRosa", { nombre: "Rosa", apellido: "Mamani Cutipa", tel: "951100202" }],
  ["03-miguel", "EMP-03 Miguel Torres Vega", "idEmpleadoMiguel", { nombre: "Miguel", apellido: "Torres Vega", tel: "951100203" }],
  ["04-ana", "EMP-04 Ana Lucía Paredes Núñez", "idEmpleadoAna", { nombre: "Ana Lucía", apellido: "Paredes Núñez", tel: "951100204" }],
  ["05-juan", "EMP-05 Juan Apaza Salas", "idEmpleadoJuan", { nombre: "Juan", apellido: "Apaza Salas", tel: "951100205" }],
];

empleados.forEach((item, index) => {
  const [file, name, variable, data] = item;
  add(
    `05-empleados/${file}.bru`,
    request({
      name,
      seq: index + 1,
      method: "POST",
      url: "{{baseUrl}}/admin/empleados-servicio",
      token: "tokenAdmin",
      status: 201,
      body: empleado(data),
      script: save("idEmpleadoServicio", variable),
    })
  );
});

add(
  "05-empleados/06-listar.bru",
  request({
    name: "EMP-06 Listar personal de limpieza",
    seq: 6,
    method: "GET",
    url: "{{baseUrl}}/admin/empleados-servicio",
    token: "tokenAdmin",
    status: 200,
    extra: `test("hay al menos cinco operarios", function () {
  expect(res.getBody().length).to.be.at.least(5);
});`,
  })
);

add(
  "05-empleados/07-buscar.bru",
  request({
    name: "EMP-07 Buscar a Pedro Ramos",
    seq: 7,
    method: "GET",
    url: "{{baseUrl}}/admin/empleados-servicio/buscar",
    token: "tokenAdmin",
    query: { texto: "RAMOS" },
    status: 200,
    extra: `test("lo encuentra", function () {
  expect(res.getBody()[0].apellidoEmpleadoServicio).to.include("RAMOS");
});`,
    docs: "El backend guarda nombre y apellido en mayúsculas.",
  })
);

add(
  "05-empleados/08-actualizar.bru",
  request({
    name: "EMP-08 Actualizar teléfono de Juan Apaza",
    seq: 8,
    method: "PUT",
    url: "{{baseUrl}}/admin/empleados-servicio/{{idEmpleadoJuan}}",
    token: "tokenAdmin",
    status: 200,
    body: empleado({ nombre: "Juan", apellido: "Apaza Salas", tel: "951100299" }),
    extra: `test("el teléfono cambió", function () {
  expect(res.getBody().telefonoEmpleadoServicio).to.equal("951100299");
});`,
  })
);

add(
  "05-empleados/09-temporal.bru",
  request({
    name: "EMP-09 Crear operario temporal",
    seq: 9,
    method: "POST",
    url: "{{baseUrl}}/admin/empleados-servicio",
    token: "tokenAdmin",
    status: 201,
    body: empleado({ nombre: "Luis", apellido: "Temporal Quispe", tel: "900100100" }),
    script: save("idEmpleadoServicio", "idEmpleadoTemporal"),
  })
);

add(
  "05-empleados/10-eliminar.bru",
  request({
    name: "EMP-10 Eliminar el operario temporal",
    seq: 10,
    method: "DELETE",
    url: "{{baseUrl}}/admin/empleados-servicio/{{idEmpleadoTemporal}}",
    token: "tokenAdmin",
    status: 204,
  })
);

add(
  "06-servicios/folder.bru",
  folder(
    "06. Administrador · Servicios de limpieza",
    6,
    "Servicios de oficina. La cantidad de personal es un texto numérico: el vendedor no puede confirmar la orden hasta asignar a esa cantidad de operarios."
  )
);

const servicios = [
  ["01-limpieza", "SRV-01 Limpieza integral de oficinas", "idServicioLimpieza", {
    categoria: "LIMPIEZA",
    nombre: "Limpieza integral de oficinas",
    tipo: "CONTRATO",
    descripcion: "Limpieza diaria de puestos, pisos, kitchenette y baños en horario de oficina.",
    precio: 980,
    duracion: "Lunes a viernes",
    personal: "2",
  }],
  ["02-desinfeccion", "SRV-02 Desinfección profunda de oficinas", "idServicioDesinfeccion", {
    categoria: "DESINFECCION",
    nombre: "Desinfección profunda de oficinas",
    tipo: "PUNTUAL",
    descripcion: "Nebulización y desinfección de superficies de alto contacto: escritorios, pomos y ascensores.",
    precio: 460,
    duracion: "4 horas",
    personal: "3",
  }],
  ["03-vidrios", "SRV-03 Limpieza de vidrios y mamparas", "idServicioVidrios", {
    categoria: "LIMPIEZA",
    nombre: "Limpieza de vidrios y mamparas",
    tipo: "PUNTUAL",
    descripcion: "Limpieza interior de mamparas, divisiones de vidrio y ventanas de oficina.",
    precio: 260,
    duracion: "3 horas",
    personal: "2",
  }],
  ["04-sanitizacion", "SRV-04 Sanitización de baños y kitchenette", "idServicioSanitizacion", {
    categoria: "HIGIENE",
    nombre: "Sanitización de baños y kitchenette",
    tipo: "PUNTUAL",
    descripcion: "Sanitización de baños, kitchenette y comedor de personal con desinfectante de superficies.",
    precio: 180,
    duracion: "2 horas",
    personal: "1",
  }],
  ["05-alfombras", "SRV-05 Limpieza de alfombras y tapices", "idServicioAlfombras", {
    categoria: "LIMPIEZA",
    nombre: "Limpieza de alfombras y tapices",
    tipo: "PUNTUAL",
    descripcion: "Aspirado y lavado de alfombras, tapices y sillas de sala de reuniones.",
    precio: 210,
    duracion: "Medio día",
    personal: "2",
  }],
];

servicios.forEach((item, index) => {
  const [file, name, variable, data] = item;
  add(
    `06-servicios/${file}.bru`,
    request({
      name,
      seq: index + 1,
      method: "POST",
      url: "{{baseUrl}}/admin/servicios",
      token: "tokenAdmin",
      status: 201,
      body: servicio(data),
      script: save("idServicio", variable),
      extra: `test("guarda el nombre del servicio", function () {
  expect(res.getBody().nombreServicio).to.equal(${JSON.stringify(data.nombre)});
});`,
    })
  );
});

add(
  "06-servicios/06-listar.bru",
  request({
    name: "SRV-06 Listar servicios",
    seq: 6,
    method: "GET",
    url: "{{baseUrl}}/admin/servicios",
    token: "tokenAdmin",
    status: 200,
    extra: `test("el catálogo tiene los servicios de oficina", function () {
  expect(res.getBody().length).to.be.at.least(5);
});`,
  })
);

add(
  "06-servicios/07-por-id.bru",
  request({
    name: "SRV-07 Ver la limpieza integral",
    seq: 7,
    method: "GET",
    url: "{{baseUrl}}/admin/servicios/{{idServicioLimpieza}}",
    token: "tokenAdmin",
    status: 200,
    extra: `test("pide dos operarios", function () {
  expect(res.getBody().cantidadPersonalServicio).to.equal("2");
  expect(res.getBody().precioServicio).to.equal(980);
});`,
  })
);

add(
  "06-servicios/08-actualizar.bru",
  request({
    name: "SRV-08 Actualizar la limpieza de alfombras",
    seq: 8,
    method: "PUT",
    url: "{{baseUrl}}/admin/servicios/{{idServicioAlfombras}}",
    token: "tokenAdmin",
    status: 200,
    body: servicio({
      categoria: "LIMPIEZA",
      nombre: "Limpieza de alfombras y tapices",
      tipo: "PUNTUAL",
      descripcion: "Aspirado, lavado y neutralizado de olor en alfombras de sala de directorio.",
      precio: 240,
      duracion: "Medio día",
      personal: "2",
    }),
    extra: `test("el precio subió a 240", function () {
  expect(res.getBody().precioServicio).to.equal(240);
});`,
  })
);

add(
  "06-servicios/09-temporal.bru",
  request({
    name: "SRV-09 Crear servicio temporal",
    seq: 9,
    method: "POST",
    url: "{{baseUrl}}/admin/servicios",
    token: "tokenAdmin",
    status: 201,
    body: servicio({
      categoria: "LIMPIEZA",
      nombre: "Recojo express de residuos de prueba",
      tipo: "PUNTUAL",
      descripcion: "Servicio temporal solo para probar la eliminación.",
      precio: 40,
      duracion: "1 hora",
      personal: "1",
    }),
    script: save("idServicio", "idServicioTemporal"),
  })
);

add(
  "06-servicios/10-eliminar.bru",
  request({
    name: "SRV-10 Eliminar el servicio temporal",
    seq: 10,
    method: "DELETE",
    url: "{{baseUrl}}/admin/servicios/{{idServicioTemporal}}",
    token: "tokenAdmin",
    status: 204,
  })
);

add(
  "07-productos/folder.bru",
  folder(
    "07. Administrador · Productos de bioseguridad",
    7,
    "EPP y desinfectantes. El traje nace bajo el stock mínimo para probar las alertas. El paño de muestra se inactiva y se elimina."
  )
);

const productos = [
  ["01-n95", "PRD-01 Mascarilla N95 caja x 20", "idProductoN95", {
    codigo: "MSK-N95-020",
    nombre: "Mascarilla N95 plegable caja x 20",
    categoria: "BIOSEGURIDAD",
    precio: 48.9,
    stock: 40,
    min: 12,
    prov: "idProveedorAndina",
  }],
  ["02-guantes", "PRD-02 Guantes de nitrilo caja x 100", "idProductoGuantes", {
    codigo: "GNT-NIT-100",
    nombre: "Guantes de nitrilo azul caja x 100",
    categoria: "EPP",
    precio: 64,
    stock: 25,
    min: 8,
    prov: "idProveedorDescartables",
  }],
  ["03-gel", "PRD-03 Gel antibacterial 500 ml", "idProductoGel", {
    codigo: "GEL-ANT-500",
    nombre: "Gel antibacterial 70% 500 ml",
    categoria: "HIGIENE",
    precio: 16.9,
    stock: 36,
    min: 10,
    prov: "idProveedorQuimicos",
  }],
  ["04-traje", "PRD-04 Traje desechable tipo 5", "idProductoTraje", {
    codigo: "TRJ-T5-01",
    nombre: "Traje desechable de bioseguridad tipo 5",
    categoria: "EPP",
    precio: 32.5,
    stock: 8,
    min: 10,
    prov: "idProveedorEpp",
  }],
  ["05-desinfectante", "PRD-05 Desinfectante de superficies 5 L", "idProductoDesinfectante", {
    codigo: "DES-SUP-5L",
    nombre: "Desinfectante de superficies cuaternario 5 L",
    categoria: "DESINFECCION",
    precio: 92,
    stock: 18,
    min: 6,
    prov: "idProveedorQuimicos",
  }],
  ["06-alcohol", "PRD-06 Alcohol isopropílico 70% galón", "idProductoAlcohol", {
    codigo: "ALC-ISO-1G",
    nombre: "Alcohol isopropílico 70% galón",
    categoria: "DESINFECCION",
    precio: 39.5,
    stock: 22,
    min: 8,
    prov: "idProveedorQuimicos",
  }],
  ["07-protector", "PRD-07 Protector facial transparente", "idProductoProtector", {
    codigo: "PRT-FAC-01",
    nombre: "Protector facial transparente",
    categoria: "EPP",
    precio: 14,
    stock: 30,
    min: 5,
    prov: "idProveedorDescartables",
  }],
];

productos.forEach((item, index) => {
  const [file, name, variable, data] = item;
  add(
    `07-productos/${file}.bru`,
    request({
      name,
      seq: index + 1,
      method: "POST",
      url: "{{baseUrl}}/admin/productos",
      token: "tokenAdmin",
      status: 201,
      body: producto(data),
      script: save("idProducto", variable),
      extra: `test("código y categoría quedan en mayúsculas", function () {
  expect(res.getBody().codigoProducto).to.equal("${data.codigo}");
  expect(res.getBody().categoriaProducto).to.equal("${data.categoria}");
  expect(res.getBody().stockProducto).to.equal(${data.stock});
});`,
    })
  );
});

add(
  "07-productos/08-listar.bru",
  request({
    name: "PRD-08 Listar productos",
    seq: 8,
    method: "GET",
    url: "{{baseUrl}}/admin/productos",
    token: "tokenAdmin",
    status: 200,
    extra: `test("el catálogo ya tiene los EPP", function () {
  expect(res.getBody().length).to.be.at.least(7);
});`,
  })
);

add(
  "07-productos/09-buscar.bru",
  request({
    name: "PRD-09 Buscar mascarillas N95",
    seq: 9,
    method: "GET",
    url: "{{baseUrl}}/admin/productos/buscar",
    token: "tokenAdmin",
    query: { texto: "N95" },
    status: 200,
    extra: `test("encuentra la caja de N95", function () {
  expect(res.getBody()[0].codigoProducto).to.equal("MSK-N95-020");
});`,
  })
);

add(
  "07-productos/10-por-id.bru",
  request({
    name: "PRD-10 Ver el traje desechable",
    seq: 10,
    method: "GET",
    url: "{{baseUrl}}/admin/productos/{{idProductoTraje}}",
    token: "tokenAdmin",
    status: 200,
    extra: `test("nace bajo el mínimo", function () {
  expect(res.getBody().stockProducto).to.equal(8);
  expect(res.getBody().stockMinimo).to.equal(10);
});`,
  })
);

const protector = {
  codigo: "PRT-FAC-01",
  nombre: "Protector facial transparente",
  categoria: "EPP",
  stock: 30,
  min: 5,
  prov: "idProveedorDescartables",
};

add(
  "07-productos/11-actualizar-precio.bru",
  request({
    name: "PRD-11 Subir el precio del protector facial",
    seq: 11,
    method: "PUT",
    url: "{{baseUrl}}/admin/productos/{{idProductoProtector}}",
    token: "tokenAdmin",
    status: 200,
    body: producto({ ...protector, precio: 15.5 }),
    extra: `test("el precio cambió y el stock no se toca por esta vía", function () {
  expect(res.getBody().precioProducto).to.equal(15.5);
  expect(res.getBody().stockProducto).to.equal(30);
});`,
    docs: "El PUT no modifica el stock. El stock solo se mueve por inventario o por un pedido.",
  })
);

add(
  "07-productos/12-restaurar-precio.bru",
  request({
    name: "PRD-12 Restaurar el precio del protector facial",
    seq: 12,
    method: "PUT",
    url: "{{baseUrl}}/admin/productos/{{idProductoProtector}}",
    token: "tokenAdmin",
    status: 200,
    body: producto({ ...protector, precio: 14 }),
    extra: `test("volvió a 14", function () {
  expect(res.getBody().precioProducto).to.equal(14);
});`,
  })
);

add(
  "07-productos/13-temporal.bru",
  request({
    name: "PRD-13 Crear paño de microfibra de muestra",
    seq: 13,
    method: "POST",
    url: "{{baseUrl}}/admin/productos",
    token: "tokenAdmin",
    status: 201,
    body: producto({
      codigo: "PAN-MIC-01",
      nombre: "Paño de microfibra para oficina, muestra",
      categoria: "LIMPIEZA",
      precio: 8.5,
      stock: 4,
      min: 2,
      prov: "idProveedorClean",
    }),
    script: save("idProducto", "idProductoTemporal"),
  })
);

add(
  "07-productos/14-inactivar.bru",
  request({
    name: "PRD-14 Inactivar el paño de muestra",
    seq: 14,
    method: "PATCH",
    url: "{{baseUrl}}/admin/productos/{{idProductoTemporal}}/estado",
    token: "tokenAdmin",
    query: { estado: "INACTIVO" },
    status: 200,
    extra: `test("queda inactivo", function () {
  expect(res.getBody().estadoProducto).to.equal("INACTIVO");
});`,
  })
);

add(
  "07-productos/15-eliminar.bru",
  request({
    name: "PRD-15 Eliminar el paño de muestra",
    seq: 15,
    method: "DELETE",
    url: "{{baseUrl}}/admin/productos/{{idProductoTemporal}}",
    token: "tokenAdmin",
    status: 204,
    docs: "Se puede borrar porque todavía no tiene movimientos ni pedidos.",
  })
);

add(
  "08-inventario-admin/folder.bru",
  folder(
    "08. Administrador · Inventario",
    8,
    "El admin repone y ajusta el alcohol isopropílico. El traje ya debería salir en alertas porque tiene 8 unidades y el mínimo es 10."
  )
);

add(
  "08-inventario-admin/01-stock.bru",
  request({
    name: "INV-01 Stock inicial del alcohol",
    seq: 1,
    method: "GET",
    url: "{{baseUrl}}/admin/inventario/stock/{{idProductoAlcohol}}",
    token: "tokenAdmin",
    status: 200,
    extra: `test("hay 22 galones", function () {
  expect(res.getBody()).to.equal(22);
});`,
  })
);

add(
  "08-inventario-admin/02-alertas.bru",
  request({
    name: "INV-02 Alertas de stock bajo",
    seq: 2,
    method: "GET",
    url: "{{baseUrl}}/admin/inventario/alertas",
    token: "tokenAdmin",
    status: 200,
    extra: `test("el traje está en advertencia", function () {
  const traje = res.getBody().find((item) => item.nombreProducto.includes("Traje"));
  expect(traje).to.not.equal(undefined);
  expect(traje.nivelUrgencia).to.include("ADVERTENCIA");
});`,
  })
);

add(
  "08-inventario-admin/03-movimientos.bru",
  request({
    name: "INV-03 Historial de movimientos",
    seq: 3,
    method: "GET",
    url: "{{baseUrl}}/admin/inventario/movimientos",
    token: "tokenAdmin",
    status: 200,
    extra: `test("responde una lista", function () {
  expect(res.getBody()).to.be.an("array");
});`,
  })
);

add(
  "08-inventario-admin/04-ingreso.bru",
  request({
    name: "INV-04 Ingreso de 5 galones de alcohol",
    seq: 4,
    method: "POST",
    url: "{{baseUrl}}/admin/inventario/ingreso",
    token: "tokenAdmin",
    status: 201,
    body: `{
  "productoId": {{idProductoAlcohol}},
  "cantidad": 5,
  "tipoMovimiento": "INGRESO",
  "justificacion": "Reposición para el laboratorio de la Clínica Biosalud"
}`,
    extra: `test("el stock queda en 27", function () {
  expect(res.getBody().tipoMovimiento).to.equal("INGRESO");
  expect(res.getBody().stockResultante).to.equal(27);
});`,
  })
);

add(
  "08-inventario-admin/05-ajuste.bru",
  request({
    name: "INV-05 Ajuste por un galón dañado",
    seq: 5,
    method: "POST",
    url: "{{baseUrl}}/admin/inventario/ajuste",
    token: "tokenAdmin",
    status: 201,
    body: `{
  "productoId": {{idProductoAlcohol}},
  "cantidad": 1,
  "tipoMovimiento": "AJUSTE",
  "justificacion": "Merma: galón con el sello roto en almacén"
}`,
    extra: `test("el stock queda en 26", function () {
  expect(res.getBody().tipoMovimiento).to.equal("AJUSTE");
  expect(res.getBody().stockResultante).to.equal(26);
});`,
  })
);

add(
  "08-inventario-admin/06-stock-final.bru",
  request({
    name: "INV-06 Consultar el stock del alcohol otra vez",
    seq: 6,
    method: "GET",
    url: "{{baseUrl}}/admin/inventario/stock/{{idProductoAlcohol}}",
    token: "tokenAdmin",
    status: 200,
    extra: `test("sigue en 26", function () {
  expect(res.getBody()).to.equal(26);
});`,
  })
);

add(
  "09-almacenista/folder.bru",
  folder(
    "09. Almacenista",
    9,
    "El almacenista consulta proveedores y productos, recibe desinfectante y deja el traje en cero para ver la alerta de agotado. Después lo repone."
  )
);

add(
  "09-almacenista/01-proveedores.bru",
  request({
    name: "ALM-01 Listar proveedores",
    seq: 1,
    method: "GET",
    url: "{{baseUrl}}/almacenista/proveedores",
    token: "tokenAlmacenista",
    status: 200,
    extra: `test("puede ver proveedores", function () {
  expect(res.getBody().length).to.be.at.least(5);
});`,
  })
);

add(
  "09-almacenista/02-buscar-proveedor.bru",
  request({
    name: "ALM-02 Buscar a Químicos Higiénicos del Sur",
    seq: 2,
    method: "GET",
    url: "{{baseUrl}}/almacenista/proveedores/buscar",
    token: "tokenAlmacenista",
    query: { texto: "HIGIENICOS" },
    status: 200,
    extra: `test("encuentra el RUC", function () {
  expect(res.getBody()[0].rucProveedor).to.equal("20450222345");
});`,
  })
);

add(
  "09-almacenista/03-productos.bru",
  request({
    name: "ALM-03 Listar productos",
    seq: 3,
    method: "GET",
    url: "{{baseUrl}}/almacenista/productos",
    token: "tokenAlmacenista",
    status: 200,
    extra: `test("ve el stock real", function () {
  const n95 = res.getBody().find((item) => item.codigoProducto === "MSK-N95-020");
  expect(n95.stockProducto).to.equal(40);
});`,
  })
);

add(
  "09-almacenista/04-buscar-producto.bru",
  request({
    name: "ALM-04 Buscar desinfectante",
    seq: 4,
    method: "GET",
    url: "{{baseUrl}}/almacenista/productos/buscar",
    token: "tokenAlmacenista",
    query: { texto: "cuaternario" },
    status: 200,
    extra: `test("encuentra el bidón de 5 L", function () {
  expect(res.getBody()[0].codigoProducto).to.equal("DES-SUP-5L");
});`,
  })
);

add(
  "09-almacenista/05-ver-producto.bru",
  request({
    name: "ALM-05 Ver el desinfectante por id",
    seq: 5,
    method: "GET",
    url: "{{baseUrl}}/almacenista/productos/{{idProductoDesinfectante}}",
    token: "tokenAlmacenista",
    status: 200,
    extra: `test("stock inicial 18", function () {
  expect(res.getBody().stockProducto).to.equal(18);
});`,
  })
);

add(
  "09-almacenista/06-ingreso.bru",
  request({
    name: "ALM-06 Ingreso de 10 bidones de desinfectante",
    seq: 6,
    method: "POST",
    url: "{{baseUrl}}/almacenista/inventario/ingreso",
    token: "tokenAlmacenista",
    status: 201,
    body: `{
  "productoId": {{idProductoDesinfectante}},
  "cantidad": 10,
  "tipoMovimiento": "INGRESO",
  "justificacion": "OC-2026-014 de Químicos Higiénicos del Sur, para servicios de oficina"
}`,
    extra: `test("el stock queda en 28", function () {
  expect(res.getBody().stockResultante).to.equal(28);
});`,
  })
);

add(
  "09-almacenista/07-stock.bru",
  request({
    name: "ALM-07 Consultar stock del desinfectante",
    seq: 7,
    method: "GET",
    url: "{{baseUrl}}/almacenista/inventario/stock/{{idProductoDesinfectante}}",
    token: "tokenAlmacenista",
    status: 200,
    extra: `test("confirma 28", function () {
  expect(res.getBody()).to.equal(28);
});`,
  })
);

add(
  "09-almacenista/08-agotar-traje.bru",
  request({
    name: "ALM-08 Ajuste que deja el traje en cero",
    seq: 8,
    method: "POST",
    url: "{{baseUrl}}/almacenista/inventario/ajuste",
    token: "tokenAlmacenista",
    status: 201,
    body: `{
  "productoId": {{idProductoTraje}},
  "cantidad": 8,
  "tipoMovimiento": "AJUSTE",
  "justificacion": "Salida a cuadrilla de desinfección; se repone en la siguiente petición"
}`,
    extra: `test("queda agotado", function () {
  expect(res.getBody().stockResultante).to.equal(0);
});`,
  })
);

add(
  "09-almacenista/09-alerta-agotado.bru",
  request({
    name: "ALM-09 Ver la alerta de agotado",
    seq: 9,
    method: "GET",
    url: "{{baseUrl}}/almacenista/inventario/alertas",
    token: "tokenAlmacenista",
    status: 200,
    extra: `test("el traje pide reposición inmediata", function () {
  const traje = res.getBody().find((item) => item.nombreProducto.includes("Traje"));
  expect(traje.stockActual).to.equal(0);
  expect(traje.nivelUrgencia).to.include("AGOTADO");
});`,
  })
);

add(
  "09-almacenista/10-reponer-traje.bru",
  request({
    name: "ALM-10 Reponer 8 trajes",
    seq: 10,
    method: "POST",
    url: "{{baseUrl}}/almacenista/inventario/ingreso",
    token: "tokenAlmacenista",
    status: 201,
    body: `{
  "productoId": {{idProductoTraje}},
  "cantidad": 8,
  "tipoMovimiento": "INGRESO",
  "justificacion": "Reposición urgente de EPP Industrial Arequipa"
}`,
    extra: `test("vuelve a 8", function () {
  expect(res.getBody().stockResultante).to.equal(8);
});`,
  })
);

add(
  "09-almacenista/11-movimientos.bru",
  request({
    name: "ALM-11 Historial de movimientos",
    seq: 11,
    method: "GET",
    url: "{{baseUrl}}/almacenista/inventario/movimientos",
    token: "tokenAlmacenista",
    status: 200,
    extra: `test("el movimiento más reciente es el ingreso del traje", function () {
  expect(res.getBody()[0].tipoMovimiento).to.equal("INGRESO");
  expect(res.getBody()[0].nombreProducto).to.include("Traje");
});`,
  })
);

add(
  "09-almacenista/12-alerta-advertencia.bru",
  request({
    name: "ALM-12 El traje vuelve a advertencia",
    seq: 12,
    method: "GET",
    url: "{{baseUrl}}/almacenista/inventario/alertas",
    token: "tokenAlmacenista",
    status: 200,
    extra: `test("8 unidades sigue bajo el mínimo de 10", function () {
  const traje = res.getBody().find((item) => item.nombreProducto.includes("Traje"));
  expect(traje.stockActual).to.equal(8);
  expect(traje.nivelUrgencia).to.include("ADVERTENCIA");
});`,
  })
);

add(
  "10-vendedor/folder.bru",
  folder(
    "10. Vendedor · Catálogo y venta en mostrador",
    10,
    "La vendedora consulta EPP, servicios y operarios, y registra una salida de guantes por venta en mostrador."
  )
);

add(
  "10-vendedor/01-productos.bru",
  request({
    name: "VEN-01 Listar productos",
    seq: 1,
    method: "GET",
    url: "{{baseUrl}}/vendedor/productos",
    token: "tokenVendedor",
    status: 200,
    extra: `test("ve precios y stock", function () {
  const gel = res.getBody().find((item) => item.codigoProducto === "GEL-ANT-500");
  expect(gel.precioProducto).to.equal(16.9);
  expect(gel.stockProducto).to.equal(36);
});`,
  })
);

add(
  "10-vendedor/02-buscar-producto.bru",
  request({
    name: "VEN-02 Buscar guantes de nitrilo",
    seq: 2,
    method: "GET",
    url: "{{baseUrl}}/vendedor/productos/buscar",
    token: "tokenVendedor",
    query: { texto: "nitrilo" },
    status: 200,
    extra: `test("encuentra la caja", function () {
  expect(res.getBody()[0].codigoProducto).to.equal("GNT-NIT-100");
});`,
  })
);

add(
  "10-vendedor/03-producto-id.bru",
  request({
    name: "VEN-03 Ver un producto",
    seq: 3,
    method: "GET",
    url: "{{baseUrl}}/vendedor/productos/{{idProductoGuantes}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("stock 25 antes de la venta de mostrador", function () {
  expect(res.getBody().stockProducto).to.equal(25);
});`,
  })
);

add(
  "10-vendedor/04-servicios.bru",
  request({
    name: "VEN-04 Listar servicios",
    seq: 4,
    method: "GET",
    url: "{{baseUrl}}/vendedor/servicios",
    token: "tokenVendedor",
    status: 200,
    extra: `test("está la sanitización", function () {
  const nombres = res.getBody().map((item) => item.nombreServicio);
  expect(nombres).to.include("Sanitización de baños y kitchenette");
});`,
  })
);

add(
  "10-vendedor/05-servicio-id.bru",
  request({
    name: "VEN-05 Ver la desinfección profunda",
    seq: 5,
    method: "GET",
    url: "{{baseUrl}}/vendedor/servicios/{{idServicioDesinfeccion}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("requiere 3 personas", function () {
  expect(res.getBody().cantidadPersonalServicio).to.equal("3");
});`,
  })
);

add(
  "10-vendedor/06-empleados.bru",
  request({
    name: "VEN-06 Listar operarios de limpieza",
    seq: 6,
    method: "GET",
    url: "{{baseUrl}}/vendedor/empleados-servicio",
    token: "tokenVendedor",
    status: 200,
    extra: `test("puede ver al personal", function () {
  expect(res.getBody().length).to.be.at.least(5);
});`,
  })
);

add(
  "10-vendedor/07-buscar-empleado.bru",
  request({
    name: "VEN-07 Buscar a Rosa Mamani",
    seq: 7,
    method: "GET",
    url: "{{baseUrl}}/vendedor/empleados-servicio/buscar",
    token: "tokenVendedor",
    query: { texto: "MAMANI" },
    status: 200,
    extra: `test("la encuentra", function () {
  expect(res.getBody()[0].apellidoEmpleadoServicio).to.include("MAMANI");
});`,
  })
);

add(
  "10-vendedor/08-repartidores.bru",
  request({
    name: "VEN-08 Listar repartidores disponibles",
    seq: 8,
    method: "GET",
    url: "{{baseUrl}}/vendedor/repartidores",
    token: "tokenVendedor",
    status: 200,
    extra: `test("Diego está disponible", function () {
  const nombres = res.getBody().map((item) => item.nombreUsuario);
  expect(nombres).to.include("Diego Salazar Apaza");
});`,
  })
);

add(
  "10-vendedor/09-salida.bru",
  request({
    name: "VEN-09 Venta en mostrador de 2 cajas de guantes",
    seq: 9,
    method: "POST",
    url: "{{baseUrl}}/vendedor/inventario/salida",
    token: "tokenVendedor",
    status: 201,
    body: `{
  "productoId": {{idProductoGuantes}},
  "cantidad": 2,
  "tipoMovimiento": "SALIDA",
  "justificacion": "Venta en mostrador a un consultorio odontológico de Cayma"
}`,
    extra: `test("quedan 23 cajas", function () {
  expect(res.getBody().tipoMovimiento).to.equal("SALIDA");
  expect(res.getBody().stockResultante).to.equal(23);
});`,
  })
);

add(
  "10-vendedor/10-stock.bru",
  request({
    name: "VEN-10 Consultar stock de guantes",
    seq: 10,
    method: "GET",
    url: "{{baseUrl}}/vendedor/inventario/stock/{{idProductoGuantes}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("confirma 23", function () {
  expect(res.getBody()).to.equal(23);
});`,
  })
);

add(
  "11-cliente/folder.bru",
  folder(
    "11. Cliente · Catálogo, pedidos y órdenes",
    11,
    "El catálogo de productos y servicios es público. Pedir y contratar exige el token del cliente. La clínica compra EPP. El centro empresarial contrata limpieza."
  )
);

add(
  "11-cliente/01-productos-publico.bru",
  request({
    name: "CLI-01 Catálogo público de productos",
    seq: 1,
    method: "GET",
    url: "{{baseUrl}}/cliente/productos",
    status: 200,
    extra: `test("no expone el stock exacto", function () {
  const n95 = res.getBody().find((item) => item.nombreProducto.includes("N95"));
  expect(n95.disponible).to.equal(true);
  expect(n95.stockProducto).to.equal(undefined);
  expect(n95.precioProducto).to.equal(48.9);
});`,
  })
);

add(
  "11-cliente/02-producto-publico.bru",
  request({
    name: "CLI-02 Ver un producto sin token",
    seq: 2,
    method: "GET",
    url: "{{baseUrl}}/cliente/productos/{{idProductoGel}}",
    status: 200,
    extra: `test("el gel está disponible", function () {
  expect(res.getBody().nombreProducto).to.include("Gel antibacterial");
  expect(res.getBody().disponible).to.equal(true);
});`,
  })
);

add(
  "11-cliente/03-servicios-publico.bru",
  request({
    name: "CLI-03 Catálogo público de servicios",
    seq: 3,
    method: "GET",
    url: "{{baseUrl}}/cliente/servicios",
    status: 200,
    extra: `test("publica la limpieza de oficinas", function () {
  const nombres = res.getBody().map((item) => item.nombreServicio);
  expect(nombres).to.include("Limpieza integral de oficinas");
});`,
  })
);

add(
  "11-cliente/04-buscar-servicios.bru",
  request({
    name: "CLI-04 Buscar servicios de oficina",
    seq: 4,
    method: "GET",
    url: "{{baseUrl}}/cliente/servicios/buscar",
    query: { texto: "oficina" },
    status: 200,
    extra: `test("devuelve limpieza y desinfección", function () {
  expect(res.getBody().length).to.be.at.least(2);
});`,
  })
);

add(
  "11-cliente/05-pedido-clinica.bru",
  request({
    name: "CLI-05 La clínica pide mascarillas y gel",
    seq: 5,
    method: "POST",
    url: "{{baseUrl}}/cliente/pedidos",
    token: "tokenClinica",
    status: 201,
    body: `{
  "metodoPagoPedido": "TRANSFERENCIA",
  "detalles": [
    { "idProducto": {{idProductoN95}}, "cantidadProducto": 2 },
    { "idProducto": {{idProductoGel}}, "cantidadProducto": 3 }
  ]
}`,
    script: save("idPedido", "idPedidoClinica"),
    extra: `test("queda pendiente y el total es 148.50", function () {
  expect(res.getBody().estadoPedido).to.equal("PENDIENTE");
  expect(res.getBody().montoTotalPedido).to.be.closeTo(148.5, 0.01);
  expect(res.getBody().metodoPagoPedido).to.equal("TRANSFERENCIA");
  expect(res.getBody().nombreVendedor).to.equal("Por asignar");
});`,
    docs: "2 cajas N95 a 48.90 más 3 geles a 16.90. El stock baja al crear el pedido, antes de que el vendedor lo confirme.",
  })
);

add(
  "11-cliente/06-mis-pedidos.bru",
  request({
    name: "CLI-06 La clínica lista sus pedidos",
    seq: 6,
    method: "GET",
    url: "{{baseUrl}}/cliente/pedidos",
    token: "tokenClinica",
    status: 200,
    extra: `test("ve el pedido que acaba de crear", function () {
  const ids = res.getBody().map((item) => item.idPedido);
  expect(ids).to.include(Number(bru.getVar("idPedidoClinica")));
});`,
  })
);

add(
  "11-cliente/07-pedido-protector.bru",
  request({
    name: "CLI-07 La clínica pide un protector facial",
    seq: 7,
    method: "POST",
    url: "{{baseUrl}}/cliente/pedidos",
    token: "tokenClinica",
    status: 201,
    body: `{
  "metodoPagoPedido": "YAPE",
  "detalles": [
    { "idProducto": {{idProductoProtector}}, "cantidadProducto": 1 }
  ]
}`,
    script: save("idPedido", "idPedidoProtector"),
    extra: `test("queda pendiente por 14 soles", function () {
  expect(res.getBody().estadoPedido).to.equal("PENDIENTE");
  expect(res.getBody().montoTotalPedido).to.equal(14);
});`,
    docs: "Este pedido se deja pendiente a propósito: más adelante se usa para fechas inválidas y para el comprobante que todavía no existe.",
  })
);

add(
  "11-cliente/08-orden-sanitizacion.bru",
  request({
    name: "CLI-08 Cayma solicita sanitización de baños",
    seq: 8,
    method: "POST",
    url: "{{baseUrl}}/cliente/ordenes-servicio",
    token: "tokenOficinas",
    status: 201,
    body: `{
  "idServicio": {{idServicioSanitizacion}},
  "fechaProgramadaServicio": "2026-12-02T09:00:00",
  "metodoPagoServicio": "TRANSFERENCIA",
  "incidenciaServicio": "Priorizar los baños del piso 3 y el kitchenette del piso 5."
}`,
    script: save("idOrdenServicio", "idOrdenSanitizacion"),
    extra: `test("queda pendiente por 180", function () {
  expect(res.getBody().estadoServicio).to.equal("PENDIENTE");
  expect(res.getBody().montoTotalServicio).to.equal(180);
  expect(res.getBody().nombreServicio).to.equal("Sanitización de baños y kitchenette");
});`,
  })
);

add(
  "11-cliente/09-orden-desinfeccion.bru",
  request({
    name: "CLI-09 Cayma solicita desinfección profunda",
    seq: 9,
    method: "POST",
    url: "{{baseUrl}}/cliente/ordenes-servicio",
    token: "tokenOficinas",
    status: 201,
    body: `{
  "idServicio": {{idServicioDesinfeccion}},
  "fechaProgramadaServicio": "2026-12-04T18:00:00",
  "metodoPagoServicio": "FACTURA",
  "incidenciaServicio": "Hacerlo después de las 18:00, cuando las oficinas estén vacías."
}`,
    script: save("idOrdenServicio", "idOrdenDesinfeccion"),
    extra: `test("el servicio cuesta 460 y pide confirmación", function () {
  expect(res.getBody().estadoServicio).to.equal("PENDIENTE");
  expect(res.getBody().montoTotalServicio).to.equal(460);
});`,
  })
);

add(
  "11-cliente/10-orden-vidrios.bru",
  request({
    name: "CLI-10 Cayma solicita limpieza de vidrios",
    seq: 10,
    method: "POST",
    url: "{{baseUrl}}/cliente/ordenes-servicio",
    token: "tokenOficinas",
    status: 201,
    body: `{
  "idServicio": {{idServicioVidrios}},
  "fechaProgramadaServicio": "2026-12-06T08:30:00",
  "metodoPagoServicio": "EFECTIVO",
  "incidenciaServicio": "Solo mamparas internas. La fachada se coordina aparte."
}`,
    script: save("idOrdenServicio", "idOrdenVidrios"),
    extra: `test("queda pendiente", function () {
  expect(res.getBody().estadoServicio).to.equal("PENDIENTE");
  expect(res.getBody().nombreServicio).to.equal("Limpieza de vidrios y mamparas");
});`,
  })
);

add(
  "11-cliente/11-mis-ordenes.bru",
  request({
    name: "CLI-11 Cayma lista sus órdenes",
    seq: 11,
    method: "GET",
    url: "{{baseUrl}}/cliente/ordenes-servicio",
    token: "tokenOficinas",
    status: 200,
    extra: `test("ve las tres solicitudes", function () {
  expect(res.getBody().length).to.be.at.least(3);
});`,
  })
);

add(
  "11-cliente/12-orden-propia.bru",
  request({
    name: "CLI-12 Cayma abre su orden de sanitización",
    seq: 12,
    method: "GET",
    url: "{{baseUrl}}/cliente/ordenes-servicio/{{idOrdenSanitizacion}}",
    token: "tokenOficinas",
    status: 200,
    extra: `test("sigue pendiente de personal", function () {
  expect(res.getBody().estadoServicio).to.equal("PENDIENTE");
  expect(res.getBody().nombreAuxiliar).to.equal("Por asignar");
});`,
  })
);

add(
  "11-cliente/13-orden-ajena.bru",
  request({
    name: "CLI-13 La clínica intenta ver una orden de Cayma",
    seq: 13,
    method: "GET",
    url: "{{baseUrl}}/cliente/ordenes-servicio/{{idOrdenSanitizacion}}",
    token: "tokenClinica",
    status: 409,
    docs: "Un cliente solo puede abrir sus propias órdenes.",
  })
);

add(
  "12-pedidos-vendedor/folder.bru",
  folder(
    "12. Vendedor · Pedidos",
    12,
    "Camila confirma el pedido de la clínica y se lo asigna a Diego. El protector se queda pendiente: primero con un usuario que no es repartidor y después con una fecha imposible."
  )
);

add(
  "12-pedidos-vendedor/01-pendientes.bru",
  request({
    name: "PED-01 Listar pedidos pendientes",
    seq: 1,
    method: "GET",
    url: "{{baseUrl}}/vendedor/pedidos",
    token: "tokenVendedor",
    status: 200,
    extra: `test("están los dos pedidos de la clínica", function () {
  const ids = res.getBody().map((item) => item.idPedido);
  expect(ids).to.include(Number(bru.getVar("idPedidoClinica")));
  expect(ids).to.include(Number(bru.getVar("idPedidoProtector")));
});`,
  })
);

add(
  "12-pedidos-vendedor/02-confirmar.bru",
  request({
    name: "PED-02 Confirmar el pedido y asignar a Diego",
    seq: 2,
    method: "PUT",
    url: "{{baseUrl}}/vendedor/pedidos/{{idPedidoClinica}}/confirmar",
    token: "tokenVendedor",
    status: 200,
    pre: `const salida = new Date(Date.now() + 60 * 60 * 1000);
const entrega = new Date(Date.now() + 5 * 60 * 60 * 1000);
const iso = (fecha) => fecha.toISOString().slice(0, 19);
bru.setVar("fechaSalida", iso(salida));
bru.setVar("fechaEntrega", iso(entrega));`,
    body: `{
  "idDelivery": {{idUsuarioRepartidor}},
  "fechaSalidaPedido": "{{fechaSalida}}",
  "fechaEntregaPedido": "{{fechaEntrega}}"
}`,
    extra: `test("pasa a EN_RUTA con Diego asignado", function () {
  expect(res.getBody().estadoPedido).to.equal("EN_RUTA");
  expect(res.getBody().nombreDelivery).to.equal("Diego Salazar Apaza");
  expect(res.getBody().nombreVendedor).to.equal("Camila Ríos Quispe");
  expect(res.getBody().numeroComprobantePedido).to.be.a("string").that.is.not.empty;
});`,
    docs: "Al confirmar se emite el comprobante. La fecha de entrega va después de la salida.",
  })
);

add(
  "12-pedidos-vendedor/03-mis-ventas.bru",
  request({
    name: "PED-03 Ver mis ventas",
    seq: 3,
    method: "GET",
    url: "{{baseUrl}}/vendedor/pedidos/mis-ventas",
    token: "tokenVendedor",
    status: 200,
    extra: `test("la venta de la clínica quedó a nombre de Camila", function () {
  const venta = res.getBody().find((item) => item.idPedido === Number(bru.getVar("idPedidoClinica")));
  expect(venta.estadoPedido).to.equal("EN_RUTA");
  expect(venta.nombreCliente).to.equal("Clínica Biosalud Arequipa S.A.C.");
});`,
  })
);

add(
  "12-pedidos-vendedor/04-repartidor-invalido.bru",
  request({
    name: "PED-04 Asignar el protector a alguien que no es repartidor",
    seq: 4,
    method: "PUT",
    url: "{{baseUrl}}/vendedor/pedidos/{{idPedidoProtector}}/confirmar",
    token: "tokenVendedor",
    status: 422,
    body: `{
  "idDelivery": {{idUsuarioVendedora}}
}`,
    docs: "Solo un usuario con rol REPARTIDOR puede recibir el pedido. La operación se revierte y el pedido sigue pendiente.",
  })
);

add(
  "12-pedidos-vendedor/05-fecha-invalida.bru",
  request({
    name: "PED-05 Entrega anterior a la salida",
    seq: 5,
    method: "PUT",
    url: "{{baseUrl}}/vendedor/pedidos/{{idPedidoProtector}}/confirmar",
    token: "tokenVendedor",
    status: 422,
    pre: `const salida = new Date(Date.now() + 5 * 60 * 60 * 1000);
const entrega = new Date(Date.now() + 60 * 60 * 1000);
const iso = (fecha) => fecha.toISOString().slice(0, 19);
bru.setVar("fechaSalidaMala", iso(salida));
bru.setVar("fechaEntregaMala", iso(entrega));`,
    body: `{
  "idDelivery": {{idUsuarioRepartidor}},
  "fechaSalidaPedido": "{{fechaSalidaMala}}",
  "fechaEntregaPedido": "{{fechaEntregaMala}}"
}`,
    docs: "La regla de negocio responde 422 y no confirma el pedido.",
  })
);

add(
  "12-pedidos-vendedor/06-stock-despues.bru",
  request({
    name: "PED-06 Stock después de los pedidos",
    seq: 6,
    method: "GET",
    url: "{{baseUrl}}/vendedor/inventario/stock/{{idProductoN95}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("las dos cajas de N95 ya salieron", function () {
  expect(res.getBody()).to.equal(38);
});`,
  })
);

add(
  "12-pedidos-vendedor/07-stock-gel.bru",
  request({
    name: "PED-07 Stock del gel",
    seq: 7,
    method: "GET",
    url: "{{baseUrl}}/vendedor/inventario/stock/{{idProductoGel}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("salieron 3 unidades", function () {
  expect(res.getBody()).to.equal(33);
});`,
  })
);

add(
  "12-pedidos-vendedor/08-stock-protector.bru",
  request({
    name: "PED-08 Stock del protector",
    seq: 8,
    method: "GET",
    url: "{{baseUrl}}/vendedor/inventario/stock/{{idProductoProtector}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("el pedido pendiente también descontó stock", function () {
  expect(res.getBody()).to.equal(29);
});`,
  })
);

add(
  "13-repartidor/folder.bru",
  folder(
    "13. Repartidor",
    13,
    "Diego solo ve pedidos en ruta asignados a él. Entrega el pedido de la clínica en la recepción del piso de hospitalización."
  )
);

add(
  "13-repartidor/01-listar.bru",
  request({
    name: "REP-01 Ver pedidos asignados",
    seq: 1,
    method: "GET",
    url: "{{baseUrl}}/repartidor/pedidos",
    token: "tokenRepartidor",
    status: 200,
    extra: `test("ve la dirección de la clínica y no el monto", function () {
  const pedido = res.getBody().find((item) => item.idPedido === Number(bru.getVar("idPedidoClinica")));
  expect(pedido.estadoPedido).to.equal("EN_RUTA");
  expect(pedido.nombreCliente).to.equal("Clínica Biosalud Arequipa S.A.C.");
  expect(pedido.direccionEntregaCliente).to.include("Goyeneche");
  expect(pedido.montoTotalPedido).to.equal(undefined);
});`,
  })
);

add(
  "13-repartidor/02-entregar.bru",
  request({
    name: "REP-02 Marcar el pedido como entregado",
    seq: 2,
    method: "PUT",
    url: "{{baseUrl}}/repartidor/pedidos/{{idPedidoClinica}}/estado",
    token: "tokenRepartidor",
    status: 200,
    body: `{
  "nuevoEstado": "ENTREGADO",
  "incidentePedido": "Recepción del piso 4 cerrada. Se dejó en enfermería con cargo firmado."
}`,
    extra: `test("figura como entregado", function () {
  expect(res.getBody().estadoPedido).to.equal("ENTREGADO");
  expect(res.getBody().incidentePedido).to.include("enfermería");
});`,
  })
);

add(
  "13-repartidor/03-pedido-ajeno.bru",
  request({
    name: "REP-03 Intentar actualizar el protector, que no es suyo",
    seq: 3,
    method: "PUT",
    url: "{{baseUrl}}/repartidor/pedidos/{{idPedidoProtector}}/estado",
    token: "tokenRepartidor",
    status: 409,
    body: `{
  "nuevoEstado": "ENTREGADO",
  "incidentePedido": "No debería poder entregar un pedido que no le asignaron."
}`,
  })
);

add(
  "14-ordenes/folder.bru",
  folder(
    "14. Vendedor · Órdenes y asignaciones",
    14,
    "La desinfección pide 3 operarios y se rechaza si no hay personal. La sanitización pide 1: se asigna a Pedro y se confirma. La limpieza de vidrios se cancela."
  )
);

add(
  "14-ordenes/01-listar.bru",
  request({
    name: "ORD-01 Listar órdenes",
    seq: 1,
    method: "GET",
    url: "{{baseUrl}}/vendedor/ordenes-servicio",
    token: "tokenVendedor",
    status: 200,
    extra: `test("la desinfección todavía no tiene personal", function () {
  const orden = res.getBody().find((item) => item.idOrdenServicio === Number(bru.getVar("idOrdenDesinfeccion")));
  expect(orden.estadoServicio).to.equal("PENDIENTE");
  expect(orden.cantidadPersonalRequerida).to.equal(3);
  expect(orden.cantidadPersonalAsignada).to.equal(0);
});`,
  })
);

add(
  "14-ordenes/02-ver.bru",
  request({
    name: "ORD-02 Ver la desinfección",
    seq: 2,
    method: "GET",
    url: "{{baseUrl}}/vendedor/ordenes-servicio/{{idOrdenDesinfeccion}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("el cliente es Cayma", function () {
  expect(res.getBody().nombreCliente).to.equal("Centro Empresarial Cayma S.A.C.");
});`,
  })
);

add(
  "14-ordenes/03-confirmar-sin-personal.bru",
  request({
    name: "ORD-03 Confirmar la desinfección sin operarios",
    seq: 3,
    method: "PUT",
    url: "{{baseUrl}}/vendedor/ordenes-servicio/{{idOrdenDesinfeccion}}/confirmar",
    token: "tokenVendedor",
    status: 422,
    docs: "El servicio pide 3 personas y hay 0 asignadas.",
  })
);

add(
  "14-ordenes/04-asignar.bru",
  request({
    name: "ORD-04 Asignar a Pedro a la sanitización",
    seq: 4,
    method: "POST",
    url: "{{baseUrl}}/vendedor/asignaciones",
    token: "tokenVendedor",
    status: 201,
    body: `{
  "idOrdenServicio": {{idOrdenSanitizacion}},
  "idEmpleado": {{idEmpleadoPedro}},
  "nombreResponsableAsignacion": "Camila Ríos Quispe",
  "descripcionAsignacion": "Sanitizar baños del piso 3 y kitchenette del piso 5 en el Centro Empresarial Cayma."
}`,
    script: save("idAsignacion", "idAsignacionPedro"),
    extra: `test("Pedro quedó asignado", function () {
  expect(res.getBody().nombreCompletoEmpleado).to.include("PEDRO");
  expect(res.getBody().nombreCompletoEmpleado).to.include("RAMOS");
});`,
  })
);

add(
  "14-ordenes/05-listar-asignaciones.bru",
  request({
    name: "ORD-05 Listar asignaciones",
    seq: 5,
    method: "GET",
    url: "{{baseUrl}}/vendedor/asignaciones",
    token: "tokenVendedor",
    status: 200,
    extra: `test("aparece la asignación de Pedro", function () {
  const ids = res.getBody().map((item) => item.idAsignacion);
  expect(ids).to.include(Number(bru.getVar("idAsignacionPedro")));
});`,
  })
);

add(
  "14-ordenes/06-por-orden.bru",
  request({
    name: "ORD-06 Asignaciones de la sanitización",
    seq: 6,
    method: "GET",
    url: "{{baseUrl}}/vendedor/asignaciones/orden/{{idOrdenSanitizacion}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("hay un operario", function () {
  expect(res.getBody()).to.have.length(1);
});`,
  })
);

add(
  "14-ordenes/07-por-empleado.bru",
  request({
    name: "ORD-07 Asignaciones de Pedro",
    seq: 7,
    method: "GET",
    url: "{{baseUrl}}/vendedor/asignaciones/empleado/{{idEmpleadoPedro}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("tiene la orden de Cayma", function () {
  expect(res.getBody()[0].idOrdenServicio).to.equal(Number(bru.getVar("idOrdenSanitizacion")));
});`,
  })
);

add(
  "14-ordenes/08-asignacion-id.bru",
  request({
    name: "ORD-08 Ver la asignación por id",
    seq: 8,
    method: "GET",
    url: "{{baseUrl}}/vendedor/asignaciones/{{idAsignacionPedro}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("la responsable es Camila", function () {
  expect(res.getBody().nombreResponsableAsignacion).to.equal("Camila Ríos Quispe");
});`,
  })
);

add(
  "14-ordenes/09-confirmar.bru",
  request({
    name: "ORD-09 Confirmar la sanitización",
    seq: 9,
    method: "PUT",
    url: "{{baseUrl}}/vendedor/ordenes-servicio/{{idOrdenSanitizacion}}/confirmar",
    token: "tokenVendedor",
    query: { incidencia: "Cliente pidió priorizar los baños del piso 3" },
    status: 200,
    extra: `test("queda confirmada y con comprobante", function () {
  expect(res.getBody().estadoServicio).to.equal("CONFIRMADO");
  expect(res.getBody().cantidadPersonalAsignada).to.equal(1);
  expect(res.getBody().numeroComprobanteServicio).to.be.a("string").that.is.not.empty;
  expect(res.getBody().nombreVendedor).to.equal("Camila Ríos Quispe");
});`,
  })
);

add(
  "14-ordenes/10-cancelar.bru",
  request({
    name: "ORD-10 Cancelar la limpieza de vidrios",
    seq: 10,
    method: "PUT",
    url: "{{baseUrl}}/vendedor/ordenes-servicio/{{idOrdenVidrios}}/cancelar",
    token: "tokenVendedor",
    query: { incidencia: "El cliente reprogramó la limpieza de mamparas para el próximo mes" },
    status: 200,
    extra: `test("queda cancelada", function () {
  expect(res.getBody().estadoServicio).to.equal("CANCELADO");
});`,
  })
);

add(
  "15-comprobantes/folder.bru",
  folder(
    "15. Comprobantes PDF",
    15,
    "El PDF existe recién cuando el vendedor confirma. Lo pueden descargar admin, gerente, vendedor y el cliente dueño. El repartidor no tiene permiso sobre esta ruta."
  )
);

add(
  "15-comprobantes/01-pdf-pedido.bru",
  request({
    name: "CMP-01 PDF del pedido entregado, como la clínica",
    seq: 1,
    method: "GET",
    url: "{{baseUrl}}/comprobantes/pedidos/{{idPedidoClinica}}",
    token: "tokenClinica",
    status: 200,
    extra: `test("responde un PDF", function () {
  const tipo = String(res.getHeader("content-type") || res.getHeader("Content-Type") || "");
  expect(tipo.toLowerCase()).to.include("pdf");
});`,
  })
);

add(
  "15-comprobantes/02-pdf-pedido-pendiente.bru",
  request({
    name: "CMP-02 PDF del protector, que sigue pendiente",
    seq: 2,
    method: "GET",
    url: "{{baseUrl}}/comprobantes/pedidos/{{idPedidoProtector}}",
    token: "tokenClinica",
    status: 409,
    docs: "El comprobante se emite al confirmar. Este pedido nunca se confirmó.",
  })
);

add(
  "15-comprobantes/03-pdf-orden.bru",
  request({
    name: "CMP-03 PDF de la sanitización, como vendedora",
    seq: 3,
    method: "GET",
    url: "{{baseUrl}}/comprobantes/ordenes-servicio/{{idOrdenSanitizacion}}",
    token: "tokenVendedor",
    status: 200,
    extra: `test("responde un PDF", function () {
  const tipo = String(res.getHeader("content-type") || res.getHeader("Content-Type") || "");
  expect(tipo.toLowerCase()).to.include("pdf");
});`,
  })
);

add(
  "15-comprobantes/04-pdf-orden-cancelada.bru",
  request({
    name: "CMP-04 PDF de la orden de vidrios cancelada",
    seq: 4,
    method: "GET",
    url: "{{baseUrl}}/comprobantes/ordenes-servicio/{{idOrdenVidrios}}",
    token: "tokenVendedor",
    status: 409,
    docs: "Cancelar no emite comprobante.",
  })
);

add(
  "15-comprobantes/05-pdf-gerente.bru",
  request({
    name: "CMP-05 La gerente descarga el mismo pedido",
    seq: 5,
    method: "GET",
    url: "{{baseUrl}}/comprobantes/pedidos/{{idPedidoClinica}}",
    token: "tokenGerente",
    status: 200,
    extra: `test("también es PDF", function () {
  const tipo = String(res.getHeader("content-type") || res.getHeader("Content-Type") || "");
  expect(tipo.toLowerCase()).to.include("pdf");
});`,
  })
);

add(
  "15-comprobantes/06-pdf-repartidor.bru",
  request({
    name: "CMP-06 El repartidor no puede descargar comprobantes",
    seq: 6,
    method: "GET",
    url: "{{baseUrl}}/comprobantes/pedidos/{{idPedidoClinica}}",
    token: "tokenRepartidor",
    status: 403,
  })
);

add(
  "16-gerente/folder.bru",
  folder(
    "16. Gerente general",
    16,
    "Elena revisa el historial de inventario y descarga el Excel de productos."
  )
);

add(
  "16-gerente/01-movimientos.bru",
  request({
    name: "GER-01 Historial de movimientos",
    seq: 1,
    method: "GET",
    url: "{{baseUrl}}/gerente/inventario/movimientos",
    token: "tokenGerente",
    status: 200,
    extra: `test("hay ingresos, ajustes y la salida de mostrador", function () {
  const tipos = res.getBody().map((item) => item.tipoMovimiento);
  expect(tipos).to.include.members(["INGRESO", "AJUSTE", "SALIDA"]);
});`,
  })
);

add(
  "16-gerente/02-excel.bru",
  request({
    name: "GER-02 Descargar el reporte de productos",
    seq: 2,
    method: "GET",
    url: "{{baseUrl}}/gerente/reportes/productos/excel",
    token: "tokenGerente",
    status: 200,
    extra: `test("responde un Excel", function () {
  const tipo = String(res.getHeader("content-type") || res.getHeader("Content-Type") || "");
  expect(tipo.toLowerCase()).to.include("spreadsheetml");
});`,
  })
);

add(
  "16-gerente/03-excel-prohibido.bru",
  request({
    name: "GER-03 El almacenista no descarga el Excel",
    seq: 3,
    method: "GET",
    url: "{{baseUrl}}/gerente/reportes/productos/excel",
    token: "tokenAlmacenista",
    status: 403,
  })
);

add(
  "17-errores/folder.bru",
  folder(
    "17. Validaciones y reglas",
    17,
    "Casos que no cambian la data válida: validación, duplicados, stock, rutas que no existen y un rol en una ruta que no le corresponde."
  )
);

add(
  "17-errores/01-rol-vacio.bru",
  request({
    name: "ERR-01 Rol con nombre vacío",
    seq: 1,
    method: "POST",
    url: "{{baseUrl}}/admin/roles",
    token: "tokenAdmin",
    status: 400,
    body: `{
  "nombreRol": ""
}`,
  })
);

add(
  "17-errores/02-precio-negativo.bru",
  request({
    name: "ERR-02 Producto con precio negativo",
    seq: 2,
    method: "POST",
    url: "{{baseUrl}}/admin/productos",
    token: "tokenAdmin",
    status: 400,
    body: producto({
      codigo: "ERR-PRECIO",
      nombre: "Producto con precio inválido",
      categoria: "EPP",
      precio: -5,
      stock: 1,
      min: 1,
      prov: "idProveedorAndina",
    }),
  })
);

add(
  "17-errores/03-dni-invalido.bru",
  request({
    name: "ERR-03 Persona natural con documento de 11 dígitos",
    seq: 3,
    method: "POST",
    url: "{{baseUrl}}/admin/usuarios",
    token: "tokenAdmin",
    status: 400,
    body: usuario({
      nombre: "Documento Mal Armado",
      correo: "dni.mal@rokerperu.com",
      tipo: "NATURAL",
      doc: "12345678901",
      tel: "900000000",
      dir: "Arequipa",
      rol: "idRolCliente",
    }),
    docs: "Una persona natural debe tener DNI de 8 dígitos. Una jurídica, RUC de 11.",
  })
);

add(
  "17-errores/04-ruta-inexistente.bru",
  request({
    name: "ERR-04 Ruta que no existe",
    seq: 4,
    method: "GET",
    url: "{{baseUrl}}/admin/ferreteria",
    token: "tokenAdmin",
    status: 404,
  })
);

add(
  "17-errores/05-producto-inexistente.bru",
  request({
    name: "ERR-05 Producto que no existe",
    seq: 5,
    method: "GET",
    url: "{{baseUrl}}/admin/productos/999999",
    token: "tokenAdmin",
    status: 404,
  })
);

add(
  "17-errores/06-codigo-duplicado.bru",
  request({
    name: "ERR-06 Código de producto duplicado",
    seq: 6,
    method: "POST",
    url: "{{baseUrl}}/admin/productos",
    token: "tokenAdmin",
    status: 409,
    body: producto({
      codigo: "MSK-N95-020",
      nombre: "Otra mascarilla con el mismo código",
      categoria: "BIOSEGURIDAD",
      precio: 10,
      stock: 1,
      min: 1,
      prov: "idProveedorAndina",
    }),
  })
);

add(
  "17-errores/07-ruc-duplicado.bru",
  request({
    name: "ERR-07 RUC de proveedor duplicado",
    seq: 7,
    method: "POST",
    url: "{{baseUrl}}/admin/proveedores",
    token: "tokenAdmin",
    status: 409,
    body: proveedor({
      nombre: "Otro laboratorio con el mismo RUC",
      ruc: "20450111234",
      tel: "054200000",
      correo: "otro@bioseguridadandina.pe",
    }),
  })
);

add(
  "17-errores/08-stock-insuficiente.bru",
  request({
    name: "ERR-08 Pedir más trajes de los que hay",
    seq: 8,
    method: "POST",
    url: "{{baseUrl}}/cliente/pedidos",
    token: "tokenClinica",
    status: 409,
    body: `{
  "metodoPagoPedido": "EFECTIVO",
  "detalles": [
    { "idProducto": {{idProductoTraje}}, "cantidadProducto": 500 }
  ]
}`,
    extra: `test("informa el stock disponible", function () {
  expect(res.getBody().stockDisponible).to.equal(8);
  expect(res.getBody().cantidadSolicitada).to.equal(500);
});`,
  })
);

add(
  "17-errores/09-ajuste-excesivo.bru",
  request({
    name: "ERR-09 Ajuste mayor que el stock",
    seq: 9,
    method: "POST",
    url: "{{baseUrl}}/admin/inventario/ajuste",
    token: "tokenAdmin",
    status: 500,
    body: `{
  "productoId": {{idProductoAlcohol}},
  "cantidad": 10000,
  "tipoMovimiento": "AJUSTE",
  "justificacion": "Ajuste imposible para probar el rechazo"
}`,
    docs: "InventarioService lanza una excepción genérica cuando el ajuste dejaría el stock negativo. Hoy eso responde 500 y no descuenta unidades.",
  })
);

add(
  "17-errores/10-rol-prohibido.bru",
  request({
    name: "ERR-10 La clínica entra a la ruta de admin",
    seq: 10,
    method: "GET",
    url: "{{baseUrl}}/admin/usuarios",
    token: "tokenClinica",
    status: 403,
  })
);

add(
  "17-errores/11-confirmar-pedido-ya-entregado.bru",
  request({
    name: "ERR-11 Volver a confirmar el pedido ya entregado",
    seq: 11,
    method: "PUT",
    url: "{{baseUrl}}/vendedor/pedidos/{{idPedidoClinica}}/confirmar",
    token: "tokenVendedor",
    status: 409,
    body: `{
  "idDelivery": {{idUsuarioRepartidor}}
}`,
    docs: "Solo se confirman pedidos en estado PENDIENTE.",
  })
);

add(
  "17-errores/12-asignacion-duplicada.bru",
  request({
    name: "ERR-12 Asignar otra vez a Pedro a la misma orden",
    seq: 12,
    method: "POST",
    url: "{{baseUrl}}/vendedor/asignaciones",
    token: "tokenVendedor",
    status: 409,
    body: `{
  "idOrdenServicio": {{idOrdenSanitizacion}},
  "idEmpleado": {{idEmpleadoPedro}},
  "nombreResponsableAsignacion": "Camila Ríos Quispe",
  "descripcionAsignacion": "Intento duplicado de la misma cuadrilla."
}`,
  })
);

console.log(files.length + " archivos");
