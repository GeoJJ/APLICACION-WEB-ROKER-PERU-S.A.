package RokerPeru.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class DemoApplicationTests {
    @Value("${local.server.port}") int port;
    private final HttpClient client = HttpClient.newHttpClient();

    @Test void contextLoads() { assertThat(port).isPositive(); }

    @Test void permitePreflightDelLoginDesdeLasDosDireccionesLocales() throws Exception {
        for (String origin : new String[]{"http://localhost:5173", "http://127.0.0.1:5173"}) {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/roker-backend/auth/login"))
                    .header("Origin", origin).header("Access-Control-Request-Method", "POST")
                    .header("Access-Control-Request-Headers", "content-type")
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).contains(origin);
        }
    }

    @Test void rechazaElPreflightDesdeUnOrigenNoAutorizado() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/roker-backend/auth/login"))
                .header("Origin", "https://no-autorizado.example")
                .header("Access-Control-Request-Method", "POST")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
    }

    @Test void lasRutasAdministrativasSiguenProtegidas() throws Exception {
        var logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(RokerPeru.demo.config.SecurityConfig.class);
        var eventos = new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
        eventos.start();
        logger.addAppender(eventos);
        try {
            var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/roker-backend/admin/usuarios")).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isIn(401, 403);
            assertThat(eventos.list.stream().filter(evento -> evento.getFormattedMessage().contains("Acceso rechazado:")).count()).isEqualTo(1);
        } finally {
            logger.detachAppender(eventos);
            eventos.stop();
        }
    }
}
