const SIZES = {
    lg: "text-3xl",
    md: "text-2xl",
};

export default function PageTitle({ as: Tag = "h1", size = "lg", className = "", children }) {
    return (
        <Tag className={`font-medium text-black ${SIZES[size]} ${className}`}>
            {children}
        </Tag>
    );
}