import { useMemo, useState } from 'react';
import { Boxes, Plus, Minus, AlertTriangle, RotateCcw } from 'lucide-react';
import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';
import { SectionHeader, Panel, Modal, Field, FormActions, SearchBox, Summary } from '@/components/shared/ManagementUI';
import DataTable from '@/components/shared/DataTable';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
import Select from '@/components/ui/Select';

const MODES = {
    ingreso: { title: 'Registrar ingreso', help: 'Suma unidades al stock del producto (recepción de mercadería).' },
    ajuste: { title: 'Registrar ajuste', help: 'RESTA unidades del stock (mermas, pérdidas o correcciones). No puede dejar el stock en negativo.' },
};

const LEVELS = ['TODAS', 'AGOTADO', 'CRÍTICO', 'ADVERTENCIA'];
const getLevel = n => LEVELS.slice(1).find(l => n.startsWith(l)) || 'NINGUNA';
const urgency = n => ['AGOTADO', 'CRÍTICO'].some(l => n.startsWith(l)) ? 'danger' : n.startsWith('ADVERTENCIA') ? 'warning' : 'success';
const movementBadge = t => t === 'INGRESO' ? 'success' : t === 'AJUSTE' ? 'warning' : 'info';
const toOpts = items => items.map(([value, label]) => ({ value, label }));

