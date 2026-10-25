import StatCard from "@/components/shared/StatCard";
import { ADMIN_STATS } from "@/data/backoffice";

export default function DashboardStats() {
    return (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
            {ADMIN_STATS.map((stat, index) => (
                <StatCard key={index} label={stat.label} value={stat.value} change={stat.change} />
            ))}
        </div>
    );
}