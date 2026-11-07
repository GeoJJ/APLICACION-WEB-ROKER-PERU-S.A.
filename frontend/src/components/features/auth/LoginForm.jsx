import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from '@/components/auth/AuthProvider';
import { ROLES } from '@/data/roles';
import { Mail, Lock, Eye, EyeOff } from "lucide-react";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

export default function LoginForm() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [error, setError] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [formData, setFormData] = useState({ email: "", password: "" });
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);

        setError('');
        try {
            const user = await login(formData.email, formData.password);
            const from=location.state?.from;
            navigate(typeof from==='string'&&from.startsWith('/')&&!from.startsWith('//')?from:ROLES.find(r => r.code === user.role)?.home || '/', { replace: true });
        } catch (e) { setError(e.message); }
        finally { setIsLoading(false); }
    };

    return (
        <Card className="w-full max-w-md p-8 shadow-lg">
            <div className="mb-6 text-center">
                <h2 className="text-2xl font-bold text-gray-900">Bienvenido de nuevo</h2>
                <p className="mt-2 text-sm text-gray-500">
                    Ingresa tus credenciales para acceder a tu cuenta
                </p>
            </div>

            <div role="status" className="mb-5 rounded-xl bg-blue-50 p-4 text-sm leading-6 text-brand">El inicio de sesión está pendiente en el backend. Puedes revisar los paneles sin simular una sesión.</div><div className="mb-6 flex flex-wrap gap-3">{ROLES.filter(r=>r.id!=='customer').map(r=><Link key={r.id} to={r.home} className="rounded-lg border border-gray-200 px-3 py-2 text-xs text-brand">{r.label}</Link>)}</div><form onSubmit={handleSubmit} className="space-y-5">
                {error && <p role="alert" className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}
                <Input
                    icon={Mail}
                    type="email"
                    placeholder="Correo electrónico"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    required
                />

                <div className="relative">
                    <Input
                        icon={Lock}
                        type={showPassword ? "text" : "password"}
                        placeholder="Contraseña"
                        value={formData.password}
                        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        required
                    />
                    <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 focus:outline-none"
                    >
                        {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                    </button>
                </div>



                <Button type="submit" className="w-full" disabled={true}>
                    {isLoading ? "Ingresando..." : "Iniciar Sesión"}
                </Button>
            </form>

            <div className="mt-6 text-center text-sm text-gray-600">
                ¿No tienes una cuenta?{" "}
                <Link to="/register" className="font-semibold text-brand hover:underline">
                    Regístrate aquí
                </Link>
            </div>
        </Card>
    );
}
