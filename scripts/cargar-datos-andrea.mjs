import fs from 'node:fs';
import path from 'node:path';
import vm from 'node:vm';
import assert from 'node:assert/strict';
import crypto from 'node:crypto';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const collection = JSON.parse(fs.readFileSync(path.join(root, 'prueba_datos/RokerPeru Datos Tomcat.postman_collection.json'), 'utf8'));
const base = (process.env.ROKER_API_URL || 'http://localhost:8080/api').replace(/\/$/, '');
const email = process.env.ROKER_ADMIN_EMAIL;
const password = process.env.ROKER_ADMIN_PASSWORD;
if (!email || !password) throw new Error('Define ROKER_ADMIN_EMAIL y ROKER_ADMIN_PASSWORD antes de cargar los datos.');
const local = path.join(root, '.local/carga-andrea');
fs.mkdirSync(local, { recursive: true });
const stateFile = path.join(local, 'estado.json');
const state = fs.existsSync(stateFile) ? JSON.parse(fs.readFileSync(stateFile, 'utf8')) : { base, email, variables: {}, completed: {} };
if (state.base !== base || state.email !== email) throw new Error('El control local pertenece a otro servidor o administrador. Usa su entorno original.');
if (state.pending) throw new Error(`Hay una operación sin confirmar: ${state.pending.name}. Verificarla en la base antes de reintentar; no se reenviará automáticamente.`);
const lockFile = path.join(local, 'carga.lock');
const lock = fs.openSync(lockFile, 'wx');
fs.writeFileSync(lock, String(process.pid));
const vars = new Map(collection.variable.map(v => [v.key, v.value]));
for (const [key, value] of Object.entries(state.variables)) vars.set(key, value);
vars.set('base_url', base);
vars.set('roker_admin_email', email);
vars.set('roker_admin_password', password);
const summary = { peticionesEnviadas: 0, maestrosReutilizados: 0, operacionesReanudadas: 0, verificaciones: 0, operaciones: [] };

