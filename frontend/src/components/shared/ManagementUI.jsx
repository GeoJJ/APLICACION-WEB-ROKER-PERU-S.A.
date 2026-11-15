import { useBackOffice } from '@/components/layout/backoffice/BackOfficeProvider';
import { useEffect, useRef, useId, isValidElement, cloneElement } from 'react';
import { X, Search } from 'lucide-react';
import Button from '@/components/ui/Button';

export function SectionHeader({ icon: Icon, title, description, action }) {
  return <div className="mb-8 flex flex-wrap items-start justify-between gap-4"><div><h1 className="flex items-center gap-3 text-2xl font-bold text-gray-900"><Icon className="shrink-0 text-blue-600" size={30} />{title}</h1><p className="mt-2 text-sm text-gray-500">{description}</p></div>{action}</div>;
}
export function Panel({ children, className = '' }) { return <div className={`rounded-xl border border-gray-200 bg-white p-6 shadow-sm ${className}`}>{children}</div>; }
export function Field({ label, children }) {
  const id = useId();
  const control = isValidElement(children) && ['input', 'select', 'textarea'].includes(children.type);
  return <div className="grid gap-2 text-sm font-medium text-gray-700">{control ? <label htmlFor={id}>{label}</label> : <span>{label}</span>}{control ? cloneElement(children, { id }) : children}</div>;
}
export function SearchBox({ value, onChange, placeholder = 'Buscar...' }) { return <div className="relative w-full sm:max-w-md"><Search size={19} className="absolute left-3 top-3.5 text-brand" /><input className="bo-input pl-10" aria-label={placeholder} placeholder={placeholder} value={value} onChange={e => onChange(e.target.value)} /></div>; }
export function Modal({ title, children, onClose }) {
  const { notice, setNotice } = useBackOffice();
  const ref = useRef(null);
  useEffect(() => { setNotice(''); const dialog = ref.current; dialog.showModal(); return () => dialog.close(); }, [setNotice]);
  return <dialog ref={ref} onCancel={onClose} className="m-auto max-h-[90vh] w-[min(94vw,700px)] rounded-2xl border border-gray-200 bg-white p-6 shadow-xl backdrop:bg-slate-950/40"><div className="mb-6 flex items-center justify-between gap-4"><h2 className="text-xl font-semibold">{title}</h2><button type="button" onClick={onClose} aria-label="Cerrar" className="rounded-lg p-2 hover:bg-gray-100"><X size={22} /></button></div>{notice && <p role="status" className="mb-4 rounded-lg bg-blue-50 p-3 text-sm text-brand">{notice}</p>}{children}</dialog>;
}
export function FormActions({ onClose }) { const {readOnly}=useBackOffice(); return <div className="mt-6 flex justify-end gap-3"><Button variant="outline" onClick={onClose}>Cancelar</Button><Button type="submit" disabled={readOnly}>Guardar</Button></div>; }
export function Summary({ items }) { return <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{items.map(([label, value]) => <Panel key={label}><p className="text-xs font-semibold uppercase tracking-wide text-gray-500">{label}</p><p className="mt-3 text-2xl font-bold text-brand">{value}</p></Panel>)}</div>; }
