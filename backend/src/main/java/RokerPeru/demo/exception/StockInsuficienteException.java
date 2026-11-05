package RokerPeru.demo.exception;

public class StockInsuficienteException extends AppException {
    private final int stockDisponible;
    private final int cantidadSolicitada;

    public StockInsuficienteException(String producto, int stockDisponible, int cantidadSolicitada){
        super("Stock insuficiente para '" + producto + "': disponible " + stockDisponible
                + ", solicitado " + cantidadSolicitada);
        this.stockDisponible = stockDisponible;
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public int getStockDisponible() { return stockDisponible; }
    public int getCantidadSolicitada() { return cantidadSolicitada; }
}
