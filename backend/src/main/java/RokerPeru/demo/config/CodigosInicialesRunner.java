package RokerPeru.demo.config;

import RokerPeru.demo.logging.RegistroOperacion;
import RokerPeru.demo.entities.OrdenesServicio;
import RokerPeru.demo.entities.Pedidos;
import RokerPeru.demo.entities.Producto;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.enums.TipoCodigoEnum;
import RokerPeru.demo.repository.OrdenesServicioRepository;
import RokerPeru.demo.repository.PedidosRepository;
import RokerPeru.demo.repository.ProductoRepository;
import RokerPeru.demo.repository.ServicioRepository;
import RokerPeru.demo.services.CodigoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Al arrancar, asigna código a los registros que ya existían antes de esta mejora (productos sin código,
 * pedidos, servicios y órdenes de servicio), en orden de creación. Si no hay nada pendiente no hace nada,
 * así que puede ejecutarse en cada arranque sin riesgo. Los productos que ya tienen un código manual
 * (por ejemplo "MASC-001") se respetan tal cual.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodigosInicialesRunner implements ApplicationRunner {

    private final ProductoRepository productoRepository;
    private final PedidosRepository pedidosRepository;
    private final ServicioRepository servicioRepository;
    private final OrdenesServicioRepository ordenesRepository;
    private final CodigoService codigoService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> {
            asignar(productoRepository.findSinCodigo(), TipoCodigoEnum.PRODUCTO, Producto::setCodigoProducto);
            asignar(pedidosRepository.findByCodigoPedidoIsNullOrderByIdPedidoAsc(), TipoCodigoEnum.PEDIDO, Pedidos::setCodigoPedido);
            asignar(servicioRepository.findByCodigoServicioIsNullOrderByIdServicioAsc(), TipoCodigoEnum.SERVICIO, Servicio::setCodigoServicio);
            asignar(ordenesRepository.findByCodigoOrdenServicioIsNullOrderByIdOrdenServicioAsc(), TipoCodigoEnum.ORDEN_SERVICIO, OrdenesServicio::setCodigoOrdenServicio);
        });
    }

    private <T> void asignar(List<T> pendientes, TipoCodigoEnum tipo, BiConsumer<T, String> setter) {
        if (pendientes.isEmpty()) {
            return;
        }
        for (T registro : pendientes) {
            setter.accept(registro, codigoService.generar(tipo));
        }
        RegistroOperacion.infoConfirmada(log, "Códigos {} asignados a {} registros existentes", tipo.getPrefijo(), pendientes.size());
    }
}