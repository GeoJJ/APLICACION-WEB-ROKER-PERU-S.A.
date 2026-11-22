import { Target, Sparkles, BookOpen, ShieldCheck, Star, Handshake } from 'lucide-react';

// Adaptación de la información institucional publicada por https://roker.com.pe/nosotros/.
export const ABOUT_INTRO = 'Roker Perú desarrolla, fabrica y comercializa productos de higiene, desinfección y bioseguridad para instituciones de salud y otros entornos. Su trabajo combina experiencia, innovación y estándares de calidad.';
export const ABOUT_SECTIONS = [
    { id: 'proposito', icon: Target, title: 'Nuestro propósito', text: 'Contribuir a entornos más seguros y saludables mediante soluciones de bioseguridad.' },
    { id: 'cultura', icon: Sparkles, title: 'Nuestra cultura', text: 'Promover la mejora continua, la innovación y el desarrollo tecnológico, con integridad y compromiso con las personas.' },
    { id: 'historia', icon: BookOpen, title: 'Nuestra trayectoria', text: 'Más de tres décadas de experiencia en bioseguridad y Buenas Prácticas de Manufactura desde 2005.' },
];
export const VALUES = [
    { id: 'seguridad', icon: ShieldCheck, title: 'Seguridad', text: 'Priorizar el cuidado de las personas y la protección de los entornos donde viven y trabajan.' },
    { id: 'calidad', icon: Star, title: 'Calidad', text: 'Mejorar continuamente los procesos y ofrecer soluciones confiables.' },
    { id: 'compromiso', icon: Handshake, title: 'Compromiso', text: 'Actuar con integridad y cercanía para construir relaciones de confianza con los clientes.' },
];
