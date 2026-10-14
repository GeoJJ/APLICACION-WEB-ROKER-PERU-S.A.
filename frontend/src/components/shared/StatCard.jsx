import Card from "@/components/ui/Card";

export default function StatCard({ label, value, change, className = "" }) {
    const isNegative = change?.startsWith("-");

    return (
        <Card className={`p-5 ${className}`}>
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-500">{label}</p>
            <p className="mt-3 text-3xl font-bold text-black">{value}</p>
            {change && (
                <p className={`mt-2 font-semibold ${isNegative ? "text-red-600" : "text-brand"}`}>
                    {change}
                </p>
            )}
        </Card>
    );
}