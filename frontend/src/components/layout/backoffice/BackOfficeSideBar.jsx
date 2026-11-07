import { Link, useLocation } from "react-router-dom";
import { BarChart3, Users, Package, ShoppingCart, FileText, ClipboardList, LogOut } from "lucide-react";

const menuConfig = {
staff: [{to:'/staff',icon:ClipboardList,label:'Mis servicios'}],
    manager: [{to:'/manager/solicitudes',icon:FileText,label:'Solicitudes de servicios'},{to:'/manager/inventario',icon:Package,label:'Inventario'},
        { to: "/manager", icon: BarChart3, label: "Resumen gerencial" },
        { to: "/manager/trabajadores", icon: Users, label: "Trabajadores" },
        { to: "/manager/productos", icon: Package, label: "Productos" },
        { to: "/manager/servicios", icon: ShoppingCart, label: "Servicios" },
        { to: "/manager/reportes", icon: FileText, label: "Reportes" },
        {to:'/manager/proveedores',icon:Package,label:'Proveedores'},
    ],
    warehouse: [
        { to: "/warehouse", icon: BarChart3, label: "Inicio" },
        { to: "/warehouse/productos", icon: Package, label: "Productos" },
        { to: "/warehouse/ingresos", icon: LogOut, label: "Ingresos" },
    ],
    seller: [{to:'/seller/solicitudes',icon:FileText,label:'Solicitudes de servicios'},
        { to: "/seller", icon: BarChart3, label: "Inicio" },
        { to: "/seller/pedidos", icon: ClipboardList, label: "Pedidos" },
        { to: "/seller/ventas", icon: ShoppingCart, label: "Ventas" },
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
        <aside className="border-r border-gray-200 bg-gray-50 md:fixed md:left-0 md:top-16 md:z-40 md:h-[calc(100vh-4rem)] md:w-64">
            <nav className="flex gap-1 overflow-auto px-3 py-4 md:block md:h-full md:space-y-1">
                {menuItems.map((item) => {
                    const isActive = location.pathname === item.to;
                    const Icon = item.icon;
                    return (
                        <Link
                            key={item.to}
                            to={item.to}
                            className={`flex shrink-0 items-center gap-3 rounded-lg px-3 py-3 text-sm font-medium transition ${isActive ? "bg-blue-600 text-white" : "text-gray-700 hover:bg-gray-200"
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
