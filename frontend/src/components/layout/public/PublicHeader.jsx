import { useState } from "react";
import { Link, NavLink } from "react-router-dom";
import { Search, ShoppingCart, CircleUser, Menu, X } from "lucide-react";
import logo from "@/assets/logos/logo.png";
import { NAV_LINKS } from "@/data/navigation";

const linkClass = ({ isActive }) =>
  `hover:text-[#00468c] ${isActive ? "font-medium text-[#00468c]" : ""}`;

export default function PublicHeader({ cartCount = 0 }) {
  const [open, setOpen] = useState(false);
  const close = () => setOpen(false);

  return (
    <header className="sticky top-0 z-20 bg-white">
      <div className="flex items-center justify-between gap-6 px-6 py-3.5">
        <Link to="/" aria-label="Rocket Peru S.A. - Inicio">
          <img src={logo} alt="Rocket Peru S.A." className="h-[52px] w-auto" />
        </Link>

        {/* Menú escritorio */}
        <nav className="hidden items-center gap-8 text-[17px] md:flex lg:gap-[52px]" aria-label="Principal">
          {NAV_LINKS.map((item) =>
            item.children ? (
              <div key={item.label} className="group relative">
                <span className="cursor-pointer hover:text-[#00468c]">{item.label}</span>
                <div className="invisible absolute left-0 top-full min-w-[200px] rounded-xl bg-white py-2 opacity-0 shadow-lg transition group-hover:visible group-hover:opacity-100">
                  {item.children.map((c) => (
                    <NavLink key={c.to} to={c.to} className={(s) => `block px-4 py-2 ${linkClass(s)}`}>
                      {c.label}
                    </NavLink>
                  ))}
                </div>
              </div>
            ) : item.to ? (
              <NavLink key={item.label} to={item.to} end={item.to === "/"} className={linkClass}>
                {item.label}
              </NavLink>
            ) : (
              <a key={item.label} href={item.href} className="hover:text-[#00468c]">{item.label}</a>
            )
          )}
        </nav>

        <div className="flex items-center gap-4 md:gap-6">
          <button aria-label="Buscar"><Search size={26} /></button>
          <button aria-label="Carrito" className="relative">
            <ShoppingCart size={26} />
            {cartCount > 0 && (
              <span className="absolute -right-2 -top-1.5 grid h-[18px] min-w-[18px] place-items-center rounded-full bg-[#00468c] px-1 text-[11px] font-semibold text-white">
                {cartCount}
              </span>
            )}
          </button>
          <button aria-label="Mi cuenta" className="text-[#00468c]"><CircleUser size={30} /></button>
          <button
            className="md:hidden"
            aria-label={open ? "Cerrar menú" : "Abrir menú"}
            aria-expanded={open}
            onClick={() => setOpen(!open)}
          >
            {open ? <X size={28} /> : <Menu size={28} />}
          </button>
        </div>
      </div>

      {/* Menú móvil */}
      {open && (
        <nav className="border-t border-gray-100 bg-white shadow-lg md:hidden" aria-label="Principal móvil">
          {NAV_LINKS.map((item) =>
            item.children ? (
              <div key={item.label} className="border-b border-gray-100">
                <p className="px-6 pt-4 text-gray-500">{item.label}</p>
                {item.children.map((c) => (
                  <Link key={c.to} to={c.to} onClick={close} className="block px-10 py-3">{c.label}</Link>
                ))}
              </div>
            ) : item.to ? (
              <Link key={item.label} to={item.to} onClick={close} className="block border-b border-gray-100 px-6 py-4">
                {item.label}
              </Link>
            ) : (
              <a key={item.label} href={item.href} onClick={close} className="block border-b border-gray-100 px-6 py-4">
                {item.label}
              </a>
            )
          )}
        </nav>
      )}
    </header>
  );
}