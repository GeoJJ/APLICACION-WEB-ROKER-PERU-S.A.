import { Link } from "react-router-dom";
import Button from "@/components/ui/Button";

export default function CtaBanner() {
    return (
        <section className="mx-auto max-w-[1280px] px-6 pb-14">
            <div className="flex flex-col items-start justify-between gap-6 rounded-2xl bg-brand px-8 py-8 text-white md:flex-row md:items-center">
                <div>
                    <h2 className="text-2xl font-semibold md:text-3xl">
                        ¿Necesitas un servicio de limpieza y desinfección?
                    </h2>
                    <p className="mt-1 text-lg">Solicita uno y obtén una atención rápida</p>
                </div>
                <Button as={Link} to="/servicios" variant="secondary" size="lg" className="shrink-0">
                    Solicitar servicio
                </Button>
            </div>
        </section>
    );
}