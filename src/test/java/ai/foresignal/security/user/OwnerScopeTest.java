package ai.foresignal.security.user;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.restrict.AlertAudience;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertView;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link OwnerScope}, {@link AlertAudience} and the audience inside {@link ReadRestriction} (prompt R2-008). */
class OwnerScopeTest {

    static final String ALICE = "01ALICE";
    static final String BOB = "01BOB";

    private static AlertView alert(String owner) {
        Instant t = Instant.parse("2026-05-01T12:00:00Z");
        return new AlertView(1, UUID.randomUUID(), "R", 10, "src", "e1", "k", t, t, "m", "warning", "firing", 1, null,
                null, null, null, null, null, null, null, null, false, null, List.of(), null, null, null, null, false,
                null, null, false, null, owner);
    }

    @Test
    @DisplayName("a user sees and manages their own rows only")
    void user() {
        OwnerScope scope = new OwnerScope(ALICE, false);
        assertThat(scope.sees(ALICE)).isTrue();
        assertThat(scope.sees(BOB)).isFalse();
        assertThat(scope.sees(null)).isFalse();
        assertThat(scope.manages(ALICE)).isTrue();
        assertThat(scope.manages(null)).isFalse();
        assertThat(scope.ownerForNew()).isEqualTo(ALICE);
        assertThat(scope.alertAudience()).isEqualTo(AlertAudience.own(ALICE));
    }

    @Test
    @DisplayName("an admin session sees and manages everything, and owns what it creates")
    void admin() {
        OwnerScope scope = new OwnerScope(ALICE, true);
        assertThat(scope.sees(BOB)).isTrue();
        assertThat(scope.manages(null)).isTrue();
        assertThat(scope.ownerForNew()).isEqualTo(ALICE);
        assertThat(scope.alertAudience()).isNull();
    }

    @Test
    @DisplayName("an API token sees everything and creates shared rows; anyone else sees shared rows only")
    void machineAndAnonymous() {
        assertThat(OwnerScope.MACHINE.sees(BOB)).isTrue();
        assertThat(OwnerScope.MACHINE.ownerForNew()).isNull();
        assertThat(OwnerScope.MACHINE.alertAudience()).isNull();
        assertThat(OwnerScope.SHARED_ONLY.sees(null)).isTrue();
        assertThat(OwnerScope.SHARED_ONLY.sees(ALICE)).isFalse();
        assertThat(OwnerScope.SHARED_ONLY.manages(null)).isFalse();
        assertThat(OwnerScope.SHARED_ONLY.alertAudience()).isEqualTo(AlertAudience.SHARED_ONLY);
        assertThat(OwnerScope.of((jakarta.servlet.http.HttpServletRequest) null)).isEqualTo(OwnerScope.MACHINE);
    }

    @Test
    @DisplayName("an alert query under an audience keeps the caller's and the shared alerts")
    void alertQuery() {
        AlertQuery mine = AlertQuery.all().withRestriction(ReadRestriction.NONE.withAlertAudience(AlertAudience.own(ALICE)));
        assertThat(mine.matches(alert(ALICE))).isTrue();
        assertThat(mine.matches(alert(null))).isTrue();
        assertThat(mine.matches(alert(BOB))).isFalse();
        AlertQuery shared = AlertQuery.all().withRestriction(ReadRestriction.NONE.withAlertAudience(AlertAudience.SHARED_ONLY));
        assertThat(shared.matches(alert(null))).isTrue();
        assertThat(shared.matches(alert(ALICE))).isFalse();
        assertThat(AlertQuery.all().matches(alert(BOB))).isTrue();
    }

    @Test
    @DisplayName("the notification topics hide another user's alert notifications from a user")
    void notificationFilter() {
        ai.foresignal.notify.NotificationMessage bobs = ai.foresignal.notify.NotificationMessage.builder(
                        ai.foresignal.notify.NotificationMessage.ALERT, "alert.fired", "t", "warning")
                .data(java.util.Map.of("sourceId", "src", "ownerPrincipalId", BOB)).build();
        ai.foresignal.notify.NotificationMessage shared = ai.foresignal.notify.NotificationMessage.builder(
                        ai.foresignal.notify.NotificationMessage.ALERT, "alert.fired", "t", "warning")
                .data(java.util.Map.of("sourceId", "src")).build();
        ai.foresignal.stream.sse.NotificationFilter alice = new ai.foresignal.stream.sse.NotificationFilter(
                java.util.Set.of(), ReadRestriction.NONE.withAlertAudience(AlertAudience.own(ALICE)), null);
        assertThat(alice.matches(bobs)).isFalse();
        assertThat(alice.matches(shared)).isTrue();
        assertThat(new ai.foresignal.stream.sse.NotificationFilter(java.util.Set.of()).matches(bobs)).isTrue();
    }

    @Test
    @DisplayName("the audience is part of the restriction's identity, so two users never share a cached response")
    void canonical() {
        ReadRestriction alice = ReadRestriction.NONE.withAlertAudience(AlertAudience.own(ALICE));
        ReadRestriction bob = ReadRestriction.NONE.withAlertAudience(AlertAudience.own(BOB));
        assertThat(alice.canonical()).isNotEqualTo(bob.canonical()).contains("o=" + ALICE);
        assertThat(alice).isNotEqualTo(bob).isNotEqualTo(ReadRestriction.NONE);
        assertThat(ReadRestriction.NONE.withAlertAudience(null)).isSameAs(ReadRestriction.NONE);
        assertThat(alice.toStored()).as("never stored").isNull();
        assertThat(alice.withExcludeSensitive(true).alertAudience()).isEqualTo(AlertAudience.own(ALICE));
    }
}
