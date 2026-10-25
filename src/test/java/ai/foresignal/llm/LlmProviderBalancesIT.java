package ai.foresignal.llm;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.WireMockSupport;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link LlmProviderBalances} against WireMock stubs of the documented DeepSeek and OpenRouter
 * balance endpoints.
 */
@IntegrationTest
class LlmProviderBalancesIT extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-25T12:00:00Z");

    @Autowired LlmProviderBalances balances;

    static LlmProviderConfig config(String baseUrl) {
        return new LlmProviderConfig("p", LlmProviderType.OPENAI_COMPATIBLE, baseUrl, "m", null, null, null, null,
                null, Map.of(), false, "sk-test", 30, 1_000_000, true, null);
    }

    @Test
    @DisplayName("the reporters are recognized by host; every other provider has no balance")
    void reporters() {
        assertThat(LlmProviderBalances.reporter("https://api.deepseek.com")).isEqualTo("DeepSeek");
        assertThat(LlmProviderBalances.reporter("https://openrouter.ai/api/v1")).isEqualTo("OpenRouter");
        assertThat(LlmProviderBalances.reporter("https://api.openai.com/v1")).isNull();
        assertThat(LlmProviderBalances.reporter("https://api.x.ai/v1")).isNull();
        assertThat(balances.balance(config("https://api.groq.com/openai/v1"))).isEmpty();
    }

    @Test
    @DisplayName("DeepSeek: GET /user/balance, total_balance per currency, with the provider's key")
    void deepSeek() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/user/balance")).willReturn(okJson("{\"is_available\":true,"
                + "\"balance_infos\":[{\"currency\":\"CNY\",\"total_balance\":\"110.00\",\"granted_balance\":\"10.00\","
                + "\"topped_up_balance\":\"100.00\"},{\"currency\":\"USD\",\"total_balance\":\"4.20\"}]}")));

        LlmProviderBalances.Balance balance = balances.fetch(config(baseUrl()), "DeepSeek", NOW);

        assertThat(balance.error()).isNull();
        assertThat(balance.kind()).isEqualTo("account");
        assertThat(balance.amounts()).extracting(LlmProviderBalances.Amount::currency).containsExactly("CNY", "USD");
        assertThat(balance.amounts().get(1).amount()).isEqualByComparingTo(new BigDecimal("4.20"));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/user/balance"))
                .withHeader("Authorization", equalTo("Bearer sk-test")));
    }

    @Test
    @DisplayName("OpenRouter: credits for a management key, else the key's remaining limit, else an explained null")
    void openRouter() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v1/credits"))
                .willReturn(okJson("{\"data\":{\"total_credits\":100.5,\"total_usage\":25.75}}")));
        LlmProviderBalances.Balance account = balances.fetch(config(baseUrl() + "/api/v1"), "OpenRouter", NOW);
        assertThat(account.kind()).isEqualTo("account");
        assertThat(account.amounts().getFirst().amount()).isEqualByComparingTo("74.75");

        WIREMOCK.resetAll();
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v1/credits")).willReturn(aResponse().withStatus(403)
                .withBody("{\"error\":{\"code\":403,\"message\":\"Only management keys can perform this operation\"}}")));
        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v1/key"))
                .willReturn(okJson("{\"data\":{\"limit\":100,\"limit_remaining\":74.5,\"usage\":25.5}}")));
        LlmProviderBalances.Balance key = balances.fetch(config(baseUrl() + "/api/v1"), "OpenRouter", NOW);
        assertThat(key.kind()).isEqualTo("key-limit");
        assertThat(key.amounts().getFirst().amount()).isEqualByComparingTo("74.5");
        assertThat(key.amounts().getFirst().currency()).isEqualTo("USD");

        WIREMOCK.stubFor(get(urlPathEqualTo("/api/v1/key")).willReturn(okJson("{\"data\":{\"limit\":null,"
                + "\"limit_remaining\":null}}")));
        LlmProviderBalances.Balance none = balances.fetch(config(baseUrl() + "/api/v1"), "OpenRouter", NOW);
        assertThat(none.amounts()).isEmpty();
        assertThat(none.error()).contains("no spending limit");
    }

    @Test
    @DisplayName("a failing balance request is reported in the balance, never thrown")
    void failure() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/user/balance")).willReturn(aResponse().withStatus(401)));

        LlmProviderBalances.Balance balance = balances.fetch(config(baseUrl()), "DeepSeek", NOW);

        assertThat(balance.amounts()).isEmpty();
        assertThat(balance.error()).contains("HTTP 401");
    }
}
