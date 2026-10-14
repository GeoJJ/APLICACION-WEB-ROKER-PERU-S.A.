import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Mail, Lock, Eye, EyeOff } from "lucide-react";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

export default function LoginForm() {
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const [formData, setFormData] = useState({ email: "", password: "" });
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);

        // TODO: Aquí irá la llamada a tu backend Spring Boot
        setTimeout(() => {
            console.log("Intento de login con:", formData);
            setIsLoading(false);
            alert("¡Login simulado exitoso!");
            // navigate("/dashboard"); 
        }, 1500);
    };

    return (
        <Card className="w-full max-w-md p-8 shadow-lg">
            <div className="mb-6 text-center">
                <h2 className="text-2xl font-bold text-gray-900">Bienvenido de nuevo</h2>
                <p className="mt-2 text-sm text-gray-500">
                    Ingresa tus credenciales para acceder a tu cuenta
                </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
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

                <div className="flex items-center justify-between text-sm">
                    <label className="flex cursor-pointer items-center gap-2 text-gray-600">
                        <input type="checkbox" className="rounded border-gray-300 text-brand focus:ring-brand" />
                        Recordarme
                    </label>
                    <Link to="/recuperar-password" className="font-medium text-brand hover:underline">
                        ¿Olvidaste tu contraseña?
                    </Link>
                </div>

                <Button type="submit" className="w-full" disabled={isLoading}>
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