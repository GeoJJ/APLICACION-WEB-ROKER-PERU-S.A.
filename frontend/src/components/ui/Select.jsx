import { useState, useRef, useEffect } from "react";
import { ChevronDown, Check } from "lucide-react";

export default function Select({
    options = [],
    placeholder,
    value = "",
    onChange,
    required = false,
    disabled = false,
    className = "",
    id,
    "aria-labelledby": ariaLabelledBy,
    "aria-label": ariaLabel,
}) {
    const [open, setOpen] = useState(false);
    const containerRef = useRef(null);

    const selectedOption = options.find((opt) => String(opt.value) === String(value));

    useEffect(() => {
        function handleClickOutside(e) {
            if (containerRef.current && !containerRef.current.contains(e.target)) {
                setOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    function handleSelect(newValue) {
        setOpen(false);
        if (onChange) {
            onChange({ target: { value: newValue } });
        }
    }

    return (
        <div ref={containerRef} className={`relative ${className}`}>
            {required && (
                <input
                    tabIndex={-1}
                    autoComplete="off"
                    value={value}
                    onChange={() => {}}
                    required
                    className="pointer-events-none absolute inset-0 h-full w-full opacity-0"
                    aria-hidden="true"
                />
            )}

            <div
                role="combobox"
                id={id}
                aria-labelledby={ariaLabelledBy}
                tabIndex={disabled ? -1 : 0}
                aria-label={ariaLabel}
                aria-expanded={open}
                onClick={() => !disabled && setOpen((prev) => !prev)}
                onKeyDown={(e) => {
                    if (e.key === "Escape" || e.key === "Tab") setOpen(false);
                    if (!disabled && (e.key === "Enter" || e.key === " ")) {
                        e.preventDefault();
                        setOpen((prev) => !prev);
                    }
                }}
                className={`bo-input flex w-full cursor-pointer items-center justify-between gap-2 select-none ${
                    open ? "border-brand ring-2 ring-brand/15" : ""
                } ${disabled ? "cursor-not-allowed bg-gray-50 opacity-60" : ""}`}
            >
                <span className={`truncate ${selectedOption ? "text-gray-700" : "text-gray-400"}`}>
                    {selectedOption ? selectedOption.label : placeholder || "Seleccionar..."}
                </span>
                <ChevronDown
                    size={18}
                    strokeWidth={2.5}
                    className={`shrink-0 text-brand transition-transform duration-200 ${open ? "rotate-180" : ""}`}
                    aria-hidden="true"
                />
            </div>

            {open && (
                <ul
                    role="listbox"
                    className="absolute left-0 z-50 mt-1.5 max-h-60 w-full min-w-[11rem] overflow-auto rounded-lg border border-gray-200 bg-white p-1 text-sm shadow-lg"
                >
                    {placeholder && (
                        <li
                            role="option"
                            aria-selected={value === ""}
                            onClick={() => handleSelect("")}
                            className={`flex cursor-pointer items-center justify-between rounded-md px-3 py-2 transition-colors ${
                                value === ""
                                    ? "bg-brand/10 font-medium text-brand"
                                    : "text-gray-500 hover:bg-gray-50"
                            }`}
                        >
                            <span className="truncate">{placeholder}</span>
                            {value === "" && <Check size={15} className="shrink-0 text-brand" />}
                        </li>
                    )}

                    {options.map((option) => {
                        const isSelected = String(option.value) === String(value);
                        return (
                            <li
                                key={option.value}
                                role="option"
                                aria-selected={isSelected}
                                onClick={() => handleSelect(option.value)}
                                className={`flex cursor-pointer items-center justify-between rounded-md px-3 py-2 transition-colors ${
                                    isSelected
                                        ? "bg-brand/10 font-medium text-brand"
                                        : "text-gray-700 hover:bg-gray-50"
                                }`}
                            >
                                <span className="truncate">{option.label}</span>
                                {isSelected && <Check size={15} className="shrink-0 text-brand" />}
                            </li>
                        );
                    })}
                </ul>
            )}
        </div>
    );
}
