package ai.foresignal.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ScopeTest {

    @Test
    @DisplayName("scopes are ordered read < agent < admin")
    void isOrdered() {
        assertThat(Scope.READ.ordinal()).isLessThan(Scope.AGENT.ordinal());
        assertThat(Scope.AGENT.ordinal()).isLessThan(Scope.ADMIN.ordinal());
    }

    @ParameterizedTest(name = "{0} includes {1} -> {2}")
    @CsvSource({
        "READ, READ, true",
        "READ, AGENT, false",
        "READ, ADMIN, false",
        "AGENT, READ, true",
        "AGENT, AGENT, true",
        "AGENT, ADMIN, false",
        "ADMIN, READ, true",
        "ADMIN, AGENT, true",
        "ADMIN, ADMIN, true",
    })
    void includesIsMonotonic(Scope held, Scope required, boolean expected) {
        assertThat(held.includes(required)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"READ, read", "AGENT, agent", "ADMIN, admin"})
    void wireValueIsLowercase(Scope scope, String expected) {
        assertThat(scope.wireValue()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"read", "READ", " Read ", "agent", "ADMIN"})
    void parsesWireValues(String value) {
        assertThat(Scope.fromWireValue(value)).isNotNull();
    }

    @Test
    void roundTripsThroughWireValue() {
        for (Scope scope : Scope.values()) {
            assertThat(Scope.fromWireValue(scope.wireValue())).isEqualTo(scope);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "owner", "superuser", "write"})
    void rejectsUnknownScopes(String value) {
        assertThatThrownBy(() -> Scope.fromWireValue(value))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullScope() {
        assertThatThrownBy(() -> Scope.fromWireValue(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
