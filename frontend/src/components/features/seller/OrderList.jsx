import DataTable from "@/components/shared/DataTable";
import Badge from "@/components/ui/Badge";
import { SELLER_ORDERS } from "@/data/backoffice";

export default function OrderList() {
    const columns = [
        { key: "id", label: "Pedido" },
        { key: "cliente", label: "Cliente" },
        { key: "tipo", label: "Tipo" },
        { key: "total", label: "Total" },
        {
            key: "estado",
            label: "Estado",
            render: (row) => {
                const variant = row.estado === "Completado" ? "success" : row.estado === "En camino" ? "info" : "warning";
                return <Badge variant={variant}>{row.estado}</Badge>;
            },
        },
    ];

    return <DataTable columns={columns} rows={SELLER_ORDERS} />;
}