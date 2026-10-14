const VARIANTS = {
    primary: "bg-brand text-white hover:bg-brand/90",
    secondary: "bg-white text-brand-dark hover:bg-gray-100",
    outline: "border-2 border-brand text-brand hover:bg-brand hover:text-white",
};

const SIZES = {
    sm: "px-4 py-2 text-sm",
    md: "px-6 py-2.5 text-base",
    lg: "px-10 py-4 text-lg",
};

export default function Button({
    as: Component = "button",
    variant = "primary",
    size = "md",
    className = "",
    children,
    ...props
}) {
    // Solo un <button> necesita type="button" (evita enviar formularios por accidente)
    const defaults = Component === "button" ? { type: "button" } : {};

    return (
        <Component
            {...defaults}
            className={`inline-flex cursor-pointer items-center justify-center rounded-xl font-semibold shadow-md transition disabled:cursor-not-allowed disabled:opacity-50 ${VARIANTS[variant]} ${SIZES[size]} ${className}`}
            {...props}
        >
            {children}
        </Component>
    );
}