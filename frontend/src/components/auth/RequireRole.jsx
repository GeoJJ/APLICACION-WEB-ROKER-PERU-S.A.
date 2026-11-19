import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { homeFor } from '@/lib/auth';

export default function RequireRole({ role }) {
    const { role: current } = useAuth();
    const location = useLocation();
    if (!current) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
    if (role && current !== role) return <Navigate to={homeFor(current)} replace />;
    return <Outlet />;
}