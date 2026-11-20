import { Link, useLocation } from "react-router-dom";
import {
    BarChart3,
    Users,
    Package,
    ShoppingCart,
    FileText,
    ClipboardList,
    LogOut,
    UserCog,
    ShieldCheck,
    Boxes,
    Store,
} from "lucide-react";

const menuConfig = {
    staff: [{ to: "/staff", icon: ClipboardList, label: "Mis servicios" }],
    manager: [
        { to: "/manager", icon: BarChart3, label: "Resumen gerencial" },
        { to: "/manager/inventario", icon: Package, label: "Inventario" },
        //{to:'/manager/reportes',icon:FileText,label:'Reportes'},
    ],
    admin: [
        { to: "/admin", icon: BarChart3, label: "Resumen administrativo" },
        { to: "/admin/trabajadores", icon: Users, label: "Trabajadores" },
        { to: "/admin/productos", icon: Package, label: "Productos" },
        { to: "/admin/servicios", icon: ShoppingCart, label: "Servicios" },
        { to: "/admin/proveedores", icon: Package, label: "Proveedores" },
        { to: "/admin/personal", icon: UserCog, label: "Personal de servicio" },
        { to: "/admin/roles", icon: ShieldCheck, label: "Roles" },
        { to: "/admin/inventario", icon: Boxes, label: "Inventario" },
    ],
    warehouse: [
        { to: "/warehouse", icon: BarChart3, label: "Inicio" },
        { to: "/warehouse/productos", icon: Package, label: "Productos" },
        { to: "/warehouse/ingresos", icon: LogOut, label: "Ingresos" },
    ],
    seller: [
        { to: "/seller/solicitudes", icon: FileText, label: "Solicitudes de servicio" },
        { to: "/seller/pedidos", icon: ClipboardList, label: "Pedidos de productos" },
        { to: "/seller/ventas", icon: ShoppingCart, label: "Ventas" },
        { to: "/seller/mostrador", icon: Store, label: "Mostrador y Catálogo" },
    ],
    delivery: [
        { to: "/delivery", icon: BarChart3, label: "Mis entregas" },
        { to: "/delivery/historial", icon: FileText, label: "Historial" },
    ],
};

export default function BackOfficeSidebar({ role = "manager" }) {
    const location = useLocation();
    const menuItems = menuConfig[role] || menuConfig.manager;

    return (
        <aside className="border-r border-gray-200/80 bg-white md:fixed md:left-0 md:top-16 md:z-40 md:h-[calc(100vh-4rem)] md:w-64">
            <nav
                aria-label="Navegación del panel"
                className="flex gap-1.5 overflow-x-auto px-3 py-3 md:flex-col md:gap-1.5 md:px-4 md:pt-8 md:pb-6"
            >
                {menuItems.map((item) => {
                    const isActive = location.pathname === item.to;
                    const Icon = item.icon;

                    return (
                        <Link
                            key={item.to}
                            to={item.to}
                            className={`group relative flex shrink-0 items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium transition-all duration-150 ${isActive
                                    ? "bg-brand text-white shadow-sm"
                                    : "text-gray-600 hover:bg-gray-100/90 hover:text-gray-900"
                                }`}
                        >
                            <Icon
                                size={19}
                                className={`shrink-0 transition-transform duration-150 ${isActive
                                        ? "text-white"
                                        : "text-gray-400 group-hover:scale-105 group-hover:text-brand"
                                    }`}
                            />
                            <span className="truncate">{item.label}</span>
                        </Link>
                    );
                })}
            </nav>
        </aside>
    );
}