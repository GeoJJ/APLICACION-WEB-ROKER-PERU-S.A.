import { jwtDecode } from 'jwt-decode';

export const ROLES = [
    { id: 'admin',     code: 'ADMINISTRADOR',   label: 'Administrador',        home: '/admin',     color: 'bg-indigo-600' },
    { id: 'manager',   code: 'GERENTE_GENERAL', label: 'Gerente general',      home: '/manager',   color: 'bg-blue-600' },
    { id: 'warehouse', code: 'ALMACENISTA',     label: 'Almacenista',          home: '/warehouse', color: 'bg-green-600' },
    { id: 'seller',    code: 'VENDEDOR',        label: 'Vendedor',             home: '/seller',    color: 'bg-purple-600' },
    { id: 'delivery',  code: 'REPARTIDOR',      label: 'Repartidor',           home: '/delivery',  color: 'bg-orange-600' },
    { id: 'staff',     code: 'PERSONAL',        label: 'Personal de servicio', home: '/staff',     color: 'bg-teal-600' },
    { id: 'customer',  code: 'CLIENTE',         label: 'Cliente',              home: '/productos', color: 'bg-slate-600' },
];
export const roleByCode = code => ROLES.find(r => r.code === code);
export const roleById = id => ROLES.find(r => r.id === id);
export const homeFor = id => roleById(id)?.home || '/';

const TOKEN_KEY = 'token';
export const SESSION_EXPIRED = 'auth:expired';

export const tokenStore = {
    get() { try { return localStorage.getItem(TOKEN_KEY); } catch { return null; } },
    set(token) { try { localStorage.setItem(TOKEN_KEY, token); } catch { /* sin almacenamiento */ } },
    clear() { try { localStorage.removeItem(TOKEN_KEY); } catch { /* sin almacenamiento */ } },
};

export function sessionFromToken(token) {
    try {
        const claims = jwtDecode(token);
        if (claims.exp && claims.exp * 1000 <= Date.now()) return null;

        const raw = claims.role ?? claims.rol ?? claims.nombreRol ?? claims.roles ?? claims.authorities;
        const first = Array.isArray(raw) ? raw[0] : raw;
        const name = first && typeof first === 'object' ? first.authority ?? first.nombreRol ?? first.name : first;
        const code = String(name ?? '').replace(/^ROLE_/, '').toUpperCase();
        const role = roleByCode(code)?.id;
        if (!role) {
            console.warn('[auth] El token no trae un rol reconocido. Claims recibidos:', Object.keys(claims));
            return null;
        }

        const email = String(claims.sub ?? claims.correoUsuario ?? '');
        const nombre = claims.nombre ?? claims.nombreUsuario ?? claims.name ?? email.split('@')[0];
        return { role, email, nombre, name: nombre };
    } catch {
        return null;
    }
}