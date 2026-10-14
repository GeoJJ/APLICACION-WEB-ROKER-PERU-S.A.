export default function Textarea({ icon: Icon, rows = 5, className = "", ...props }) {
    return (
        <div
            className={`flex items-start gap-3 rounded-lg border-2 border-gray-200 bg-white px-4 py-3 focus-within:border-brand ${className}`}
        >
            {Icon && <Icon size={22} className="mt-0.5 shrink-0 text-brand-dark" />}
            <textarea
                rows={rows}
                className="w-full resize-none bg-transparent outline-none placeholder:text-gray-400"
                {...props}
            />
        </div>
    );
}