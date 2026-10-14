export default function Input({ icon: Icon, className = "", ...props }) {
    return (
        <div
            className={`flex items-center gap-3 rounded-lg border-2 border-gray-200 bg-white px-4 py-3 focus-within:border-brand ${className}`}
        >
            {Icon && <Icon size={22} className="shrink-0 text-brand-dark" />}
            <input
                className="w-full bg-transparent outline-none placeholder:text-gray-400"
                {...props}
            />
        </div>
    );
}