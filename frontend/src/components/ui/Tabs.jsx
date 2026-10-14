export default function Tabs({ tabs, active, onChange, className = "" }) {
    return (
        <div role="tablist" className={`flex flex-wrap gap-3 ${className}`}>
            {tabs.map((tab) => {
                const isActive = tab.id === active;
                return (
                    <button
                        key={tab.id}
                        type="button"
                        role="tab"
                        aria-selected={isActive}
                        onClick={() => onChange(tab.id)}
                        className={`cursor-pointer rounded-lg border-2 px-5 py-2 text-sm font-semibold transition ${isActive
                                ? "border-brand text-brand"
                                : "border-gray-300 text-gray-500 hover:border-brand hover:text-brand"
                            }`}
                    >
                        {tab.label}
                    </button>
                );
            })}
        </div>
    );
}