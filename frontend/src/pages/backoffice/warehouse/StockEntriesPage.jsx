import { useState } from "react";
import { LogIn, Plus } from "lucide-react";
import { useBackOffice } from "@/components/layout/backoffice/BackOfficeProvider";
import { SectionHeader, Panel, Modal, Field, FormActions, SearchBox } from "@/components/shared/ManagementUI";
import Button from "@/components/ui/Button";
import DataTable from "@/components/shared/DataTable";

export default function StockEntriesPage() {
  const { data, registerMovement } = useBackOffice();
  const [draft, setDraft] = useState(null);
  const [busy, setBusy] = useState(false);
  const [search, setSearch] = useState("");

  const ingresos = data.movements.filter((m) => m.tipo === "INGRESO");

  async function save(e) {
    e.preventDefault();
    setBusy(true);
    const ok = await registerMovement("ingreso", draft);
    setBusy(false);
    if (ok) setDraft(null);
  }

  return (
    <>
      <SectionHeader
        icon={LogIn}
        title="Ingresos de Almacén"
        description="Registra la recepción de mercadería y consulta los movimientos de inventario."
        action={
          <Button onClick={() => setDraft({ productId: "", cantidad: "" })}>
            <Plus size={18} className="mr-2" />
            Registrar ingreso
          </Button>
        }
      />
      <Panel>
        <div className="mb-6">
          <SearchBox value={search} onChange={setSearch} placeholder="Buscar producto..." />
        </div>
        <DataTable
          rows={ingresos.filter((m) => (m.producto?.toLowerCase() || "").includes(search.toLowerCase()))}
          columns={[
            { key: "fecha", label: "Fecha" },
            { key: "producto", label: "Producto" },
            { key: "cantidad", label: "Unidades" },
            { key: "observaciones", label: "Detalle" },
          ]}
          emptyText="Todavía no hay ingresos registrados."
        />
      </Panel>
      {draft && (
        <Modal title="Registrar ingreso" onClose={() => setDraft(null)}>
          <form onSubmit={save}>
            <div className="grid gap-4">
              <Field label="Producto">
                <select
                  required
                  className="bo-input"
                  value={draft.productId}
                  onChange={(e) => setDraft({ ...draft, productId: e.target.value })}
                >
                  <option value="">Seleccionar producto</option>
                  {data.products
                    .filter((p) => p.estado === "Activo")
                    .map((p) => (
                      <option key={p.id} value={p.id}>{p.producto}</option>
                    ))}
                </select>
              </Field>
              <Field label="Cantidad recibida">
                <input
                  className="bo-input"
                  required
                  type="number"
                  min="1"
                  step="1"
                  value={draft.cantidad}
                  onChange={(e) => setDraft({ ...draft, cantidad: e.target.value })}
                />
              </Field>
            </div>
            <FormActions onClose={() => setDraft(null)} busy={busy} />
          </form>
        </Modal>
      )}
    </>
  );
}