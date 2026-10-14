import PageTitle from "@/components/shared/PageTitle";
import OrderList from "@/components/features/seller/OrderList";
import { useState } from "react";
import { Search } from "lucide-react";

export default function SellerOrdersPage() {
    const [search, setSearch] = useState("");

    return (
        <div className="space-y-8">
            <PageTitle as="h1">Pedidos</PageTitle>

            <div className="relative max-w-md">
                <input
                    type="text"
                    placeholder="Buscar por cliente..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="w-full rounded-xl border-2 border-gray-200 px-4 py-3 pl-4 pr-12 outline-none focus:border-brand"
                />
                <Search size={22} className="absolute right-4 top-1/2 -translate-y-1/2 text-brand" />
            </div>

            <OrderList />
        </div>
    );
}