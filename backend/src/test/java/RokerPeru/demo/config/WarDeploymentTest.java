package RokerPeru.demo.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("war")
class WarDeploymentTest {
    @Autowired private MockMvc mvc;

    @ParameterizedTest
    @ValueSource(strings = {"/", "/login", "/productos", "/carrito", "/servicios/1/solicitar",
            "/admin/productos", "/manager/reportes", "/seller/pedidos", "/delivery/historial"})
    void recargarUnaRutaReactEntregaElPortal(String ruta) throws Exception {
        mvc.perform(get(ruta)).andExpect(status().isOk()).andExpect(forwardedUrl("/index.html"));
    }

    @Test void elFrontendCompiladoEstaIncluido() throws Exception {
        mvc.perform(get("/index.html")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"root\"")));
    }

    @Test void laApiPublicaSigueDisponibleConPrefijo() throws Exception {
        mvc.perform(get("/api/cliente/productos")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test void abrirUnaPantallaNoAutorizaSuApiPrivada() throws Exception {
        mvc.perform(get("/api/admin/productos")).andExpect(status().isForbidden());
    }
}
