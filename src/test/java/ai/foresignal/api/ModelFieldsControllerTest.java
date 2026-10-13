package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code Accept-Encoding} negotiation of the field data (prompt 051): 406 only when gzip is refused. */
class ModelFieldsControllerTest {

    @Test
    @DisplayName("absent, gzip, x-gzip or * accept gzip; gzip;q=0, identity alone or *;q=0 refuse it")
    void acceptsGzip() {
        assertThat(ModelFieldsController.acceptsGzip(null)).isTrue();
        assertThat(ModelFieldsController.acceptsGzip(" ")).isTrue();
        assertThat(ModelFieldsController.acceptsGzip("gzip, deflate, br")).isTrue();
        assertThat(ModelFieldsController.acceptsGzip("x-gzip")).isTrue();
        assertThat(ModelFieldsController.acceptsGzip("br;q=1.0, *;q=0.1")).isTrue();
        assertThat(ModelFieldsController.acceptsGzip("GZIP;Q=0.5")).isTrue();
        assertThat(ModelFieldsController.acceptsGzip("identity")).isFalse();
        assertThat(ModelFieldsController.acceptsGzip("gzip;q=0, *")).isFalse();
        assertThat(ModelFieldsController.acceptsGzip("*;q=0")).isFalse();
        assertThat(ModelFieldsController.acceptsGzip("gzip;q=x")).isFalse();
    }
}
