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
}