export default function AdminInventoryPage() {
    const { data, registerMovement } = useBackOffice();
    const [draft, setDraft] = useState(null);
    const [busy, setBusy] = useState(false);
    const [alertLevel, setAlertLevel] = useState('TODAS');
    const [f, setF] = useState({ search: '', type: 'TODOS', product: 'TODOS' });

    const setFilter = (k, v) => setF(prev => ({ ...prev, [k]: v }));
    const product = draft && data.products.find(p => String(p.id) === String(draft.productId));

    const activeAlerts = useMemo(() => data.alerts.filter(a => !a.nivel.startsWith('NINGUNA')), [data.alerts]);
    const filteredAlerts = useMemo(
        () => alertLevel === 'TODAS' ? activeAlerts : activeAlerts.filter(a => getLevel(a.nivel) === alertLevel),
        [activeAlerts, alertLevel]
    );

    const typeOpts = useMemo(() => {
        const types = [...new Set(data.movements.map(m => m.tipo).filter(Boolean))];
        return toOpts([['TODOS', 'Todos los tipos'], ...types.map(t => [t, t])]);
    }, [data.movements]);

    const productOpts = useMemo(
        () => toOpts([['TODOS', 'Todos los productos'], ...data.products.map(p => [p.id, p.producto])]),
        [data.products]
    );

    const filteredMovements = useMemo(() => data.movements.filter(m =>
        (m.producto?.toLowerCase() || '').includes(f.search.toLowerCase()) &&
        (f.type === 'TODOS' || m.tipo === f.type) &&
        (f.product === 'TODOS' || [m.productoId, m.idProducto, m.producto].some(v => String(v) === String(f.product)))
    ), [data.movements, f]);

    const hasFilters = Boolean(f.search.trim() || f.type !== 'TODOS' || f.product !== 'TODOS');
    const openModal = (type, productId = '') => setDraft({ type, productId, cantidad: '' });

    async function submit(e) {
        e.preventDefault();
        setBusy(true);
        const ok = await registerMovement(draft.type, draft);
        setBusy(false);
        if (ok) setDraft(null);
    }

    return <>
        <SectionHeader
            icon={Boxes}
            title="Inventario"
            description="Ingresos, ajustes, alertas de stock e historial de movimientos."
            action={<div className="flex flex-wrap gap-3">
                <Button onClick={() => openModal('ingreso')}><Plus size={18} className="mr-2" />Registrar ingreso</Button>
                <Button variant="outline" onClick={() => openModal('ajuste')}><Minus size={18} className="mr-2" />Registrar ajuste</Button>
            </div>}
        />

        <Summary items={[
            ['Productos', data.products.length],
            ['Unidades en stock', data.products.reduce((s, p) => s + p.stock, 0)],
            ['Alertas de stock', activeAlerts.length],
            ['Movimientos', data.movements.length],
        ]} />

        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-lg font-semibold">Alertas de stock</h2>
            {activeAlerts.length > 0 && <div className="flex flex-wrap gap-2">
                {LEVELS.map(level => (
                    <button key={level} type="button" onClick={() => setAlertLevel(level)}
                        className={`rounded-full px-3 py-1 text-xs font-medium transition ${alertLevel === level ? 'bg-brand text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
                        {level}
                    </button>
                ))}
            </div>}
        </div>

        <div className="mb-8 grid gap-3">
            {filteredAlerts.length ? filteredAlerts.map(a => (
                <Panel key={a.id}>
                    <div className="flex flex-wrap items-center justify-between gap-3 text-sm">
                        <div className="flex flex-wrap items-center gap-3">
                            <AlertTriangle className="shrink-0 text-amber-500" size={20} />
                            <span><strong>{a.producto}</strong>: {a.stock} unidades (mínimo {a.umbral}).</span>
                            <Badge variant={urgency(a.nivel)}>{a.nivel}</Badge>
                        </div>
                        <button type="button" onClick={() => openModal('ingreso', a.productoId ?? a.idProducto ?? '')}
                            className="text-xs font-medium text-brand hover:underline">
                            Reponer stock
                        </button>
                    </div>
                </Panel>
            )) : <Panel>
                {activeAlerts.length === 0 ? 'Todos los productos tienen stock suficiente.' : 'No hay alertas que coincidan con el filtro seleccionado.'}
            </Panel>}
        </div>

        <h2 className="mb-4 text-lg font-semibold">Historial de movimientos</h2>
        <Panel>
            <div className="mb-6 flex flex-wrap items-center gap-3">
                <SearchBox value={f.search} onChange={v => setFilter('search', v)} placeholder="Buscar producto..." />
                <Select aria-label="Filtrar por tipo de movimiento" className="w-full sm:w-48"
                    value={f.type} onChange={e => setFilter('type', e.target.value)} options={typeOpts} />
                <Select aria-label="Filtrar por producto" className="w-full sm:w-56"
                    value={f.product} onChange={e => setFilter('product', e.target.value)} options={productOpts} />
                {hasFilters && <button type="button" title="Limpiar filtros"
                    onClick={() => setF({ search: '', type: 'TODOS', product: 'TODOS' })}
                    className="inline-flex h-10 shrink-0 cursor-pointer items-center justify-center rounded-xl border border-gray-200 px-3 text-gray-600 transition hover:bg-gray-50">
                    <RotateCcw size={16} />
                </button>}
            </div>

            <DataTable
                emptyText={hasFilters ? 'No hay movimientos que coincidan con los filtros aplicados.' : 'Todavía no hay movimientos registrados.'}
                rows={filteredMovements}
                columns={[
                    { key: 'fecha', label: 'Fecha' },
                    { key: 'producto', label: 'Producto' },
                    { key: 'tipo', label: 'Tipo', render: m => <Badge variant={movementBadge(m.tipo)}>{m.tipo}</Badge> },
                    { key: 'cantidad', label: 'Cantidad' },
                ]}
            />
        </Panel>

        {draft && <Modal title={MODES[draft.type].title} onClose={() => setDraft(null)}>
            <form onSubmit={submit}>
                <p className="mb-4 text-sm text-gray-600">{MODES[draft.type].help}</p>
                <div className="grid gap-4">
                    <Field label="Producto">
                        <Select required placeholder="Seleccionar producto" value={draft.productId}
                            onChange={e => setDraft({ ...draft, productId: e.target.value })}
                            options={toOpts(data.products.filter(p => p.estado === 'Activo').map(p => [p.id, p.producto]))} />
                    </Field>
                    {product && <p className="text-sm text-gray-500">Stock actual: <strong>{product.stock}</strong> unidades</p>}
                    <Field label="Cantidad">
                        <input required name="cantidad" className="bo-input" type="number" min="1" step="1"
                            max={draft.type === 'ajuste' ? product?.stock : undefined}
                            value={draft.cantidad} onChange={e => setDraft({ ...draft, cantidad: e.target.value })} />
                    </Field>
                </div>
                <FormActions onClose={() => setDraft(null)} busy={busy} />
            </form>
        </Modal>}
    </>;
}