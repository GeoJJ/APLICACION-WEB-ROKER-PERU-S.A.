import { Link, useLocation } from "react-router-dom";
import { BarChart3, Users, Package, ShoppingCart, FileText, ClipboardList, LogOut } from "lucide-react";

const menuConfig = {
    admin: [
        { to: "/admin", icon: BarChart3, label: "DashBoard" },
        { to: "/admin/trabajadores", icon: Users, label: "Trabajadores" },
        { to: "/admin/productos", icon: Package, label: "Productos" },
        { to: "/admin/servicios", icon: ShoppingCart, label: "Servicios" },
        { to: "/admin/reportes", icon: FileText, label: "Reportes" },
    ],
    warehouse: [
        { to: "/warehouse", icon: BarChart3, label: "Inicio" },
        { to: "/warehouse/productos", icon: Package, label: "Productos" },
        { to: "/warehouse/ingresos", icon: LogOut, label: "Ingresos" },
    ],
    seller: [
        { to: "/seller", icon: BarChart3, label: "Inicio" },
        { to: "/seller/pedidos", icon: ClipboardList, label: "Pedidos" },
        { to: "/seller/ventas", icon: ShoppingCart, label: "Ventas" },
    ],
    delivery: [
        { to: "/delivery", icon: BarChart3, label: "Mis entregas" },
        { to: "/delivery/historial", icon: FileText, label: "Historial" },
    ],
};

export default function BackOfficeSidebar({ role = "admin" }) {
    const location = useLocation();
    const menuItems = menuConfig[role] || menuConfig.admin;

    return (
        <aside className="fixed left-0 top-0 z-40 h-screen w-64 border-r border-gray-200 bg-gray-50">
            <div className="h-16" /> {/* Espacio para el header */}
            <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4">
                {menuItems.map((item) => {
                    const isActive = location.pathname === item.to;
                    const Icon = item.icon;
                    return (
                        <Link
                            key={item.to}
                            to={item.to}
                            className={`flex items-center gap-3 rounded-lg px-3 py-3 text-sm font-medium transition ${isActive ? "bg-blue-600 text-white" : "text-gray-700 hover:bg-gray-200"
                                }`}
                        >
                            <Icon size={20} />
                            {item.label}
                        </Link>
                    );
                })}
            </nav>
        </aside>
    );
}