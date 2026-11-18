import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { User, Mail, Lock, Building, FileText, Phone, MapPin } from "lucide-react";
import Input from "@/components/ui/Input";
import Button from "@/components/ui/Button";
import Card from "@/components/ui/Card";
import api from '@/lib/axiosConfig';

export default function RegisterForm() {
    const navigate = useNavigate();
    const [error, setError] = useState('');
    const [formData, setFormData] = useState({ 
        tipoPersona: "NATURAL", // Por defecto Natural (DNI)
        name: "", 
        email: "", 
        documento: "", 
        telefono: "",
        direccion: "",
        password: "", 
        confirmPassword: "" 
    });
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            alert("Las contraseñas no coinciden");
            return;
        }

        // Validaciones previas según el tipo de persona que exige el backend de tu compañero
        if (formData.tipoPersona === "NATURAL" && formData.documento.length !== 8) {
            alert("El DNI de una persona natural debe tener exactamente 8 dígitos");
            return;
        }

        if (formData.tipoPersona === "JURIDICA" && formData.documento.length !== 11) {
            alert("El RUC de una persona jurídica debe tener exactamente 11 dígitos");
            return;
        }

        setIsLoading(true);
        setError('');

        try {
            await api.post('/auth/register', {
                nombreUsuario: formData.name,
                correoUsuario: formData.email,
                tipoPersonaUsuario: formData.tipoPersona, // "NATURAL" o "JURIDICA"
                numeroDocumentoUsuario: formData.documento, // DNI (8) o RUC (11)[cite: 21]
                telefonoUsuario: formData.telefono || null,
                direccionUsuario: formData.direccion || null,
                passwordUsuario: formData.password
            });
            
            navigate('/login');
        } catch (e) {
            setError(e.response?.data?.mensaje || e.response?.data?.message || 'Error al registrarse en el servidor');
        } finally {
            setIsLoading(false);
        }
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
                {error && <p role="alert" className="rounded-lg bg-red-50 p-3 text-sm text-red-700">{error}</p>}
                
                {/* Selector para elegir entre Persona Natural o Jurídica */}
                <div className="grid grid-cols-2 gap-2">
                    <button
                        type="button"
                        onClick={() => setFormData({...formData, tipoPersona: "NATURAL", documento: ""})}
                        className={`py-2 text-xs font-semibold rounded-lg border transition ${
                            formData.tipoPersona === "NATURAL" 
                                ? "bg-blue-600 text-white border-blue-600" 
                                : "bg-white text-gray-700 border-gray-300 hover:bg-gray-50"
                        }`}
                    >
                        Persona Natural
                    </button>
                    <button
                        type="button"
                        onClick={() => setFormData({...formData, tipoPersona: "JURIDICA", documento: ""})}
                        className={`py-2 text-xs font-semibold rounded-lg border transition ${
                            formData.tipoPersona === "JURIDICA" 
                                ? "bg-blue-600 text-white border-blue-600" 
                                : "bg-white text-gray-700 border-gray-300 hover:bg-gray-50"
                        }`}
                    >
                        Persona Jurídica
                    </button>
                </div>

                <Input 
                    icon={FileText}
                    placeholder={formData.tipoPersona === "NATURAL" ? "DNI (8 dígitos)" : "RUC (11 dígitos)"} 
                    aria-label="Documento" 
                    maxLength={formData.tipoPersona === "NATURAL" ? 8 : 11}
                    required 
                    value={formData.documento} 
                    onChange={e => setFormData({...formData, documento: e.target.value})} 
                />
                
                <Input
                    icon={formData.tipoPersona === "NATURAL" ? User : Building}
                    type="text"
                    placeholder={formData.tipoPersona === "NATURAL" ? "Nombre completo" : "Razón social / Nombre de empresa"}
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
                    icon={Phone}
                    type="tel"
                    placeholder="Teléfono (9 dígitos, opcional)"
                    maxLength={9}
                    value={formData.telefono}
                    onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                />

                <Input
                    icon={MapPin}
                    type="text"
                    placeholder="Dirección (opcional)"
                    value={formData.direccion}
                    onChange={(e) => setFormData({ ...formData, direccion: e.target.value })}
                />

                <Input
                    icon={Lock}
                    type="password"
                    placeholder="Contraseña (mínimo 8 caracteres)"
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