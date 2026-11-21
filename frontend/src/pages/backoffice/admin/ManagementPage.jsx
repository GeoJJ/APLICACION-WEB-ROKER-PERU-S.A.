import { useState } from 'react';
import { Users, Package, ShoppingCart, ShieldCheck, UserCog, Plus, Pencil, Trash2 } from 'lucide-react';
import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';
import { useAuth } from '@/hooks/useAuth';
import { ROLES } from '@/lib/auth';
import { money, stockState } from '@/data/backofficeStore';
import { SectionHeader, Panel, Field, Modal, FormActions, SearchBox } from '@/components/shared/ManagementUI';
import DataTable from '@/components/shared/DataTable';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
import Select from '@/components/ui/Select';

const configs = {
    workers: {
        title: 'Gestión de Trabajadores', singular: 'trabajador', icon: Users, canDelete: true,
        columns: ['nombre', 'email', 'telefono', 'idRol'],
        fields: [
            { key: 'nombre', label: 'Nombre completo' },
            { key: 'email', label: 'Correo electrónico', type: 'email' },
            { key: 'telefono', label: 'Teléfono', type: 'tel', pattern: '[0-9]{9}', hint: '9 dígitos', optional: true },
            { key: 'documento', label: 'DNI (8) o RUC (11)', pattern: '[0-9]{8}|[0-9]{11}', hint: '8 dígitos (DNI) u 11 dígitos (RUC)' },
            { key: 'direccion', label: 'Dirección', optional: true },
            { key: 'idRol', label: 'Rol', type: 'role' },
        ],
    },
    products: {
        title: 'Gestión de Productos', singular: 'producto', icon: Package, canDelete: true,
        columns: ['codigo', 'producto', 'categoria', 'precio', 'umbral', 'estado'],
        fields: [
            { key: 'codigo', label: 'Código' },
            { key: 'producto', label: 'Producto' },
            { key: 'categoria', label: 'Categoría' },
            { key: 'precio', label: 'Precio (S/)', type: 'number' },
            { key: 'umbral', label: 'Umbral de stock', type: 'number' },
            { key: 'stock', label: 'Stock inicial', type: 'number', onlyNew: true },
            { key: 'idProveedor', label: 'Proveedor', type: 'provider' },
            { key: 'estado', label: 'Estado', type: 'status' },
        ],
    },
    services: {
        title: 'Gestión de Servicios', singular: 'servicio', icon: ShoppingCart, canDelete: true,
        columns: ['categoria', 'nombre', 'precio', 'personal'],
        fields: [
            { key: 'categoria', label: 'Categoría' },
            { key: 'nombre', label: 'Servicio' },
            { key: 'tipo', label: 'Tipo de servicio', optional: true },
            { key: 'descripcion', label: 'Descripción', optional: true },
            { key: 'precio', label: 'Tarifa (S/)', type: 'number' },
            { key: 'duracion', label: 'Duración (horas)', type: 'number', hint: 'Solo cantidad de horas (ej. 1, 2)', optional: true },
            { key: 'personal', label: 'Personal requerido', type: 'number', optional: true },
        ],
    },
    providers: {
        title: 'Gestión de Proveedores', singular: 'proveedor', icon: Package, canDelete: true,
        columns: ['nombre', 'ruc', 'telefono', 'email'],
        fields: [
            { key: 'nombre', label: 'Proveedor' },
            { key: 'ruc', label: 'RUC (11 dígitos)', pattern: '[0-9]{11}', hint: '11 dígitos' },
            { key: 'telefono', label: 'Teléfono', type: 'tel', optional: true },
            { key: 'email', label: 'Correo', type: 'email', optional: true },
        ],
    },
    employees: {
        title: 'Personal de servicio', singular: 'empleado', icon: UserCog, canDelete: true,
        description: 'Empleados que ejecutan los servicios. No son usuarios del sistema.',
        columns: ['nombre', 'apellido', 'telefono'],
        fields: [
            { key: 'nombre', label: 'Nombre' },
            { key: 'apellido', label: 'Apellido' },
            { key: 'telefono', label: 'Teléfono', type: 'tel', pattern: '[0-9]{9}', hint: '9 dígitos', optional: true },
        ],
    },
    roles: {
        title: 'Gestión de Roles', singular: 'rol', icon: ShieldCheck, canDelete: false,
        columns: ['nombre', 'panel'],
        fields: [{
            key: 'nombre',
            label: 'Nombre del rol',
            placeholder: 'MAYÚSCULAS_CON_GUION_BAJO',
            pattern: '[A-Z_]{3,50}',
            hint: 'Solo mayúsculas y guion bajo, mínimo 3 letras',
        }],
    },
};

