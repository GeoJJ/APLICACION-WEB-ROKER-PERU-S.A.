package RokerPeru.demo.enums;

public enum TipoComprobanteEnum {

    // Series para productos (Pedidos)
    BOLETA_PRODUCTO("B001"),
    FACTURA_PRODUCTO("F001"),

    // Series para servicios (Órdenes de Servicio)
    BOLETA_SERVICIO("B002"),
    FACTURA_SERVICIO("F002");

    private final String serie;

    TipoComprobanteEnum(String serie) {
        this.serie = serie;
    }

    public String getSerie() {
        return serie;
    }

    // Las series B002 y F002 se usan solo en órdenes de servicio; B001 y F001 solo en pedidos
    public boolean esServicio() {
        return this == BOLETA_SERVICIO || this == FACTURA_SERVICIO;
    }
}