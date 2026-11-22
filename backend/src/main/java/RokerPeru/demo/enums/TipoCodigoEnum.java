package RokerPeru.demo.enums;

/**
 * PRD-0001 (producto), PED-0001 (pedido), SRV-0001 (servicio), ORD-0001 (orden de servicio).
 */
public enum TipoCodigoEnum {

    PRODUCTO("PRD", "producto", "codigo_producto"),
    PEDIDO("PED", "pedidos", "codigo_pedido"),
    SERVICIO("SRV", "servicios", "codigo_servicio"),
    ORDEN_SERVICIO("ORD", "ordenes_servicio", "codigo_orden_servicio");

    private final String prefijo;
    private final String tabla;
    private final String columna;

    TipoCodigoEnum(String prefijo, String tabla, String columna) {
        this.prefijo = prefijo;
        this.tabla = tabla;
        this.columna = columna;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public String getTabla() {
        return tabla;
    }

    public String getColumna() {
        return columna;
    }
}