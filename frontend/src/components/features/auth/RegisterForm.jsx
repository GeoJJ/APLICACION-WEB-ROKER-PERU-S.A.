import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { User, Mail, Lock } from "lucide-react";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";

export default function RegisterForm() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ name: "", email: "", password: "", confirmPassword: "" });
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            alert("Las contraseñas no coinciden");
            return;
        }

        setIsLoading(true);

        // TODO: Aquí irá la llamada a tu backend Spring Boot
        setTimeout(() => {
            console.log("Intento de registro con:", formData);
            setIsLoading(false);
            alert("¡Registro simulado exitoso! Redirigiendo al login...");
            navigate("/login");
        }, 1500);
    };

    return (
        <Card className="w-full max-w-md p-8 shadow-lg">
            <div className="mb-6 text-center">
                <h2 className="text-2xl font-bold text-gray-900">Crea tu cuenta</h2>
                <p className="mt-2 text-sm text-gray-500">
                    Completa tus datos para comenzar a usar la plataforma
                </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
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
                    minLength={6}
                />

                <Input
                    icon={Lock}
                    type="password"
                    placeholder="Confirmar contraseña"
                    value={formData.confirmPassword}
                    onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                    required
                />

                <Button type="submit" className="mt-2 w-full" disabled={isLoading}>
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