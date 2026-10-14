import { useMemo, useState } from "react";
import DataTable from "@/components/shared/DataTable";
import Badge from "@/components/ui/Badge";
import { STOCK_DATA } from "@/data/backoffice";

export default function StockTable() {
    const [search, setSearch] = useState("");

    const filteredData = useMemo(() => {
        return STOCK_DATA.filter((item) =>
            item.producto.toLowerCase().includes(search.toLowerCase())
        );
    }, [search]);

    const columns = [
        { key: "producto", label: "Producto" },
        { key: "categoria", label: "Categoría" },
        { key: "stock", label: "Stock" },
        { key: "umbral", label: "Umbral" },
        {
            key: "estado",
            label: "Estado",
            render: (row) => {
                const variant = row.estado === "En Stock" ? "success" : row.estado === "Stock Bajo" ? "warning" : "danger";
                return <Badge variant={variant}>{row.estado}</Badge>;
            },
        },
    ];

    return (
        <div className="space-y-4">
            <input
                type="text"
                placeholder="Buscar producto..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full max-w-md rounded-xl border-2 border-gray-200 px-4 py-3 outline-none focus:border-brand"
            />
            <DataTable columns={columns} rows={filteredData} />
        </div>
    );
}