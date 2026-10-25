import { ChevronDown } from "lucide-react";

export default function Select({ options = [], placeholder, className = "", ...props }) {
    return (
        <div className={`relative ${className}`}>
            <select
                className="w-full cursor-pointer appearance-none rounded-lg border-2 border-gray-200 bg-white px-4 py-3 pr-12 outline-none focus:border-brand"
                {...props}
            >
                {placeholder && <option value="">{placeholder}</option>}
                {options.map((option) => (
                    <option key={option.value} value={option.value}>
                        {option.label}
                    </option>
                ))}
            </select>
            <ChevronDown
                size={22}
                strokeWidth={3}
                className="pointer-events-none absolute right-4 top-1/2 -translate-y-1/2 text-brand"
                aria-hidden="true"
            />
        </div>
    );
}