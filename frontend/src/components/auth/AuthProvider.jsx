import { useEffect, useState } from 'react';
import { api } from '@/lib/api';
import { tokenStore, sessionFromToken, SESSION_EXPIRED } from '@/lib/auth';
import { AuthContext } from '@/hooks/useAuth';

function storedSession() {
    const token = tokenStore.get();
    if (!token) return null;
    const session = sessionFromToken(token);
    if (!session) tokenStore.clear();
    return session;
}

export default function AuthProvider({ children }) {
    const [session, setSession] = useState(storedSession);

    useEffect(() => {
        const onExpired = () => setSession(null);
        window.addEventListener(SESSION_EXPIRED, onExpired);
        return () => window.removeEventListener(SESSION_EXPIRED, onExpired);
    }, []);

    async function login(email, password) {
        let data;
        try {
            data = await api('/auth/login', { method: 'POST', body: JSON.stringify({ correoUsuario: email.trim(), passwordUsuario: password }) });
        } catch (e) {
            if ([400, 401, 403].includes(e.status)) throw new Error('Correo o contraseña incorrectos', { cause: e });
            throw e;
        }
        const next = data?.token && sessionFromToken(data.token);
        if (!next) throw new Error('El servidor no envió el rol de tu cuenta en el token. Revisa AuthService.login en el backend.');
        tokenStore.set(data.token);
        setSession(next);
        return next;
    }

    async function register(form) {
        await api('/auth/register', { method: 'POST', body: JSON.stringify(form) });
    }

    function logout() {
        const token = tokenStore.get();
        if (token) {
            api('/auth/logout', {
                method: 'POST',
                headers: { Authorization: `Bearer ${token}` },
            }).catch(() => { });
        }
        tokenStore.clear();
        setSession(null);
    }

    return (
        <AuthContext.Provider value={{ user: session, role: session?.role ?? null, login, register, logout }}>
            {children}
        </AuthContext.Provider>
    );
}