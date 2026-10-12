package ai.foresignal.trigger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link OperatorRegistry} and the value helpers of {@link Operators} (prompt 017). */
class OperatorRegistryTest {

    /** A stand-in for prompt 036's watchlist operator. */
    static final Operator EXTRA = new Operator() {
        @Override
        public String name() {
            return "inWatchlist";
        }

        @Override
        public Object normalize(Object value) {
            return value;
        }

        @Override
        public String sql(SqlTerm term, Object value) {
            return "false";
        }

        @Override
        public Evaluation evaluate(Object actual, Object value) {
            return Evaluation.NO_MATCH;
        }
    };

    @Test
    @DisplayName("the built-ins are registered in spec order, and a bean adds an operator")
    void registry() {
        assertThat(OperatorRegistry.builtIn().names())
                .containsExactly(">", ">=", "<", "<=", "==", "!=", "in", "notIn", "exists", "startsWith", "matches");
        OperatorRegistry extended = new OperatorRegistry(List.of(EXTRA));
        assertThat(extended.find("inWatchlist")).contains(EXTRA);
        assertThat(extended.find(null)).isEmpty();
        assertThat(extended.find("gt")).isEmpty();
    }

    @Test
    @DisplayName("an operator may not reuse a name")
    void duplicate() {
        Operator clash = new Operators.Compare(">");
        assertThatThrownBy(() -> new OperatorRegistry(List.of(clash))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("numbers normalize to decimals; non-finite numbers and booleans are not numbers")
    void numbers() {
        assertThat(Operators.number(6)).isEqualTo(new BigDecimal("6"));
        assertThat(Operators.number(6.1)).isEqualTo(new BigDecimal("6.1"));
        assertThat(Operators.number(7L)).isEqualTo(new BigDecimal("7"));
        assertThat(Operators.number(java.math.BigInteger.TEN)).isEqualTo(BigDecimal.TEN);
        assertThat(Operators.number(Double.NaN)).isNull();
        assertThat(Operators.number(true)).isNull();
        assertThat(Operators.number("6")).isNull();
        assertThat(Operators.scalarType(List.of())).isNull();
    }
}
