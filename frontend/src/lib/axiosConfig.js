import axios from 'axios';
import { tokenStore, SESSION_EXPIRED } from './auth';

// 1. Instancia principal de Axios.
//    VITE_API_URL=/api  → el proxy de Vite quita el "/api" y reenvía al backend (localhost:8080).
const api = axios.create({
    baseURL: (import.meta.env.VITE_API_URL || '/api').replace(/\/$/, ''),
    headers: { 'Content-Type': 'application/json' },
});

// 2. INTERCEPTOR DE PETICIÓN: antes de salir, pega el token en la cabecera Authorization.
//    Login y registro van sin token (son públicos).
api.interceptors.request.use((config) => {
    const token = tokenStore.get();
    if (token && !/^\/auth\/(login|register)/.test(config.url)) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 3. INTERCEPTOR DE RESPUESTA: si el servidor rechaza el token, se cierra la sesión.
//    Spring responde 401, o 403 SIN cuerpo, cuando el token venció o está en lista negra.
//    Un 403 por rol incorrecto SÍ trae cuerpo ("detail") y NO cierra la sesión.
//    No recarga la página: avisa a AuthProvider, que limpia la sesión y RequireRole redirige a /login.
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const { status, data } = error.response ?? {};
        const sessionLost = status === 401 || (status === 403 && !data?.detail);
        if (sessionLost && error.config?.headers?.Authorization) {
            tokenStore.clear();
            window.dispatchEvent(new Event(SESSION_EXPIRED));
        }
        return Promise.reject(error);
    }
);

export default api;