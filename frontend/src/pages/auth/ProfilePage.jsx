import { useEffect, useState } from 'react';
import { UserRound, Pencil, CheckCircle2, X } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { fetchClientProfile, updateClientProfile } from '@/lib/clienteApi';
import { SectionHeader, Panel, Field } from '@/components/shared/ManagementUI';
import Button from '@/components/ui/Button';

const EMPTY_PROFILE = {
    nombreUsuario: '',
    correoUsuario: '',
    telefonoUsuario: '',
    numeroDocumentoUsuario: '',
    direccionUsuario: '',
};

function mapFromApi(data) {
    if (!data) return EMPTY_PROFILE;
    return {
        nombreUsuario: data.nombreUsuario ?? data.nombre ?? '',
        correoUsuario: data.correoUsuario ?? data.email ?? '',
        telefonoUsuario: data.telefonoUsuario ?? data.telefono ?? '',
        numeroDocumentoUsuario: data.numeroDocumentoUsuario ?? data.documento ?? '',
        direccionUsuario: data.direccionUsuario ?? data.direccion ?? '',
    };
}

export default function ProfilePage() {
    const auth = useAuth?.() || {};
    const [form, setForm] = useState(EMPTY_PROFILE);
    const [savedProfile, setSavedProfile] = useState(EMPTY_PROFILE);
    const [editing, setEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        let active = true;

        async function loadProfile() {
            setLoading(true);
            setError('');
            try {
                const data = await fetchClientProfile();
                if (!active) return;
                const mapped = mapFromApi(data);
                setForm(mapped);
                setSavedProfile(mapped);
            } catch (err) {
                if (!active) return;
                setError(err?.message || 'No se pudo cargar tu perfil.');
            } finally {
                if (active) setLoading(false);
            }
        }

        loadProfile();
        return () => {
            active = false;
        };
    }, []);

    function handleChange(field, value) {
        setForm(prev => ({ ...prev, [field]: value }));
    }

    function handleCancel() {
        setForm(savedProfile);
        setEditing(false);
        setError('');
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setSaving(true);
        setError('');
        setMessage('');

        try {
            const updated = await updateClientProfile(form);
            const mapped = mapFromApi(updated);
            setForm(mapped);
            setSavedProfile(mapped);
            setEditing(false);
            setMessage('Perfil actualizado correctamente.');

            if (typeof auth.setUser === 'function') {
                auth.setUser(prev => ({ ...(prev || {}), ...mapped }));
            }
        } catch (err) {
            setError(err?.message || 'No se pudieron guardar los cambios.');
        } finally {
            setSaving(false);
        }
    }

    return (
        <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6">
            <SectionHeader
                icon={UserRound}
                title="Mi perfil"
                description="Consulta y actualiza tus datos personales y de contacto."
            />

            {message && (
                <div
                    role="status"
                    className="mb-6 flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
                >
                    <CheckCircle2 size={18} className="shrink-0 text-emerald-600" />
                    <span>{message}</span>
                </div>
            )}

            {error && (
                <div
                    role="alert"
                    className="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
                >
                    {error}
                </div>
            )}

            <Panel>
                <form onSubmit={handleSubmit}>
                    <div className="grid gap-5 sm:grid-cols-2">
                        <Field label="Nombre completo">
                            <input
                                type="text"
                                required
                                disabled={loading || !editing || saving}
                                className="bo-input disabled:bg-gray-50 disabled:text-gray-600"
                                value={form.nombreUsuario}
                                onChange={e => handleChange('nombreUsuario', e.target.value)}
                            />
                        </Field>

                        <Field label="Correo electrónico">
                            <input
                                type="email"
                                required
                                disabled={loading || !editing || saving}
                                className="bo-input disabled:bg-gray-50 disabled:text-gray-600"
                                value={form.correoUsuario}
                                onChange={e => handleChange('correoUsuario', e.target.value)}
                            />
                        </Field>

                        <Field label="Teléfono">
                            <input
                                type="tel"
                                disabled={loading || !editing || saving}
                                className="bo-input disabled:bg-gray-50 disabled:text-gray-600"
                                value={form.telefonoUsuario}
                                onChange={e => handleChange('telefonoUsuario', e.target.value)}
                            />
                        </Field>

                        <Field label="DNI / Documento">
                            <input
                                type="text"
                                disabled={loading || !editing || saving}
                                className="bo-input disabled:bg-gray-50 disabled:text-gray-600"
                                value={form.numeroDocumentoUsuario}
                                onChange={e => handleChange('numeroDocumentoUsuario', e.target.value)}
                            />
                        </Field>

                        <div className="sm:col-span-2">
                            <Field label="Dirección">
                                <input
                                    type="text"
                                    disabled={loading || !editing || saving}
                                    className="bo-input disabled:bg-gray-50 disabled:text-gray-600"
                                    value={form.direccionUsuario}
                                    onChange={e => handleChange('direccionUsuario', e.target.value)}
                                />
                            </Field>
                        </div>
                    </div>

                    <div className="mt-6 flex flex-wrap items-center justify-end gap-3 border-t border-gray-100 pt-5">
                        {!editing ? (
                            <Button
                                type="button"
                                disabled={loading}
                                onClick={() => {
                                    setMessage('');
                                    setError('');
                                    setEditing(true);
                                }}
                            >
                                <Pencil size={16} className="mr-2" />
                                Editar perfil
                            </Button>
                        ) : (
                            <>
                                <Button
                                    type="button"
                                    variant="outline"
                                    disabled={saving}
                                    onClick={handleCancel}
                                >
                                    <X size={16} className="mr-1.5" />
                                    Cancelar
                                </Button>
                                <Button type="submit" disabled={saving}>
                                    <CheckCircle2 size={16} className="mr-2" />
                                    {saving ? 'Guardando...' : 'Guardar cambios'}
                                </Button>
                            </>
                        )}
                    </div>
                </form>
            </Panel>
        </div>
    );
}