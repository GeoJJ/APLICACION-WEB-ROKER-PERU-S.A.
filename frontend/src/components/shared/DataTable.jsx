export default function DataTable({
    columns,
    rows,
    rowKey = "id",
    emptyText = "No hay datos para mostrar",
    className = "",
}) {
    return (
        <div
            className={`w-full overflow-hidden rounded-2xl border border-gray-200/70 bg-white shadow-[0_8px_30px_rgb(0,0,0,0.04)] ${className}`}
        >
            <div className="overflow-x-auto">
                <table className="w-full min-w-[600px] border-collapse text-sm">
                    <thead>
                        <tr className="bg-brand bg-gradient-to-r from-brand via-brand to-brand/90 text-white shadow-sm">
                            {columns.map((col, idx) => (
                                <th
                                    key={col.key}
                                    className={`relative px-6 py-4 text-xs font-semibold uppercase tracking-wider text-white/95 ${
                                        col.align === "left"
                                            ? "text-left"
                                            : col.align === "right"
                                            ? "text-right"
                                            : "text-center"
                                    }`}
                                >
                                    <span>{col.label}</span>
                                    {/* Separador vertical sutil entre cabeceras */}
                                    {idx < columns.length - 1 && (
                                        <span className="absolute right-0 top-1/2 h-4 w-[1px] -translate-y-1/2 bg-white/15" />
                                    )}
                                </th>
                            ))}
                        </tr>
                    </thead>

                    <tbody className="divide-y divide-gray-100/90 bg-white">
                        {rows.length === 0 ? (
                            <tr>
                                <td
                                    colSpan={columns.length}
                                    className="px-6 py-14 text-center"
                                >
                                    <div className="mx-auto flex max-w-xs flex-col items-center justify-center">
                                        <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-brand/10 text-brand">
                                            <svg
                                                className="h-6 w-6"
                                                fill="none"
                                                viewBox="0 0 24 24"
                                                stroke="currentColor"
                                                strokeWidth={1.75}
                                            >
                                                <path
                                                    strokeLinecap="round"
                                                    strokeLinejoin="round"
                                                    d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
                                                />
                                            </svg>
                                        </div>
                                        <p className="text-sm font-semibold text-gray-800">
                                            Sin datos registrados
                                        </p>
                                        <p className="mt-1 text-xs text-gray-400">
                                            {emptyText}
                                        </p>
                                    </div>
                                </td>
                            </tr>
                        ) : (
                            rows.map((row) => (
                                <tr
                                    key={row[rowKey]}
                                    className="group transition-all duration-200 even:bg-gray-50/40 hover:bg-brand/[0.04]"
                                >
                                    {columns.map((col, colIdx) => (
                                        <td
                                            key={col.key}
                                            className={`relative px-6 py-4 text-gray-700 transition-colors group-hover:text-gray-900 ${
                                                col.align === "left"
                                                    ? "text-left"
                                                    : col.align === "right"
                                                    ? "text-right"
                                                    : "text-center"
                                            }`}
                                        >
                                            {/* Indicador lateral activo en hover */}
                                            {colIdx === 0 && (
                                                <span className="absolute bottom-2 left-0 top-2 w-1 scale-y-0 rounded-r-full bg-brand transition-transform duration-200 group-hover:scale-y-100" />
                                            )}

                                            {col.render
                                                ? col.render(row)
                                                : row[col.key]}
                                        </td>
                                    ))}
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}