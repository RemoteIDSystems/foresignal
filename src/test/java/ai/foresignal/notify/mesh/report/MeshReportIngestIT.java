package ai.foresignal.notify.mesh.report;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.notify.mesh.MeshShortCode;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.adapters.mesh.MeshReportsAdapter;
import ai.foresignal.source.adapters.mesh.MeshTextMessage;
import ai.foresignal.store.DeliveryStatus;
import ai.foresignal.store.NotificationDeliveryEntity;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.chatops.ChatopsItSupport;
import ai.foresignal.testsupport.notify.NotifyTestConfig;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * Field reports end to end (prompt 084): a bus message from an allowlisted sender on the channel's private index
 * creates a private {@code field-report} record in {@code mesh-reports}; {@code #E ACK} acknowledges the alert behind a
 * delivery's short code as {@code mesh:<sender>} and writes an audit row; unknown senders, other channels, direct
 * messages and texts without the prefix are ignored; malformed commands answer {@code ERR}; extras over 10 a minute
 * are dropped. Replies go through the real transmitter (the broker on 127.0.0.1:1 refuses, so they are not sent).
 */
@IntegrationTest
@Import(NotifyTestConfig.class)
class MeshReportIngestIT extends ChatopsItSupport {

    static final String ACKER = "!a1b2c3d4";
    static final String REPORTER = "!0badcafe";

    @Autowired MeshReportService reports;
    @Autowired SourceService sources;

    UUID channel;

    @BeforeEach
    void meshChannel() {
        reports.resetRateLimiter();
        channel = notify.channel("{'name':'mesh','type':'meshtastic','config':{'brokerUrl':'mqtt://127.0.0.1:1',"
                + "'gatewayNodeNum':'2864434397','channelIndex':1,'channelName':'Family','privateChannelConfirmed':true,"
                + "'region':'EU_868','reportsEnabled':true,'allowedSenders':{'" + ACKER + "':'ack','" + REPORTER
                + "':'report'}},'events':['alert']}");
    }

    MeshTextMessage heard(String from, Integer index, String text) {
        return new MeshTextMessage("meshtastic-local", "meshtastic", index, "Family", from, null, text, clock.instant(),
                -92.0, 6.5);
    }

    @Test
    @DisplayName("a report from an allowlisted sender becomes a private field-report record")
    void report() {
        MeshReportService.Outcome outcome = reports.handle(heard(REPORTER, 1, "#E R FIRE 51.48,-0.12 smoke on ridge"));
        assertThat(outcome.outcome()).isEqualTo("handled");
        assertThat(outcome.channelId()).isEqualTo(channel);
        assertThat(outcome.reply()).matches("OK R[0-9A-Z]{4} FIRE");
        assertThat(outcome.replySent()).as("the broker refuses").isFalse();

        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT kind, payload->>'type' AS type, payload->>'text' AS text, payload->>'reporter' AS reporter,
                       payload->>'network' AS network, payload->>'channel' AS channel
                  FROM record_head WHERE source_id = ?""", MeshReportsAdapter.ID);
        assertThat(rows).singleElement().satisfies(r -> {
            assertThat(r.get("kind")).isEqualTo(MeshReportsAdapter.KIND);
            assertThat(r.get("type")).isEqualTo("FIRE");
            assertThat(r.get("text")).isEqualTo("smoke on ridge");
            assertThat(r.get("reporter")).isEqualTo(REPORTER);
            assertThat(r.get("network")).isEqualTo("meshtastic");
            assertThat(r.get("channel")).isEqualTo("1");
        });
        assertThat(sources.list()).filteredOn(s -> s.id().equals(MeshReportsAdapter.ID)).singleElement()
                .satisfies(s -> assertThat(s.descriptor().privateSource()).isTrue());
    }

    @Test
    @DisplayName("#E ACK acknowledges the alert behind the short code as mesh:<sender> and is audited")
    void ack() {
        long alert = alerts.alert(alerts.rule("Quakes", T), "warning", T);
        String code = MeshShortCode.of(alert);
        NotificationDeliveryEntity d = new NotificationDeliveryEntity(channel, 1L, "alert", T);
        d.setAlertId(alert);
        d.setShortCode(code);
        d.setStatus(DeliveryStatus.DELIVERED);
        deliveries.saveAndFlush(d);

        assertThat(reports.handle(heard(REPORTER, 1, "#E ACK " + code)).reply()).isEqualTo("ERR not allowed");
        assertThat(alerts.state(alert)).isEqualTo("firing");

        assertThat(reports.handle(heard(ACKER, 1, "#E ack " + code.toLowerCase())).reply())
                .isEqualTo("OK ACK #" + code);
        assertThat(alerts.state(alert)).isEqualTo("acknowledged");
        assertThat(alerts.acknowledgedBy(alert)).isEqualTo("mesh:" + ACKER);
        audit.flush();
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT token_name, target_type, target_id, detail->>'code' AS code
                  FROM audit_event WHERE actor_kind = 'mesh'""");
        assertThat(rows).singleElement().satisfies(r -> {
            assertThat(r.get("token_name")).isEqualTo("mesh:" + ACKER);
            assertThat(r.get("target_type")).isEqualTo("alerts");
            assertThat(r.get("target_id")).isEqualTo(Long.toString(alert));
            assertThat(r.get("code")).isEqualTo(code);
        });
        assertThat(reports.handle(heard(ACKER, 1, "#E ACK ZZZZ")).reply()).isEqualTo("ERR unknown code ZZZZ");
    }

    @Test
    @DisplayName("unknown sender, wrong channel, direct message and missing prefix are ignored without a reply")
    void ignored() {
        assertThat(reports.handle(heard("!deadbeef", 1, "#E HELP")).outcome()).isEqualTo("ignored");
        assertThat(reports.handle(heard(ACKER, 2, "#E HELP")).outcome()).isEqualTo("ignored");
        assertThat(reports.handle(heard(ACKER, 0, "#E HELP")).outcome()).isEqualTo("ignored");
        assertThat(reports.handle(heard(ACKER, null, "#E HELP")).outcome()).isEqualTo("ignored");
        assertThat(reports.handle(heard(ACKER, 1, "R FIRE 51.48,-0.12 smoke")).outcome()).isEqualTo("ignored");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_head WHERE source_id = ?", Long.class,
                MeshReportsAdapter.ID)).isZero();
    }

    @Test
    @DisplayName("malformed commands answer ERR; HELP and STATUS answer within 133 characters")
    void replies() {
        assertThat(reports.handle(heard(REPORTER, 1, "#E R FIRE 95,10 smoke")).reply()).isEqualTo("ERR bad coordinates");
        assertThat(reports.handle(heard(REPORTER, 1, "#E R FIRE @nowhere smoke")).reply())
                .isEqualTo("ERR unknown place nowhere");
        assertThat(reports.handle(heard(REPORTER, 1, "#E HELP")).reply()).isEqualTo(MeshReportGrammar.HELP);
        String status = reports.handle(heard(REPORTER, 1, "#E STATUS")).reply();
        assertThat(status).contains("open ").contains(" crit, ").hasSizeLessThanOrEqualTo(133);
    }

    @Test
    @DisplayName("more than 10 commands a minute from one sender: the extras are ignored and counted")
    void rateLimit() {
        for (int i = 0; i < MeshReportService.COMMANDS_PER_MINUTE; i++) {
            assertThat(reports.handle(heard(REPORTER, 1, "#E HELP")).outcome()).isEqualTo("handled");
        }
        assertThat(reports.handle(heard(REPORTER, 1, "#E HELP")).outcome()).isEqualTo("rate-limited");
        assertThat(reports.handle(heard(ACKER, 1, "#E HELP")).outcome()).as("per sender").isEqualTo("handled");
        clock.setInstant(T.plusSeconds(61));
        assertThat(reports.handle(heard(REPORTER, 1, "#E HELP")).outcome()).isEqualTo("handled");
    }

    @Test
    @DisplayName("a MeshCore channel message is matched by name:<advertised name>")
    void meshcoreName() {
        notify.channel("{'name':'mc','type':'meshcore','config':{'companionUrl':'tcp://127.0.0.1:1','channelIndex':3,"
                + "'region':'EU_868','airtimeCustom':{'spreadingFactor':10,'bandwidthKhz':250,'codingRate':5,"
                + "'preambleSymbols':8},'reportsEnabled':true,'allowedSenders':{'name:Alice':'report'}},"
                + "'events':['alert']}");
        MeshTextMessage message = new MeshTextMessage("meshcore-local", "meshcore", 3, "Ops", null, null,
                "Alice: #E HELP", clock.instant(), null, 7.0);
        assertThat(reports.handle(message).reply()).isEqualTo(MeshReportGrammar.HELP);
        MeshTextMessage other = new MeshTextMessage("meshcore-local", "meshcore", 3, "Ops", null, null,
                "Mallory: #E HELP", clock.instant(), null, 7.0);
        assertThat(reports.handle(other).outcome()).isEqualTo("ignored");
    }
}
