import axios from 'axios';

// 1. Creamos la instancia principal de Axios
const api = axios.create({
    baseURL: 'http://localhost:8080', // La dirección de tu backend Spring Boot
    headers: {
        'Content-Type': 'application/json',
    },
});

// 2. INTERCEPTOR DE PETICIÓN (Request Interceptor)
// Este código se ejecuta automáticamente ANTES de que cualquier petición (GET, POST, PUT, DELETE) salga al servidor.
api.interceptors.request.use(
    (config) => {
        // Sacamos el token de la bóveda del navegador
        const token = localStorage.getItem('token');
        
        // Si tenemos un token guardado, se lo inyectamos a la cabecera Authorization
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        
        // Dejamos que la petición continúe su viaje hacia Spring Boot
        return config;
    },
    (error) => {
        // Si ocurre un error antes de enviar la petición, lo rechazamos
        return Promise.reject(error);
    }
);

// 3. (Opcional pero recomendado) INTERCEPTOR DE RESPUESTA
// Sirve para atrapar errores globales, como cuando el token expira (Status 401)
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response && error.response.status === 401) {
            // Si el backend dice "No autorizado" (el token venció o es inválido)
            // Aquí podrías forzar el cierre de sesión, por ejemplo:
            localStorage.removeItem('token');
            window.location.href = '/login'; 
        }
        return Promise.reject(error);
    }
);

export default api;