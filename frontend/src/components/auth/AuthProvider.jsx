import { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import api from '@/lib/axiosConfig'; 
import { ROLES } from '@/data/roles'; // 1. IMPORTANTE: Importar tu tabla de roles

const Context = createContext(null);

export default function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [role, setRole] = useState(null);
    const [isInitialized, setIsInitialized] = useState(false);
    const navigate = useNavigate();

    // 2. EFECTO AL REFRESCAR (F5)
    useEffect(() => {
        const checkToken = () => {
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    const decoded = jwtDecode(token);
                    if (decoded.exp * 1000 < Date.now()) {
                        logout();
                    } else {
                        // Extraemos el rol de Spring ("ADMINISTRADOR")
                        const springRole = decoded.role ? decoded.role.replace('ROLE_', '') : 'ADMINISTRADOR';
                        
                        // Buscamos el ID corto ("admin") en tu tabla
                        const frontendRoleObj = ROLES.find(r => r.code === springRole);
                        const userRole = frontendRoleObj ? frontendRoleObj.id : 'customer';

                        // Agrega el usuario con datos de respaldo
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
                    logout();
                }
            }
            setIsInitialized(true);
        };
        checkToken();
    }, []);

    // 3. FUNCIÓN DE LOGIN
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
                // Extraemos el texto antes del @ para usarlo como nombre temporal (ej. "admin")
                nombreUsuario: decoded.sub.split('@')[0],
                name: decoded.sub.split('@')[0]
            };

            // Extraemos el rol de Spring ("ADMINISTRADOR")
            const springRole = decoded.role ? decoded.role.replace('ROLE_', '') : 'ADMINISTRADOR';
            
            // Buscamos el ID corto ("admin") en tu tabla
            const frontendRoleObj = ROLES.find(r => r.code === springRole);
            const userRole = frontendRoleObj ? frontendRoleObj.id : 'customer';

            setUser(loggedUser);
            setRole(userRole); // Guardamos "admin"

            return { ...loggedUser, role: userRole };

        } catch (error) {
            if (error.response?.status === 403 || error.response?.status === 401) {
                throw new Error('Correo o contraseña incorrectos');
            }
            throw new Error(error.response?.data?.mensaje || 'Error de conexión con el servidor');
        }
    }

    function logout() {
        localStorage.removeItem('token');
        setUser(null);
        setRole(null);
        navigate('/login');
    }

    if (!isInitialized) return null;

    return (
        <Context.Provider value={{ user, role, login, logout }}>
            {children}
        </Context.Provider>
    );
}

export const useAuth = () => useContext(Context);