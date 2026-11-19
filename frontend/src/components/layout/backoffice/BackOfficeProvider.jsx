import { createContext, useContext, useEffect, useRef, useState } from 'react';
import { useAuth } from '@/hooks/useAuth';
import { emptyPanel, loadPanel } from '@/lib/backofficeApi';
import { saveResource, deleteResource, postMovement } from '@/lib/adminApi';
const Context = createContext(null);
export default function BackOfficeProvider({ children }) {
    const { role } = useAuth(); const [data, setData] = useState(emptyPanel); const [loaded, setLoaded] = useState(''); const [notice, setNotice] = useState(''); const [notes, setNotes] = useState([]); const [actorId, setActorId] = useState(''); const generation = useRef(0);
    const roleId = role || '', key = roleId + ':' + actorId;
    useEffect(() => {
        let active = true; const token = ++generation.current; if (!roleId) return;
        loadPanel(roleId, actorId).then(result => { if (active && token === generation.current) { setData(result.data); setNotes(result.notes); setLoaded(key); } });
        return () => { active = false; };
    }, [roleId, actorId, key]);
    async function refresh() { const token = ++generation.current; const result = await loadPanel(roleId, actorId); if (token === generation.current) { setData(result.data); setNotes(result.notes); setLoaded(key); } }

    // Ejecuta una operación contra el backend; si sale bien recarga los datos y avisa, si falla muestra el motivo
    // que devolvió el servidor (ProblemDetail "detail"). Devuelve true/false para que la pantalla cierre o no el modal.
    async function mutate(action, success, describeError = e => e.message) {
        setNotice('');
        try { await action(); await refresh(); setNotice(success); return true; }
        catch (e) { setNotice(describeError(e)); return false; }
    }
    // Solo el rol administrador tiene endpoints de escritura conectados
    const save = (kind, record) => mutate(() => saveResource(kind, record), record.id == null ? 'Registro creado correctamente.' : 'Cambios guardados correctamente.');
    const remove = (kind, id) => mutate(() => deleteResource(kind, id), 'Registro eliminado.',
        e => e.message.includes('restricción de datos') ? 'No se puede eliminar: el registro está en uso (pedidos, movimientos u otros datos asociados).' : e.message);
    const registerMovement = (type, form) => mutate(() => postMovement(type, form), type === 'ingreso' ? 'Ingreso registrado: el stock aumentó.' : 'Ajuste registrado: el stock disminuyó.');

    // Las pantallas de otros roles todavía usan este stub (se conectarán con sus propios controllers)
    async function update() { setNotice('La edición está pendiente de autenticación e integración con el backend actual. No se guardaron cambios.'); return false; }
    return <Context.Provider value={{ data: loaded === key ? data : emptyPanel(), loading: Boolean(roleId && loaded !== key), error: '', notice, setNotice, notes, refresh, update, save, remove, registerMovement, actorId, setActorId, readOnly: roleId !== 'admin' }}>{children}</Context.Provider>;
}
// eslint-disable-next-line react-refresh/only-export-components
export const useBackOffice = () => useContext(Context);
