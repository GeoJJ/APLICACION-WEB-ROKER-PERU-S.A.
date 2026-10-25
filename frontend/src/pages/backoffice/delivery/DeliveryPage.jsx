import { useState } from "react";
import PageTitle from "@/components/shared/PageTitle";
import DeliveryList from "@/components/features/delivery/DeliveryList";
import DeliveryDetail from "@/components/features/delivery/DeliveryDetail";
import { DELIVERIES } from "@/data/backoffice";

export default function DeliveryPage() {
    // La página actúa como orquestadora del estado
    const [activeTab, setActiveTab] = useState("hoy");
    const [selectedDelivery, setSelectedDelivery] = useState(DELIVERIES[0]);

    return (
        <div className="space-y-8">
            <PageTitle as="h1">Mis Entregas Asignadas</PageTitle>

            <DeliveryList
                deliveries={DELIVERIES}
                activeTab={activeTab}
                onTabChange={setActiveTab}
                onSelectDelivery={setSelectedDelivery}
            />

            <DeliveryDetail delivery={selectedDelivery} />
        </div>
    );
}