package ai.foresignal.isolation;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Replaces the application's Hikari {@code DataSource} bean with a {@link RoleRoutingDataSource} built around it,
 * so every pool is kept apart by {@link WorkRole} without replacing Spring Boot's data source configuration.
 *
 * <p>It runs after Spring Boot has configured the pool (its properties and any {@code @ServiceConnection}) and
 * before anything takes a connection. The sizes are read straight from the environment
 * ({@link WorkPoolProperties}), because a bean post-processor is created before ordinary beans.
 */
@Component
public class RoleRoutingDataSourcePostProcessor implements BeanPostProcessor, EnvironmentAware, Ordered {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /** Wraps the Hikari data source; leaves every other bean alone. */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof HikariDataSource hikari && !(bean instanceof RoleRoutingDataSource)) {
            WorkPoolProperties sizes = Binder.get(environment)
                    .bindOrCreate("foresignal.db.pools", Bindable.of(WorkPoolProperties.class));
            return new RoleRoutingDataSource(hikari, sizes);
        }
        return bean;
    }

    /** Last, so every other post-processor has configured the pool first. */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
