import { api } from '@/lib/api';
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { User, Mail, Lock } from "lucide-react";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

export default function RegisterForm() {
    const navigate = useNavigate();
    const [error,setError]=useState('');
    const [formData, setFormData] = useState({ name: "", email: "", password: "", confirmPassword: "", documento: "" });
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            alert("Las contraseñas no coinciden");
            return;
        }

        setIsLoading(true);

        setError('');
        try { await api('/auth/register',{method:'POST',body:JSON.stringify({nombre:formData.name,email:formData.email,documento:formData.documento,password:formData.password})}); navigate('/login'); }
        catch(e){setError(e.message);}finally{setIsLoading(false);}
    };

    return (
        <Card className="w-full max-w-md p-8 shadow-lg">
            <div className="mb-6 text-center">
                <h2 className="text-2xl font-bold text-gray-900">Crea tu cuenta</h2>
                <p className="mt-2 text-sm text-gray-500">
                    Completa tus datos para comenzar a usar la plataforma
                </p>
            </div>

            <p role="status" className="mb-5 rounded-xl bg-blue-50 p-4 text-sm text-brand">El registro de cuentas está pendiente de integración con el backend.</p><form onSubmit={handleSubmit} className="space-y-4">{error&&<p role="alert" className="text-sm text-red-700">{error}</p>}<Input placeholder="DNI (8 dígitos)" aria-label="DNI" pattern="[0-9]{8}" required value={formData.documento} onChange={e=>setFormData({...formData,documento:e.target.value})} />
                <Input
                    icon={User}
                    type="text"
                    placeholder="Nombre completo"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    required
                />

                <Input
                    icon={Mail}
                    type="email"
                    placeholder="Correo electrónico"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    required
                />

                <Input
                    icon={Lock}
                    type="password"
                    placeholder="Contraseña"
                    value={formData.password}
                    onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                    required
                    minLength={8}
                />

                <Input
                    icon={Lock}
                    type="password"
                    placeholder="Confirmar contraseña"
                    value={formData.confirmPassword}
                    onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                    required
                />

                <Button type="submit" className="mt-2 w-full" disabled={true}>
                    {isLoading ? "Creando cuenta..." : "Registrarse"}
                </Button>
            </form>

            <div className="mt-6 text-center text-sm text-gray-600">
                ¿Ya tienes una cuenta?{" "}
                <Link to="/login" className="font-semibold text-brand hover:underline">
                    Inicia sesión aquí
                </Link>
            </div>
        </Card>
    );
}