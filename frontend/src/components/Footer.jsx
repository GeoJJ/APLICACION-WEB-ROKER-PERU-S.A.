import { Link } from "react-router-dom";
import logoFooter from "../assets/logo-footer.png";

const COLUMNS = [
  {
    title: "Navegación",
    links: [
      { label: "Inicio", to: "/" },
      { label: "Productos", to: "/productos/hospitalaria" },
      { label: "Servicios", to: "/servicios" },
      { label: "Nosotros", to: "/nosotros" },
      { label: "Contacto", href: "#contacto" },
    ],
  },
  {
    title: "Contáctanos",
    links: [
      { label: "Dirección" },
      { label: "Teléfono" },
      { label: "Email" },
      { label: "Horarios de atención" },
    ],
  },
  {
    title: "Redes",
    links: [
      { label: "Facebook", href: "https://facebook.com" },
      { label: "Instagram", href: "https://instagram.com" },
      { label: "WhatsApp", href: "https://wa.me/" },
    ],
  },
];

export default function Footer() {
  return (
    <footer id="contacto" className="bg-[#020f2e] text-white">
      <div className="mx-auto grid max-w-[1280px] gap-8 px-6 pb-10 pt-9 sm:grid-cols-2 lg:grid-cols-[1.6fr_1fr_1fr_1fr]">
        <div>
          <Link to="/" aria-label="Rocket Peru S.A. - Inicio">
            <img src={logoFooter} alt="Rocket Peru S.A." className="h-16 w-auto" />
          </Link>
          <p className="mt-6 max-w-[360px] text-[13px] leading-normal tracking-wider">
            Protegemos tu espacio y tu tiempo: insumos y limpieza en un solo clic en ROCKET PERU.
          </p>
        </div>

        {COLUMNS.map(({ title, links }) => (
          <div key={title}>
            <h4 className="mb-3.5 text-base font-semibold tracking-wide">{title}</h4>
            <ul className="grid gap-2.5 text-[13px] tracking-wider">
              {links.map(({ label, href, to }) => (
                <li key={label}>
                  {to ? (
                    <Link to={to} className="hover:underline">{label}</Link>
                  ) : href ? (
                    <a href={href} className="hover:underline">{label}</a>
                  ) : (
                    <span>{label}</span>
                  )}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      <div className="border-t border-[#2a3550] bg-[#030a1c] py-2 text-center text-[13px]">
        © {new Date().getFullYear()} Rocket Peru S.A. – Todos los derechos reservados
      </div>
    </footer>
  );
}