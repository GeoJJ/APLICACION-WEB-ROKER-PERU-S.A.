const ICON_COLORS = {
    info: "text-brand",
    warning: "text-amber-500",
};

export default function NotificationItem({
    icon: Icon,
    variant = "info",
    time,
    className = "",
    children,
}) {
    return (
        <div
            className={`flex items-center justify-between gap-4 rounded-lg border-2 border-gray-100 bg-white px-4 py-3 ${className}`}
        >
            <p className="flex items-center gap-3 font-medium">
                {Icon && <Icon size={20} className={`shrink-0 ${ICON_COLORS[variant]}`} />}
                {children}
            </p>
            {time && <span className="shrink-0 text-sm text-gray-400">{time}</span>}
        </div>
    );
}