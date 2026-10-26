package ai.foresignal.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

/**
 * The cold-tier members of {@code /v1/settings/archive} (prompt R2-016): they round-trip, the S3 secrets are
 * write-only and stored only as ciphertext, and every bad member is one 400.
 */
@IntegrationTest
@Import(RetentionTestSources.class)
class ArchiveSettingsIT extends ArchiveRunItSupport {

    static final String ACCESS = "AKIAEXAMPLEACCESSKEY";
    static final String SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    @Autowired ArchiveSettingsService service;

    private ResultActions putSettings(String body) throws Exception {
        return mvc.perform(put("/v1/settings/archive").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON).content(body.replace('\'', '"')));
    }

    @Test
    @DisplayName("the cold-tier config round-trips: glacier target, storage class, cutoff and schedule")
    void roundTrip() throws Exception {
        putSettings("{'enabled':true,'target':'glacier','s3':{'endpoint':'http://127.0.0.1:9000','bucket':'cold',"
                + "'prefix':'fs','pathStyle':true,'storageClass':'DEEP_ARCHIVE'},"
                + "'secrets':{'accessKey':'" + ACCESS + "','secretKey':'" + SECRET_KEY + "'},"
                + "'cutoff':'365d','schedule':'0 0 3 * * SUN'}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.target").value("glacier"))
                .andExpect(jsonPath("$.s3.storageClass").value("DEEP_ARCHIVE"))
                .andExpect(jsonPath("$.cutoff").value("365d"))
                .andExpect(jsonPath("$.schedule").value("0 0 3 * * SUN"))
                .andExpect(jsonPath("$.secretsSet", containsInAnyOrder("accessKey", "secretKey")))
                .andExpect(jsonPath("$.secrets").doesNotExist());

        String got = mvc.perform(get("/v1/settings/archive").header(HttpHeaders.AUTHORIZATION, admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.target").value("glacier"))
                .andExpect(jsonPath("$.cutoff").value("365d"))
                .andExpect(jsonPath("$.secretsSet", hasItem("secretKey")))
                .andReturn().getResponse().getContentAsString();
        assertThat(got).doesNotContain(ACCESS).doesNotContain(SECRET_KEY);

        // Encrypted at rest: the stored row holds neither secret, and both decrypt back.
        String stored = jdbc.queryForObject("SELECT value::text FROM app_setting WHERE key = ?", String.class,
                SettingKeys.ARCHIVE);
        assertThat(stored).doesNotContain(ACCESS).doesNotContain(SECRET_KEY).contains("secretKeyCipher");
        settings.invalidateCache();
        assertThat(service.secrets(service.get())).containsEntry("accessKey", ACCESS)
                .containsEntry("secretKey", SECRET_KEY);

        // null clears the cutoff and the schedule; omitted members keep their values.
        putSettings("{'cutoff':null,'schedule':''}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cutoff").doesNotExist())
                .andExpect(jsonPath("$.target").value("glacier"))
                .andExpect(jsonPath("$.secretsSet", containsInAnyOrder("accessKey", "secretKey")));
    }

    @Test
    @DisplayName("glacier without a storage class defaults to GLACIER on upload; the stored value stays null")
    void glacierDefault() throws Exception {
        putSettings("{'enabled':true,'target':'glacier','s3':{'endpoint':'http://127.0.0.1:9000','bucket':'cold'}}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.s3.storageClass").doesNotExist());
        assertThat(GlacierArchiveTier.DEFAULT_STORAGE_CLASS).isEqualTo("GLACIER");
    }

    @Test
    @DisplayName("bad members are one 400 and change nothing")
    void validation() throws Exception {
        putSettings("{'target':'tape','cutoff':'soon','schedule':'every sunday'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3));
        putSettings("{'cutoff':'1h'}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("cutoff"));
        putSettings("{'target':'glacier','s3':{'endpoint':'http://127.0.0.1:9000','bucket':'b','storageClass':'STANDARD'}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("s3.storageClass"));
        putSettings("{'target':'s3','s3':{'endpoint':'http://127.0.0.1:9000','bucket':'b','storageClass':'GLACIER'}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("s3.storageClass"));
        putSettings("{'target':'glacier'}")
                .andExpect(status().isBadRequest());
        assertThat(service.get().target()).isEqualTo(ArchiveSettings.LOCAL);
    }
}
