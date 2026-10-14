const VARIANTS = {
    info: "bg-brand/15 text-brand",
    success: "bg-green-100 text-green-700",
    warning: "bg-amber-100 text-amber-700",
    danger: "bg-red-100 text-red-700",
};

export default function Badge({ variant = "info", className = "", children }) {
    return (
        <span
            className={`inline-flex items-center rounded-md px-3 py-1 text-xs font-semibold ${VARIANTS[variant]} ${className}`}
        >
            {children}
        </span>
    );
}