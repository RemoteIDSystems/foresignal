package ai.foresignal.isolation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleRoutingDataSourceTest {

    private static HikariDataSource background() {
        HikariDataSource pool = new HikariDataSource();
        pool.setJdbcUrl("jdbc:postgresql://localhost:1/none");
        pool.setUsername("u");
        pool.setPoolName("main");
        pool.setMaximumPoolSize(10);
        pool.addDataSourceProperty("reWriteBatchedInserts", "true");
        return pool;
    }

    @Test
    @DisplayName("keeps the application's pool for BACKGROUND and copies it, resized, for every other role")
    void poolsPerRole() {
        HikariDataSource main = background();
        try (RoleRoutingDataSource routing = new RoleRoutingDataSource(main, new WorkPoolProperties(3, 7, 2, 1))) {
            assertThat(routing.poolFor(WorkRole.BACKGROUND)).isSameAs(main);
            HikariDataSource api = routing.poolFor(WorkRole.API);
            HikariDataSource collect = routing.poolFor(WorkRole.COLLECT);
            HikariDataSource ingest = routing.poolFor(WorkRole.INGEST);
            assertThat(api.getMaximumPoolSize()).isEqualTo(3);
            assertThat(collect.getMaximumPoolSize()).isEqualTo(7);
            assertThat(ingest.getMaximumPoolSize()).isEqualTo(2);
            assertThat(collect.getMinimumIdle()).isEqualTo(1);
            assertThat(api.getPoolName()).isEqualTo("main-api");
            assertThat(collect.getJdbcUrl()).isEqualTo(main.getJdbcUrl());
            assertThat(collect.getUsername()).isEqualTo("u");
            assertThat(collect.getDataSourceProperties()).containsEntry("reWriteBatchedInserts", "true");
            assertThat(main.getMaximumPoolSize()).isEqualTo(10);
        }
    }

    @Test
    @DisplayName("unwraps to the background pool")
    void unwraps() throws Exception {
        HikariDataSource main = background();
        try (RoleRoutingDataSource routing = new RoleRoutingDataSource(main, WorkPoolProperties.defaults())) {
            assertThat(routing.unwrap(HikariDataSource.class)).isSameAs(main);
            assertThat(routing.unwrap(RoleRoutingDataSource.class)).isSameAs(routing);
            assertThat(routing.isWrapperFor(HikariDataSource.class)).isTrue();
        }
    }

    @Test
    @DisplayName("refuses a pool size below one")
    void validates() {
        assertThatThrownBy(() -> new WorkPoolProperties(0, 20, 4, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foresignal.db.pools.api");
    }
}