const COLUMN_LABELS = { nombre: 'Nombre', apellido: 'Apellido', telefono: 'Teléfono', email: 'Correo', idRol: 'Rol', panel: 'Acceso al panel', codigo: 'Código', producto: 'Producto', categoria: 'Categoría', precio: 'Precio', umbral: 'Umbral de stock', estado: 'Estado', personal: 'Personal requerido', ruc: 'RUC' };
const roleLabel = name => ROLES.find(r => r.code === name)?.label ?? name;
const itemName = r => r.nombre ?? r.producto ?? '';
const extractHours = val => {
    if (val === null || val === undefined || val === '') return '';
    const match = String(val).match(/\d+(\.\d+)?/);
    return match ? match[0] : '';
};

export default function ManagementPage({ kind, readonly = false }) {
    const { data, save, remove } = useBackOffice();
    const { user } = useAuth();
    const config = configs[kind];
    const [search, setSearch] = useState('');
    const [filter, setFilter] = useState('');
    const [draft, setDraft] = useState(null);
    const [toDelete, setToDelete] = useState(null);
    const [busy, setBusy] = useState(false);

    const visibleFields = draft ? config.fields.filter(f => !f.onlyNew || !draft.id) : [];

    const getSelectOptions = type => {
        if (type === 'provider') {
            return data.providers.map(p => ({ value: p.id, label: p.nombre }));
        }
        if (type === 'role') {
            return data.roles.map(r => ({ value: r.id, label: roleLabel(r.nombre) }));
        }
        return [
            { value: 'Activo', label: 'Activo' },
            { value: 'Inactivo', label: 'Inactivo' },
        ];
    };

    const filters = {
        workers: { all: 'Todos los roles', options: getSelectOptions('role'), test: (r, f) => String(r.idRol) === String(f) },
        products: { all: 'Todos los estados', options: getSelectOptions('status'), test: (r, f) => r.estado === f },
    }[kind];

    const cell = (key, r) => key === 'idRol' ? roleLabel(r.nombreRol)
        : key === 'estado' ? <Badge variant={r.estado === 'Activo' ? 'success' : 'warning'}>{r.estado}</Badge>
            : key === 'panel' ? (ROLES.some(x => x.code === r.nombre) ? <Badge variant="success">Reconocido</Badge> : <Badge variant="warning">Sin panel</Badge>)
                : key === 'precio' ? money(r.precio)
                    : r[key];

    const columns = config.columns.map(key => ({ key, label: COLUMN_LABELS[key], render: r => cell(key, r) }));
    if (kind === 'products') columns.push({ key: 'stock', label: 'Stock', render: r => <>{r.stock}<p className="text-xs text-gray-500">{stockState(r)}</p></> });
    if (!readonly) columns.push({
        key: 'action', label: 'Acción', render: r => <div className="flex justify-center gap-2">
            <Button size="sm" variant="outline" onClick={() => setDraft({
                ...r,
                ...(kind === 'services' ? { duracion: extractHours(r.duracion) } : {}),
            })}><Pencil size={15} className="mr-2" />Editar</Button>
            {config.canDelete && r.email !== user?.email && <button type="button" onClick={() => setToDelete(r)} aria-label={`Eliminar ${itemName(r)}`}
                className="inline-flex cursor-pointer items-center rounded-xl border-2 border-red-200 px-3 py-2 text-sm font-semibold text-red-600 transition hover:bg-red-600 hover:text-white"><Trash2 size={15} /></button>}
        </div>,
    });

    const rows = data[kind].filter(r =>
        Object.values(r).join(' ').toLowerCase().includes(search.toLowerCase()) &&
        (filter === '' || !filters || filters.test(r, filter))
    );

    const newRecord = () => setDraft(kind === 'products' ? { estado: 'Activo', stock: 0 } : {});

    async function submit(e) {
        e.preventDefault();
        setBusy(true);
        const payload = kind === 'services'
            ? {
                ...draft,
                duracion: draft.duracion === '' || draft.duracion === undefined ? null : Number(draft.duracion),
            }
            : draft;
        const ok = await save(kind, payload);
        setBusy(false);
        if (ok) setDraft(null);
    }

    async function confirmDelete() {
        setBusy(true);
        const ok = await remove(kind, toDelete.id);
        setBusy(false);
        if (ok) setToDelete(null);
    }

    return <>
        <SectionHeader
            icon={config.icon}
            title={readonly ? 'Consulta de Productos' : config.title}
            description={config.description || 'Consulta y administra la información del sistema.'}
            action={!readonly && <Button onClick={newRecord}><Plus size={18} className="mr-2" />Nuevo {config.singular}</Button>}
        />
        <Panel>
            {config.note && !readonly && <p className="mb-5 rounded-lg bg-amber-50 p-4 text-sm leading-6 text-amber-800">{config.note}</p>}
            <div className="mb-6 flex flex-wrap gap-3">
                <SearchBox value={search} onChange={setSearch} placeholder={`Buscar ${config.singular}...`} />
                {filters && (
                    <Select
                        aria-label="Filtrar"
                        className="w-full sm:w-56"
                        placeholder={filters.all}
                        value={filter}
                        onChange={e => setFilter(e.target.value)}
                        options={filters.options}
                    />
                )}
            </div>
            <DataTable columns={columns} rows={rows} />
        </Panel>

        {draft && <Modal title={`${draft.id ? 'Editar' : 'Nuevo'} ${config.singular}`} onClose={() => setDraft(null)}>
            <form onSubmit={submit}>
                <div className={`grid gap-4 ${visibleFields.length > 1 ? 'sm:grid-cols-2' : 'grid-cols-1'}`}>
                    {visibleFields.map(f => (
                        <Field key={f.key} label={f.label}>
                            {['role', 'status', 'provider'].includes(f.type)
                                ? <Select
                                    aria-label={f.label}
                                    required
                                    placeholder="Seleccionar"
                                    value={draft[f.key] ?? ''}
                                    onChange={e => setDraft({ ...draft, [f.key]: e.target.value })}
                                    options={getSelectOptions(f.type)}
                                />
                                : <input
                                    name={f.key}
                                    required={!f.optional}
                                    placeholder={f.placeholder}
                                    className="bo-input"
                                    type={f.type || 'text'}
                                    min={f.type === 'number' ? 0 : undefined}
                                    step={f.key === 'precio' ? '0.01' : f.key === 'duracion' ? '0.5' : f.type === 'number' ? '1' : undefined}
                                    pattern={f.pattern}
                                    title={f.hint}
                                    value={draft[f.key] ?? ''}
                                    onChange={e => {
                                        const val = kind === 'roles'
                                            ? e.target.value.toUpperCase().replace(/\s+/g, '_')
                                            : e.target.value;
                                        setDraft({ ...draft, [f.key]: val });
                                    }}
                                />}
                        </Field>
                    ))}
                </div>
                {kind === 'workers' && <div className="mt-4">
                    <Field label={draft.id ? 'Nueva contraseña (opcional)' : 'Contraseña inicial'}>
                        <input name="password" className="bo-input" type="password" autoComplete="new-password" minLength={8} maxLength={72} required={!draft.id}
                            value={draft.password || ''} onChange={e => setDraft({ ...draft, password: e.target.value })} />
                    </Field>
                </div>}
                <FormActions onClose={() => setDraft(null)} busy={busy} />
            </form>
        </Modal>}

        {toDelete && <Modal title={`Eliminar ${config.singular}`} onClose={() => setToDelete(null)}>
            <p className="text-sm leading-6 text-gray-600">¿Seguro que quieres eliminar <strong>{itemName(toDelete)}</strong>? Esta acción no se puede deshacer.</p>
            <div className="mt-6 flex justify-end gap-3">
                <Button variant="outline" onClick={() => setToDelete(null)}>Cancelar</Button>
                <button type="button" disabled={busy} onClick={confirmDelete}
                    className="inline-flex cursor-pointer items-center justify-center rounded-xl bg-red-600 px-6 py-2.5 font-semibold text-white shadow-md transition hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50">{busy ? 'Eliminando…' : 'Eliminar'}</button>
            </div>
        </Modal>}
    </>;
}