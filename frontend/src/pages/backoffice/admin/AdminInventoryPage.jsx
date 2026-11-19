import { useMemo, useState } from 'react';
import { Boxes, Plus, Minus, AlertTriangle, RotateCcw } from 'lucide-react';
import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';
import { SectionHeader, Panel, Modal, Field, FormActions, SearchBox, Summary } from '@/components/shared/ManagementUI';
import DataTable from '@/components/shared/DataTable';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';

const MODES = {
    ingreso: { title: 'Registrar ingreso', help: 'Suma unidades al stock del producto (recepción de mercadería).' },
    ajuste: { title: 'Registrar ajuste', help: 'RESTA unidades del stock (mermas, pérdidas o correcciones). No puede dejar el stock en negativo.' },
};

// El backend devuelve el nivel como texto ("AGOTADO - Reponer…", "CRÍTICO - …", "ADVERTENCIA - …", "NINGUNA - …")
const getLevelCode = nivel => {
    if (nivel.startsWith('AGOTADO')) return 'AGOTADO';
    if (nivel.startsWith('CRÍTICO')) return 'CRÍTICO';
    if (nivel.startsWith('ADVERTENCIA')) return 'ADVERTENCIA';
    return 'NINGUNA';
};

const urgency = nivel =>
    nivel.startsWith('AGOTADO') || nivel.startsWith('CRÍTICO')
        ? 'danger'
        : nivel.startsWith('ADVERTENCIA')
        ? 'warning'
        : 'success';

const movementBadge = tipo =>
    tipo === 'INGRESO' ? 'success' : tipo === 'AJUSTE' ? 'warning' : 'info';

