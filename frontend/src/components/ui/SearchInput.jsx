import { Search } from "lucide-react";
export default function SearchInput({ className = "", ...props }) {
    return (
        <div
            className={`flex items-center gap-3 rounded-lg border-2 border-gray-200 bg-white px-4 py-3 focus-within:border-brand ${className}`}
        >
            <input
                type="text"
                className="w-full bg-transparent outline-none placeholder:text-gray-400"
                {...props}
            />
            <Search size={24} className="shrink-0 text-brand" aria-hidden="true" />
        </div>
    );
}