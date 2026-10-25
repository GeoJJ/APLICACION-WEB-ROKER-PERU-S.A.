import { Link } from "react-router-dom";
import banner from "@/assets/images/banner.png";
import Button from "@/components/ui/Button";

export default function HeroSection() {
    return (
        <section className="relative overflow-hidden bg-brand-dark">
            {/* Imagen de fondo (decorativa, por eso alt vacío) */}
            <img
                src={banner}
                alt=""
                className="absolute inset-0 h-full w-full object-cover object-right"
            />
            {/* Capa oscura solo en móvil, para que el texto se lea sobre la foto */}
            <div className="absolute inset-0 bg-brand-dark/60 md:hidden" />

            <div className="relative mx-auto flex min-h-[480px] max-w-[1280px] flex-col justify-center px-6 pb-16 pt-10">
                <h1 className="text-4xl font-semibold leading-tight text-white md:text-6xl">
                    Protegemos lo{" "}
                    <br className="hidden md:block" />
                    que más importa
                </h1>
                <p className="mt-6 max-w-xl text-lg text-white md:text-2xl">
                    Productos de bioseguridad y servicios de limpieza y desinfección para hogares y empresas
                </p>
                <Button as={Link} to="/productos" size="lg" className="mt-8 self-start">
                    Ver catálogo
                </Button>
            </div>
        </section>
    );
}