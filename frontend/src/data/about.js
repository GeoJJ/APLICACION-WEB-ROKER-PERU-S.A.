import { Target, Eye, BookOpen, ShieldCheck, Star, Handshake } from "lucide-react";

// TODO: reemplazar los textos de ejemplo por los reales de la empresa
export const ABOUT_INTRO =
    "Escribe aquí una breve presentación de la empresa: a qué se dedica y a quién atiende.";

export const ABOUT_SECTIONS = [
    { id: "mision", icon: Target, title: "Misión", text: "Escribe aquí la misión de la empresa." },
    { id: "vision", icon: Eye, title: "Visión", text: "Escribe aquí la visión de la empresa." },
    { id: "historia", icon: BookOpen, title: "Nuestra historia", text: "Escribe aquí cómo nació la empresa." },
];

export const VALUES = [
    { id: "seguridad", icon: ShieldCheck, title: "Seguridad", text: "Describe cómo la empresa cuida a sus clientes." },
    { id: "calidad", icon: Star, title: "Calidad", text: "Describe el estándar de sus productos y servicios." },
    { id: "compromiso", icon: Handshake, title: "Compromiso", text: "Describe el compromiso con sus clientes." },
];