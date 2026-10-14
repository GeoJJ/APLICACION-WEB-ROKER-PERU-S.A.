// Dos <input type="range"> encimados: uno controla el mínimo y otro el máximo.
const INPUT_STYLES =
    "pointer-events-none absolute inset-0 h-4 w-full appearance-none bg-transparent " +
    "[&::-webkit-slider-thumb]:pointer-events-auto [&::-webkit-slider-thumb]:size-4 [&::-webkit-slider-thumb]:cursor-pointer [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-brand " +
    "[&::-moz-range-thumb]:pointer-events-auto [&::-moz-range-thumb]:size-4 [&::-moz-range-thumb]:cursor-pointer [&::-moz-range-thumb]:rounded-full [&::-moz-range-thumb]:border-0 [&::-moz-range-thumb]:bg-brand";

export default function RangeSlider({
    min = 0,
    max = 200,
    step = 10,
    value,
    onChange,
    className = "",
}) {
    const [low, high] = value;
    const percent = (n) => ((n - min) / (max - min)) * 100;

    return (
        <div className={className}>
            <div className="mb-2 flex justify-between text-sm">
                <span>S/.{low}</span>
                <span>S/.{high}</span>
            </div>

            <div className="relative h-4">
                {/* Barra gris de fondo */}
                <div className="absolute top-1/2 h-1 w-full -translate-y-1/2 rounded bg-gray-200" />
                {/* Tramo azul entre las dos bolitas */}
                <div
                    className="absolute top-1/2 h-1 -translate-y-1/2 rounded bg-brand"
                    style={{ left: `${percent(low)}%`, width: `${percent(high) - percent(low)}%` }}
                />

                <input
                    type="range"
                    min={min}
                    max={max}
                    step={step}
                    value={low}
                    onChange={(e) => onChange([Math.min(Number(e.target.value), high - step), high])}
                    className={INPUT_STYLES}
                    aria-label="Precio mínimo"
                />
                <input
                    type="range"
                    min={min}
                    max={max}
                    step={step}
                    value={high}
                    onChange={(e) => onChange([low, Math.max(Number(e.target.value), low + step)])}
                    className={INPUT_STYLES}
                    aria-label="Precio máximo"
                />
            </div>
        </div>
    );
}