import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthProvider';
export default function RequireRole({ role: expected }) {
 const {user,role,loading,error}=useAuth();const location=useLocation();
 if(loading)return <p className="p-8 text-center text-gray-500">Verificando sesión...</p>;
 if(error && !user)return <div className="p-8 text-center"><p role="alert">{error}</p><button onClick={() => window.location.reload()} className="mt-4 text-brand underline">Reintentar</button></div>;
 if(!user)return <Navigate to="/login" replace state={{from:location.pathname}} />;
 if(expected && role?.id !== expected)return <Navigate to={role?.home || '/'} replace />;
 return <Outlet />;
}
