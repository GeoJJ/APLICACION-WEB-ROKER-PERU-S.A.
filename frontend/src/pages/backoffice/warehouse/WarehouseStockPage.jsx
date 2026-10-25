import PageTitle from "@/components/shared/PageTitle";
import StockTable from "@/components/features/warehouse/StockTable";
import { STOCK_ALERTS } from "@/data/backoffice";
import { AlertTriangle } from "lucide-react";

export default function WarehouseStockPage() {
    return (
        <div className="space-y-8">
            <PageTitle as="h1">Stock de Productos</PageTitle>
            <StockTable />

            <div>
                <h2 className="mb-4 text-lg font-semibold">Alertas de Stock Bajo</h2>
                <div className="space-y-3">
                    {STOCK_ALERTS.map((alert) => (
                        <div key={alert.id} className="flex items-center justify-between rounded-xl border border-gray-200 bg-white px-6 py-4 shadow-sm">
                            <div className="flex items-center gap-3">
                                <AlertTriangle size={20} className="text-amber-500" />
                                <span className="text-sm font-medium text-gray-900">{alert.mensaje}</span>
                            </div>
                            <span className="text-sm text-gray-500">{alert.tiempo}</span>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}