export default function DataTable({
    columns,
    rows,
    rowKey = "id",
    emptyText = "No hay datos para mostrar",
    className = "",
}) {
    return (
        <div className={`overflow-x-auto ${className}`}>
            <table className="w-full min-w-[600px] border-collapse">
                <thead>
                    <tr className="bg-brand text-white">
                        {columns.map((col) => (
                            <th key={col.key} className="px-4 py-4 text-center font-semibold">
                                {col.label}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {rows.length === 0 ? (
                        <tr>
                            <td
                                colSpan={columns.length}
                                className="border border-gray-100 px-4 py-8 text-center text-gray-400"
                            >
                                {emptyText}
                            </td>
                        </tr>
                    ) : (
                        rows.map((row) => (
                            <tr key={row[rowKey]}>
                                {columns.map((col) => (
                                    <td key={col.key} className="border border-gray-100 px-4 py-4 text-center">
                                        {col.render ? col.render(row) : row[col.key]}
                                    </td>
                                ))}
                            </tr>
                        ))
                    )}
                </tbody>
            </table>
        </div>
    );
}