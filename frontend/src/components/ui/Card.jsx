export default function Card({ className = "", children, ...props }) {
    return (
        <div
            className={`rounded-2xl border-2 border-gray-100 bg-white shadow-sm ${className}`}
            {...props}
        >
            {children}
        </div>
    );
}