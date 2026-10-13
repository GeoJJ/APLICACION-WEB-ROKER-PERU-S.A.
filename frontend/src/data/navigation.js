export const NAV_LINKS = [
    { label: "Inicio", to: "/" },
    {
        label: "Productos",
        children: [
            { label: "Línea Hospitalaria", to: "/productos/hospitalaria" },
            { label: "Línea Horeca", to: "/productos/horeca" },
        ],
    },
    { label: "Servicios", to: "/servicios" },
    { label: "Nosotros", to: "/nosotros" },
    { label: "Contacto", href: "#contacto" },
];