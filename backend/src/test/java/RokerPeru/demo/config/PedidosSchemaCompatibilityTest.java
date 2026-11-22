package RokerPeru.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PedidosSchemaCompatibilityTest {
    @Autowired private JdbcTemplate jdbc;

    @Test void elEsquemaActualAdmiteVentaPresencialEnUnaBaseAntigua() {
        // Simula la restricción histórica; la transacción se revierte al terminar la prueba.
        jdbc.execute("ALTER TABLE pedidos ALTER COLUMN id_cliente SET NOT NULL");
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("schema.sql"));
            return null;
        });
        assertThat(jdbc.queryForObject("SELECT is_nullable FROM information_schema.columns "
                + "WHERE table_schema = current_schema() AND table_name = 'pedidos' "
                + "AND column_name = 'id_cliente'", String.class)).isEqualTo("YES");
    }
}
