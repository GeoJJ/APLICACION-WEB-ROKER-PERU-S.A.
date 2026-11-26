import { useEffect, useRef, useState } from "react";
import ProductCard from "@/components/features/catalog/ProductCard";
import { catalogCategoryKey, catalogProductCategories } from "@/data/catalogCategories";

function CarouselTrack({ items }) {
    const root = useRef(null);
    const track = useRef(null);
    const metrics = useRef({ step: 1, cycle: 1 });
    const [hovered, setHovered] = useState(false);
    const [focused, setFocused] = useState(false);
    const [interacting, setInteracting] = useState(false);
    const [visible, setVisible] = useState(false);
    const [pageVisible, setPageVisible] = useState(() => document.visibilityState === "visible");
    const [reducedMotion, setReducedMotion] = useState(() => window.matchMedia("(prefers-reduced-motion: reduce)").matches);
    const [canMove, setCanMove] = useState(false);
    const [position, setPosition] = useState(0);
    const [positions, setPositions] = useState(1);

    useEffect(() => {
        const node = track.current;
        let frame;
        const measure = () => {
            cancelAnimationFrame(frame);
            frame = requestAnimationFrame(() => {
                const step = (node.firstElementChild?.getBoundingClientRect().width || 1) + (parseFloat(getComputedStyle(node).columnGap) || 0);
                metrics.current = { step, cycle: items.length * step };
                const moving = items.length * step - (parseFloat(getComputedStyle(node).columnGap) || 0) > node.clientWidth + 2;
                setCanMove(moving);
                setPositions(moving ? items.length : 1);
                setPosition(Math.round(node.scrollLeft / step) % items.length);
            });
        };
        const resize = new ResizeObserver(measure);
        resize.observe(node);
        node.addEventListener("scroll", measure, { passive: true });
        const intersection = new IntersectionObserver(([entry]) => setVisible(entry.isIntersecting), { threshold: 0.25 });
        intersection.observe(root.current);
        const preference = window.matchMedia("(prefers-reduced-motion: reduce)");
        const updatePreference = event => setReducedMotion(event.matches);
        const updateVisibility = () => setPageVisible(document.visibilityState === "visible");
        preference.addEventListener("change", updatePreference);
        document.addEventListener("visibilitychange", updateVisibility);
        [...node.children].slice(items.length).forEach(item => {
            item.querySelectorAll("button, a, input").forEach(control => { control.tabIndex = -1; });
        });
        measure();
        return () => {
            cancelAnimationFrame(frame);
            resize.disconnect();
            node.removeEventListener("scroll", measure);
            intersection.disconnect();
            preference.removeEventListener("change", updatePreference);
            document.removeEventListener("visibilitychange", updateVisibility);
        };
    }, [items.length]);

    useEffect(() => {
        if (hovered || focused || interacting || !visible || !pageVisible || reducedMotion || !canMove) return;
        const node = track.current;
        let frame;
        let previous;
        const advance = time => {
            if (previous !== undefined) {
                const next = node.scrollLeft + Math.min(time - previous, 64) * 0.09;
                node.scrollLeft = next >= metrics.current.cycle ? next - metrics.current.cycle : next;
            }
            previous = time;
            frame = requestAnimationFrame(advance);
        };
        frame = requestAnimationFrame(advance);
        return () => cancelAnimationFrame(frame);
    }, [hovered, focused, interacting, visible, pageVisible, reducedMotion, canMove]);

    return <div ref={root} role="region" aria-roledescription="carrusel" aria-label="Productos destacados"
        onPointerEnter={event => { if (event.pointerType === "mouse") setHovered(true); }} onPointerLeave={event => { if (event.pointerType === "mouse") setHovered(false); setInteracting(false); }}
        onPointerDown={() => setInteracting(true)} onPointerUp={() => setInteracting(false)} onPointerCancel={() => setInteracting(false)}
        onFocusCapture={() => setFocused(true)} onBlurCapture={event => { if (!event.currentTarget.contains(event.relatedTarget)) setFocused(false); }}>
        <ul ref={track} tabIndex={0} aria-label="Lista desplazable de productos destacados" className="flex gap-5 overflow-x-auto px-1 pb-4 pt-1 outline-offset-4 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
            {[...items, ...(canMove ? items.slice(0, 4) : [])].map((product, index) => <li key={`${product.id}-${index}`} aria-hidden={index >= items.length ? "true" : undefined} ref={node => { if (node && index >= items.length) node.querySelectorAll("button, a, input").forEach(control => { control.tabIndex = -1; }); }} className="w-[82%] shrink-0 snap-start sm:w-[calc((100%_-_1.25rem)/2)] lg:w-[calc((100%_-_3.75rem)/4)] [&>div]:h-full"><ProductCard product={product} /></li>)}
        </ul>
        <div className="mt-4 flex flex-wrap justify-center gap-1" role="group" aria-label="Posiciones del carrusel">{Array.from({ length: positions }, (_, index) => <button key={index} type="button" aria-label={`Ir a la posición ${index + 1}`} aria-current={position === index ? "true" : undefined} onClick={() => {
            const node = track.current;
            const step = (node.firstElementChild?.getBoundingClientRect().width || node.clientWidth) + (parseFloat(getComputedStyle(node).columnGap) || 0);
            node.scrollTo({ left: index * step, behavior: reducedMotion ? "instant" : "smooth" });
        }} className="grid h-8 w-6 place-items-center rounded-full"><span className={`h-2.5 rounded-full transition-all ${position === index ? "w-6 bg-brand" : "w-2.5 bg-gray-300"}`} /></button>)}</div>
    </div>;
}

export default function ProductsCarousel({ products }) {
    const [category, setCategory] = useState("");
    const available = products.filter(product => product.available !== false && product.stock !== 0);
    const categories = catalogProductCategories(available);
    const items = available.filter(product => !category || catalogCategoryKey(product.category) === category).slice(0, 12);
    return <div>
        <div role="group" aria-label="Categorías de productos destacados" className="mb-6 flex flex-wrap gap-2">
            {[{ value: "", label: "Todos" }, ...categories].map(option => <button key={option.value} type="button" onClick={() => setCategory(option.value)} aria-pressed={category === option.value}
                className={`rounded-full border px-5 py-2 text-sm font-semibold transition ${category === option.value ? "border-brand bg-brand text-white" : "border-gray-200 bg-white text-gray-600 hover:border-brand hover:text-brand"}`}>{option.label}</button>)}
        </div>
        {items.length ? <CarouselTrack key={category} items={items} /> : <p className="py-8 text-gray-500">No hay productos disponibles en esta categoría.</p>}
    </div>;
}
