const SIZES = {
    lg: "text-3xl",
    md: "text-2xl",
};

const WEIGHTS = {
    medium: "font-medium",
    bold: "font-bold",
};

export default function PageTitle({
    as: Tag = "h1",
    size = "lg",
    weight = "medium",
    className = "",
    children,
}) {
    return (
        <Tag className={`text-black ${WEIGHTS[weight]} ${SIZES[size]} ${className}`}>
            {children}
        </Tag>
    );
}