function save() {
    state.variables = Object.fromEntries([...vars].filter(([key]) => /^(rol_|uid_|prov_|prod_|srv_|emp_|ped_|ord_)/.test(key)));
    const temporary = stateFile + '.tmp';
    fs.writeFileSync(temporary, JSON.stringify(state, null, 2));
    fs.renameSync(temporary, stateFile);
}
function render(text) {
    return text.replace(/\{\{([^}]+)\}\}/g, (_, key) => {
        if (!vars.has(key)) throw new Error(`Falta resolver la variable ${key}.`);
        return String(vars.get(key));
    });
}
function expect(value) {
    const chain = {};
    for (const key of ['to', 'be', 'at']) Object.defineProperty(chain, key, { get: () => chain });
    chain.include = expected => assert.ok(value.includes(expected), 'Valor fuera de los esperados');
    chain.eql = expected => assert.equal(JSON.stringify(value), JSON.stringify(expected));
    chain.a = type => assert.equal(typeof value, type);
    chain.match = regex => assert.ok(regex.test(value), 'Formato incorrecto');
    chain.least = expected => assert.ok(value >= expected, 'Cantidad inferior a la esperada');
    Object.defineProperty(chain, 'exist', { get: () => { assert.ok(value !== null && value !== undefined); return chain; } });
    Object.defineProperty(chain, 'true', { get: () => { assert.equal(value, true); return chain; } });
    return chain;
}
function scripts(item, kind, response) {
    const pm = {
        collectionVariables: { get: key => vars.get(key), set: (key, value) => vars.set(key, value) },
        test: (name, callback) => { try { callback(); summary.verificaciones++; } catch (e) { throw new Error(`${item.name}: ${name}: ${e.message}`); } },
        expect,
        response: { code: response?.code, json: () => response?.data, text: () => JSON.stringify(response?.data) },
        request: { body: { raw: '' } },
        execution: { setNextRequest: () => { throw new Error('La colección interrumpió la ejecución.'); } }
    };
    for (const event of item.event || []) {
        if (event.listen !== kind) continue;
        // Solo se expone el subconjunto de Postman utilizado por esta colección; no se expone Node ni el sistema de archivos.
        vm.runInNewContext(`(function(){\n${event.script.exec.join('\n')}\n})()`, { pm, console: { log() {}, error() {} } }, { timeout: 3000 });
    }
}
async function request(method, url, headers = {}, body) {
    const result = await fetch(url, { method, headers, body, signal: AbortSignal.timeout(30000) });
    summary.peticionesEnviadas++;
    const text = await result.text();
    let data;
    try { data = text ? JSON.parse(text) : null; } catch { data = null; }
    return { code: result.status, data };
}
const normalize = value => String(value ?? '').normalize('NFD').replace(/\p{Diacritic}/gu, '').trim().toLowerCase();
const master = {
    '/admin/roles': ['nombreRol'],
    '/admin/usuarios': ['correoUsuario'],
    '/auth/register': ['correoUsuario'],
    '/admin/proveedores': ['rucProveedor', 'nombreProveedor'],
    '/admin/productos': ['nombreProducto'],
    '/admin/servicios': ['nombreServicio'],
    '/admin/empleados-servicio': ['nombreEmpleadoServicio', 'apellidoEmpleadoServicio']
};
async function existing(endpoint, body) {
    const fields = master[endpoint];
    if (!fields) return null;
    const source = endpoint === '/auth/register' ? '/admin/usuarios' : endpoint;
    const result = await request('GET', base + source, { Authorization: `Bearer ${vars.get('token_admin')}` });
    if (result.code !== 200 || !Array.isArray(result.data)) throw new Error(`No se pudo consultar ${source} antes de crear registros.`);
    return result.data.find(row => endpoint === '/admin/proveedores'
        ? fields.some(field => normalize(row[field]) === normalize(body[field]))
        : fields.every(field => normalize(row[field]) === normalize(body[field]))) || null;
}
function captureMasterVariables(item, data) {
    const code = (item.event || []).filter(e => e.listen === 'test').flatMap(e => e.script.exec).join('\n');
    for (const match of code.matchAll(/pm\.collectionVariables\.set\('([^']+)',\s*(?:d|pm\.response\.json\(\))\.(\w+)\)/g)) {
        if (data[match[2]] === undefined) throw new Error(`No se encontró ${match[2]} al reutilizar ${item.name}.`);
        vars.set(match[1], data[match[2]]);
    }
}
try {
    for (const group of collection.item) {
        console.log(`\n${group.name}`);
        for (const item of group.item) {
            scripts(item, 'prerequest');
            const config = item.request;
            const url = render(config.url.raw);
            const endpoint = url.slice(base.length).split('?')[0];
            const body = config.body?.raw ? render(config.body.raw) : undefined;
            const headers = Object.fromEntries((config.header || []).filter(h => !h.disabled).map(h => [h.key, render(h.value)]));
            const isLogin = endpoint === '/auth/login';
            const isMaster = config.method === 'POST' && Boolean(master[endpoint]);
            if (isMaster) {
                const record = await existing(endpoint, JSON.parse(body));
                if (record) {
                    captureMasterVariables(item, record);
                    summary.maestrosReutilizados++;
                    save();
                    console.log(`REUTILIZADO ${item.name}`);
                    continue;
                }
            }
            const mutation = !['GET', 'HEAD'].includes(config.method) && !isLogin;
            const key = crypto.createHash('sha256').update(config.method + config.url.raw + (config.body?.raw || '')).digest('hex');
            let response;
            if (mutation && state.completed[key]) {
                response = state.completed[key];
                summary.operacionesReanudadas++;
                console.log(`YA CARGADO ${item.name}`);
            } else {
                if (mutation) { state.pending = { key, name: item.name }; save(); }
                response = await request(config.method, url, headers, body);
                if (response.code < 200 || response.code >= 300) {
                    // Los rechazos de validación o permisos no se reintentan ni se consideran cargas realizadas.
                    if (response.code < 500) { delete state.pending; save(); }
                    throw new Error(`${item.name}: HTTP ${response.code}: ${response.data?.detail || response.data?.message || 'La API rechazó la operación'}`);
                }
                if (mutation) {
                    state.completed[key] = response;
                    delete state.pending;
                    captureMasterVariables(item, response.data || {});
                    save();
                }
                console.log(`HTTP ${response.code} ${item.name}`);
            }
            scripts(item, 'test', response);
            save();
            summary.operaciones.push({ nombre: item.name, estado: response.code });
        }
    }
    fs.writeFileSync(path.join(local, 'resultado.json'), JSON.stringify(summary, null, 2));
    console.log('\nCARGA COMPLETA ' + JSON.stringify({ ...summary, operaciones: undefined }));
} finally {
    fs.closeSync(lock);
    fs.unlinkSync(lockFile);
}
