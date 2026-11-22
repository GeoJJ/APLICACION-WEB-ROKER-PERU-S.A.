package RokerPeru.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Separa la API de las rutas de React al servir ambas desde Tomcat. */
@Configuration
@Profile("war")
public class WarWebConfig implements WebMvcConfigurer {
    public static final String[] RUTAS_REACT = {
            "/", "/login", "/register", "/productos", "/servicios", "/servicios/{id}/solicitar",
            "/nosotros", "/contacto", "/carrito", "/perfil", "/mis-solicitudes",
            "/manager", "/manager/inventario", "/manager/reportes",
            "/admin", "/admin/trabajadores", "/admin/productos", "/admin/servicios",
            "/admin/proveedores", "/admin/personal", "/admin/roles", "/admin/inventario",
            "/warehouse", "/warehouse/productos", "/warehouse/ingresos",
            "/seller", "/seller/solicitudes", "/seller/pedidos", "/seller/ventas", "/seller/mostrador",
            "/delivery", "/delivery/historial", "/staff"
    };

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", tipo -> tipo.getPackageName().equals("RokerPeru.demo.controller"));
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (String ruta : RUTAS_REACT) {
            registry.addViewController(ruta).setViewName("forward:/index.html");
        }
    }
}
