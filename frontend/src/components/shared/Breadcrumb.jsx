import { Link } from "react-router-dom";
import { ChevronRight, Home } from "lucide-react";

export default function Breadcrumb({ items }) {
    return (
        <nav aria-label="Ruta de navegación" className="py-4">
            <ol className="flex flex-wrap items-center gap-2 text-sm text-gray-500">
                <li>
                    <Link to="/" className="flex items-center gap-1 hover:text-brand transition">
                        <Home size={16} />
                        <span className="sr-only">Inicio</span>
                    </Link>
                </li>
                {items.map((item, index) => (
                    <li key={index} className="flex items-center gap-2">
                        <ChevronRight size={16} className="text-gray-300" />
                        {item.href ? (
                            <Link to={item.href} className="hover:text-brand transition">
                                {item.label}
                            </Link>
                        ) : (
                            <span className="font-semibold text-gray-900" aria-current="page">
                                {item.label}
                            </span>
                        )}
                    </li>
                ))}
            </ol>
        </nav>
    );
}