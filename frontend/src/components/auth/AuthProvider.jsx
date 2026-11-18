import { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import api from '@/lib/axiosConfig'; 
import { ROLES } from '@/data/roles';

const Context = createContext(null);

export default function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [role, setRole] = useState(null);
    const [isInitialized, setIsInitialized] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        const checkToken = () => {
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    const decoded = jwtDecode(token);
                    if (decoded.exp * 1000 < Date.now()) {
                        logoutLocally();
                    } else {
                        const springRole = decoded.role ? decoded.role.replace('ROLE_', '') : 'ADMINISTRADOR';
                        const frontendRoleObj = ROLES.find(r => r.code === springRole);
                        const userRole = frontendRoleObj ? frontendRoleObj.id : 'customer';

                        setUser({
                            email: decoded.sub,
                            correoUsuario: decoded.sub,
                            nombreUsuario: decoded.sub.split('@')[0],
                            name: decoded.sub.split('@')[0]
                        });
                        setRole(userRole);
                    }
                } catch (error) {
                    console.error("Token corrupto", error);
                    logoutLocally();
                }
            }
            setIsInitialized(true);
        };
        checkToken();
    }, []);

    async function login(email, password) {
        try {
            const response = await api.post('/auth/login', { 
                correoUsuario: email, 
                passwordUsuario: password 
            });

            const { token } = response.data;
            localStorage.setItem('token', token);
            
            const decoded = jwtDecode(token);
            const loggedUser = {
                email: decoded.sub,
                correoUsuario: decoded.sub,
                nombreUsuario: decoded.sub.split('@')[0],
                name: decoded.sub.split('@')[0]
            };

            const springRole = decoded.role ? decoded.role.replace('ROLE_', '') : 'ADMINISTRADOR';
            const frontendRoleObj = ROLES.find(r => r.code === springRole);
            const userRole = frontendRoleObj ? frontendRoleObj.id : 'customer';

            setUser(loggedUser);
            setRole(userRole);

            return { ...loggedUser, role: userRole };

        } catch (error) {
            if (error.response?.status === 403 || error.response?.status === 401) {
                throw new Error('Correo o contraseña incorrectos');
            }
            throw new Error(error.response?.data?.mensaje || 'Error de conexión con el servidor');
        }
    }

    // Función auxiliar para limpiar estados locales
    const logoutLocally = () => {
        localStorage.removeItem('token');
        setUser(null);
        setRole(null);
    };

    // Función oficial de logout conectada al backend
    async function logout() {
        try {
            const token = localStorage.getItem('token');
            if (token) {
                // Notificamos al backend de tu compañero enviando el token en el header
                await api.post('/auth/logout', {}, {
                    headers: { Authorization: `Bearer ${token}` }
                });
            }
        } catch (error) {
            console.error("Error al cerrar sesión en el servidor", error);
        } finally {
            logoutLocally();
            navigate('/login');
        }
    }

    if (!isInitialized) return null;

    return (
        <Context.Provider value={{ user, role, login, logout }}>
            {children}
        </Context.Provider>
    );
}

export const useAuth = () => useContext(Context);