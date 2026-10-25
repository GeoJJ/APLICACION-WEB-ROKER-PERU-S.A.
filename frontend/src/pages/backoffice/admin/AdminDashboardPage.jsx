import PageTitle from "@/components/shared/PageTitle";
import DashboardStats from "@/components/features/admin/DashboardStats";

export default function AdminDashboardPage() {
    return (
        <div className="space-y-8">
            <PageTitle as="h1">DashBoard</PageTitle>
            <DashboardStats />

            {/* Placeholders para gráficos (como solicitaste) */}
            <div className="grid gap-6 lg:grid-cols-2">
                <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-lg font-semibold">Ventas por Categoría</h2>
                    <img src="https://placehold.co/600x400/f8fafc/1e3a8a?text=Gráfico+de+Pastel" alt="Chart" className="w-full rounded-xl" />
                </div>
                <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
                    <h2 className="mb-4 text-lg font-semibold">Ventas últimos 7 días</h2>
                    <img src="https://placehold.co/600x400/f8fafc/1e3a8a?text=Gráfico+de+Barras" alt="Chart" className="w-full rounded-xl" />
                </div>
            </div>
        </div>
    );
}