export default function AdminInventoryPage() {
    const { data, registerMovement } = useBackOffice();
    const [draft, setDraft] = useState(null); // { type: 'ingreso' | 'ajuste', productId, cantidad }
    const [busy, setBusy] = useState(false);

    // Filtros del historial de movimientos
    const [search, setSearch] = useState('');
    const [typeFilter, setTypeFilter] = useState('TODOS');
    const [productFilter, setProductFilter] = useState('TODOS');

    // Filtro de alertas por severidad
    const [alertFilter, setAlertFilter] = useState('TODAS');

    const product = draft && data.products.find(p => String(p.id) === String(draft.productId));

    const activeAlerts = useMemo(
        () => data.alerts.filter(a => !a.nivel.startsWith('NINGUNA')),
        [data.alerts]
    );

    const filteredAlerts = useMemo(() => {
        if (alertFilter === 'TODAS') return activeAlerts;
        return activeAlerts.filter(a => getLevelCode(a.nivel) === alertFilter);
    }, [activeAlerts, alertFilter]);

    const movementTypes = useMemo(() => {
        const types = new Set(data.movements.map(m => m.tipo).filter(Boolean));
        return ['TODOS', ...Array.from(types)];
    }, [data.movements]);

    const filteredMovements = useMemo(() => {
        return data.movements.filter(m => {
            const matchesSearch = m.producto?.toLowerCase().includes(search.toLowerCase());
            const matchesType = typeFilter === 'TODOS' || m.tipo === typeFilter;
            const matchesProduct =
                productFilter === 'TODOS' || String(m.productoId ?? m.idProducto) === String(productFilter) || m.producto === productFilter;
            return matchesSearch && matchesType && matchesProduct;
        });
    }, [data.movements, search, typeFilter, productFilter]);

    const hasMovementFilters = search.trim() !== '' || typeFilter !== 'TODOS' || productFilter !== 'TODOS';

    function resetMovementFilters() {
        setSearch('');
        setTypeFilter('TODOS');
        setProductFilter('TODOS');
    }

    async function submit(e) {
        e.preventDefault();
        setBusy(true);
        const ok = await registerMovement(draft.type, draft);
        setBusy(false);
        if (ok) setDraft(null);
    }

    return (
        <>
            <SectionHeader
                icon={Boxes}
                title="Inventario"
                description="Ingresos, ajustes, alertas de stock e historial de movimientos."
                action={
                    <div className="flex flex-wrap gap-3">
                        <Button onClick={() => setDraft({ type: 'ingreso', productId: '', cantidad: '' })}>
                            <Plus size={18} className="mr-2" />
                            Registrar ingreso
                        </Button>
                        <Button variant="outline" onClick={() => setDraft({ type: 'ajuste', productId: '', cantidad: '' })}>
                            <Minus size={18} className="mr-2" />
                            Registrar ajuste
                        </Button>
                    </div>
                }
            />

            <Summary
                items={[
                    ['Productos', data.products.length],
                    ['Unidades en stock', data.products.reduce((sum, p) => sum + p.stock, 0)],
                    ['Alertas de stock', activeAlerts.length],
                    ['Movimientos', data.movements.length],
                ]}
            />

            <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
                <h2 className="text-lg font-semibold">Alertas de stock</h2>
                {activeAlerts.length > 0 && (
                    <div className="flex flex-wrap gap-2">
                        {['TODAS', 'AGOTADO', 'CRÍTICO', 'ADVERTENCIA'].map(level => (
                            <button
                                key={level}
                                type="button"
                                onClick={() => setAlertFilter(level)}
                                className={`rounded-full px-3 py-1 text-xs font-medium transition ${
                                    alertFilter === level
                                        ? 'bg-brand text-white'
                                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                }`}
                            >
                                {level}
                            </button>
                        ))}
                    </div>
                )}
            </div>

            <div className="mb-8 grid gap-3">
                {filteredAlerts.length ? (
                    filteredAlerts.map(a => (
                        <Panel key={a.id}>
                            <div className="flex flex-wrap items-center justify-between gap-3 text-sm">
                                <div className="flex flex-wrap items-center gap-3">
                                    <AlertTriangle className="shrink-0 text-amber-500" size={20} />
                                    <span>
                                        <strong>{a.producto}</strong>: {a.stock} unidades (mínimo {a.umbral}).
                                    </span>
                                    <Badge variant={urgency(a.nivel)}>{a.nivel}</Badge>
                                </div>
                                <button
                                    type="button"
                                    onClick={() =>
                                        setDraft({
                                            type: 'ingreso',
                                            productId: a.productoId ?? a.idProducto ?? '',
                                            cantidad: '',
                                        })
                                    }
                                    className="text-xs font-medium text-brand hover:underline"
                                >
                                    Reponer stock
                                </button>
                            </div>
                        </Panel>
                    ))
                ) : (
                    <Panel>
                        {activeAlerts.length === 0
                            ? 'Todos los productos tienen stock suficiente.'
                            : 'No hay alertas que coincidan con el filtro seleccionado.'}
                    </Panel>
                )}
            </div>

            <h2 className="mb-4 text-lg font-semibold">Historial de movimientos</h2>
            <Panel>
                <div className="mb-6 grid gap-3 md:grid-cols-4">
                    <div className="md:col-span-2">
                        <SearchBox value={search} onChange={setSearch} placeholder="Buscar producto..." />
                    </div>

                    <select
                        aria-label="Filtrar por tipo de movimiento"
                        className="bo-input"
                        value={typeFilter}
                        onChange={e => setTypeFilter(e.target.value)}
                    >
                        {movementTypes.map(type => (
                            <option key={type} value={type}>
                                {type === 'TODOS' ? 'Todos los tipos' : type}
                            </option>
                        ))}
                    </select>

                    <div className="flex gap-2">
                        <select
                            aria-label="Filtrar por producto"
                            className="bo-input w-full"
                            value={productFilter}
                            onChange={e => setProductFilter(e.target.value)}
                        >
                            <option value="TODOS">Todos los productos</option>
                            {data.products.map(p => (
                                <option key={p.id} value={p.id}>
                                    {p.producto}
                                </option>
                            ))}
                        </select>

                        {hasMovementFilters && (
                            <button
                                type="button"
                                onClick={resetMovementFilters}
                                title="Limpiar filtros"
                                className="inline-flex shrink-0 items-center justify-center rounded-lg border border-gray-200 px-3 text-gray-600 transition hover:bg-gray-50"
                            >
                                <RotateCcw size={16} />
                            </button>
                        )}
                    </div>
                </div>

                <DataTable
                    emptyText={
                        hasMovementFilters
                            ? 'No hay movimientos que coincidan con los filtros aplicados.'
                            : 'Todavía no hay movimientos registrados.'
                    }
                    rows={filteredMovements}
                    columns={[
                        { key: 'fecha', label: 'Fecha' },
                        { key: 'producto', label: 'Producto' },
                        {
                            key: 'tipo',
                            label: 'Tipo',
                            render: m => <Badge variant={movementBadge(m.tipo)}>{m.tipo}</Badge>,
                        },
                        { key: 'cantidad', label: 'Cantidad' },
                    ]}
                />
            </Panel>

            {draft && (
                <Modal title={MODES[draft.type].title} onClose={() => setDraft(null)}>
                    <form onSubmit={submit}>
                        <p className="mb-4 text-sm text-gray-600">{MODES[draft.type].help}</p>
                        <div className="grid gap-4">
                            <Field label="Producto">
                                <select
                                    required
                                    name="productId"
                                    className="bo-input"
                                    value={draft.productId}
                                    onChange={e => setDraft({ ...draft, productId: e.target.value })}
                                >
                                    <option value="">Seleccionar producto</option>
                                    {data.products
                                        .filter(p => p.estado === 'Activo')
                                        .map(p => (
                                            <option key={p.id} value={p.id}>
                                                {p.producto}
                                            </option>
                                        ))}
                                </select>
                            </Field>
                            {product && (
                                <p className="text-sm text-gray-500">
                                    Stock actual: <strong>{product.stock}</strong> unidades
                                </p>
                            )}
                            <Field label="Cantidad">
                                <input
                                    required
                                    name="cantidad"
                                    className="bo-input"
                                    type="number"
                                    min="1"
                                    step="1"
                                    max={draft.type === 'ajuste' ? product?.stock : undefined}
                                    value={draft.cantidad}
                                    onChange={e => setDraft({ ...draft, cantidad: e.target.value })}
                                />
                            </Field>
                        </div>
                        <FormActions onClose={() => setDraft(null)} busy={busy} />
                    </form>
                </Modal>
            )}
        </>
    );
}