import Tabs from "@/components/ui/Tabs";
import DataTable from "@/components/shared/DataTable";
import Badge from "@/components/ui/Badge";
import Button from "@/components/ui/Button";

export default function DeliveryList({ deliveries, activeTab, onTabChange, onSelectDelivery }) {
    const tabs = [
        { id: "hoy", label: "Hoy (5)" },
        { id: "pendientes", label: "Pendientes (5)" },
        { id: "completados", label: "Completados (5)" },
    ];

    const columns = [
        { key: "id", label: "Pedido" },
        { key: "cliente", label: "Cliente" },
        { key: "direccion", label: "Dirección" },
        {
            key: "estado",
            label: "Estado",
            render: (row) => {
                const variant = row.estado === "Entregado" ? "success" : row.estado === "En camino" ? "info" : "warning";
                return <Badge variant={variant}>{row.estado}</Badge>;
            },
        },
        {
            key: "accion",
            label: "Acción",
            render: (row) => (
                <Button size="sm" variant="outline" onClick={() => onSelectDelivery(row)}>
                    Ver
                </Button>
            ),
        },
    ];

    // Filtrado simple para la demo (en producción vendría del backend)
    const filteredDeliveries = deliveries.filter((d) => d.tab === activeTab || activeTab === "todos");

    return (
        <div className="space-y-6">
            <Tabs tabs={tabs} active={activeTab} onChange={onTabChange} />
            <DataTable columns={columns} rows={filteredDeliveries} />
        </div>
    );
}