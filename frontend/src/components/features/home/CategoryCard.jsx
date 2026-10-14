import { Link } from "react-router-dom";

export default function CategoryCard({ name, to, image, icon: Icon }) {
    return (
        <Link
            to={to}
            className="block overflow-hidden rounded-2xl border-2 border-gray-100 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-lg"
        >
            <div className="grid h-40 place-items-center bg-gray-100 sm:h-48">
                {image ? (
                    <img src={image} alt="" className="h-full w-full object-contain p-4" />
                ) : (
                    Icon && <Icon size={64} strokeWidth={1.5} className="text-brand" />
                )}
            </div>
            <p className="px-3 py-4 text-center text-lg font-medium">{name}</p>
        </Link>
    );
}