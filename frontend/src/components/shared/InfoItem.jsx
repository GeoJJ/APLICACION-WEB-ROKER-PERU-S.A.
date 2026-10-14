export default function InfoItem({ icon: Icon, title, circle = true, className = "", children }) {
    return (
        <div className={`flex items-start gap-4 ${className}`}>
            {Icon && (
                <span
                    className={`grid shrink-0 place-items-center text-brand ${circle ? "size-12 rounded-full bg-gray-200" : "size-10"
                        }`}
                >
                    <Icon size={circle ? 22 : 28} />
                </span>
            )}
            <div>
                <h3 className="text-lg font-semibold text-brand-dark">{title}</h3>
                <div className="text-sm text-gray-700">{children}</div>
            </div>
        </div>
    );
}