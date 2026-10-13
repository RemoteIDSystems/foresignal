package ai.foresignal.access;

import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_A;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.ApiTokenEntity;
import ai.foresignal.security.ApiTokenRepository;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenHasher;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.feed.FeedKeyService;
import ai.foresignal.security.restrict.PathKeyResolver;
import ai.foresignal.security.restrict.RequestPrincipal;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link TileKeyService} against the database (prompt 050): keys are minted once with the {@code fsk_}
 * prefix and stored hashed as {@code api_token} rows of {@code type = tile_key} (035's feed-key model,
 * no new table); TTLs over 30 days and prefixes outside {@link TileKeyPolicy#ALLOWED_PREFIXES} are
 * refused; revocation and expiry stop the key; listing pages never carry the key.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class TileKeyServiceIT {

    static final Instant T = Instant.parse("2026-10-12T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired TileKeyService tileKeys;
    @Autowired FeedKeyService feedKeys;
    @Autowired TokenService tokens;
    @Autowired ApiTokenRepository repository;
    @Autowired TokenHasher hasher;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
    }

    private FeedKeyService.CreatedKey create(String body) {
        return tileKeys.create(JSON.readTree(body));
    }

    private FeedKeyService.CreatedKey createDefault() {
        return create("{\"name\":\"tablet\",\"prefixes\":[\"/v1/products/\",\"/v1/changes/stream\"],\"ttl\":\"7d\"}");
    }

    @Test
    @DisplayName("creation returns an fsk_ key once and stores only its hash, as a tile_key row of api_token")
    void create() {
        FeedKeyService.CreatedKey created = createDefault();
        assertThat(created.key()).matches("^fsk_[A-Za-z0-9_-]{43}$");
        ApiTokenEntity row = repository.findById(created.entity().getId()).orElseThrow();
        assertThat(row.getType()).isEqualTo(ApiTokenEntity.TYPE_TILE_KEY);
        assertThat(row.isTileKey()).isTrue();
        assertThat(row.isFeedKey()).isFalse();
        assertThat(row.getScope()).isEqualTo(Scope.READ);
        assertThat(row.getTokenHash()).isEqualTo(hasher.hash(created.key())).doesNotContain(created.key());
        assertThat(row.getTokenPrefix()).isEqualTo(created.key().substring(0, 8));
        assertThat(row.getPrefixes()).containsExactly("/v1/products/", "/v1/changes/stream");
        assertThat(row.getExpiresAt()).isEqualTo(T.plus(Duration.ofDays(7)));
        assertThat(row.isPrivateAccess()).isFalse();
        assertThat(row.getRestrictions().excludePrivate()).isTrue();
        // One table: no tile-key table exists beside api_token.
        Integer tables = jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name LIKE '%tile_key%'", Integer.class);
        assertThat(tables).isZero();
    }

    @Test
    @DisplayName("tile keys stay out of the token and feed-key listings, and cannot be used as bearer tokens")
    void separateFromTokensAndFeedKeys() {
        FeedKeyService.CreatedKey created = createDefault();
        assertThat(tokens.list()).extracting(ApiTokenEntity::getId).doesNotContain(created.entity().getId());
        assertThat(feedKeys.list()).extracting(ApiTokenEntity::getId).doesNotContain(created.entity().getId());
        assertThat(tokens.authenticate(created.key())).isEmpty();
        assertThat(tileKeys.list()).extracting(ApiTokenEntity::getId).containsExactly(created.entity().getId());
        assertThatThrownBy(() -> feedKeys.revoke(created.entity().getId())).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("ISO and shorthand TTLs up to 30 days are accepted; longer, zero and malformed ones are 400")
    void ttl() {
        assertThat(create("{\"name\":\"a\",\"prefixes\":[\"/v1/styles\"],\"ttl\":\"P30D\"}").entity().getExpiresAt())
                .isEqualTo(T.plus(Duration.ofDays(30)));
        assertThat(create("{\"name\":\"b\",\"prefixes\":[\"/v1/styles\"],\"ttl\":\"12h\"}").entity().getExpiresAt())
                .isEqualTo(T.plus(Duration.ofHours(12)));
        for (String ttl : List.of("31d", "P31D", "0s", "soon", "-1d")) {
            assertThatThrownBy(() -> create("{\"name\":\"c\",\"prefixes\":[\"/v1/styles\"],\"ttl\":\"" + ttl + "\"}"))
                    .as(ttl).isInstanceOf(ValidationException.class);
        }
        assertThatThrownBy(() -> create("{\"name\":\"c\",\"prefixes\":[\"/v1/styles\"]}"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("a prefix outside the allowed list, an empty list and unknown members are 400 VALIDATION_FAILED")
    void prefixes() {
        for (String prefixes : List.of("[\"/v1/records\"]", "[\"/v1/products\"]", "[\"/v1/tokens\"]",
                "[\"/v1/products/../tokens\"]", "[]", "[1]")) {
            assertThatThrownBy(() -> create("{\"name\":\"x\",\"prefixes\":" + prefixes + ",\"ttl\":\"1d\"}"))
                    .as(prefixes).isInstanceOfSatisfying(ValidationException.class,
                            e -> assertThat(e.getMessage()).isNotBlank());
        }
        assertThatThrownBy(() -> create("{\"name\":\"x\",\"prefixes\":[\"/v1/styles\"],\"ttl\":\"1d\",\"scope\":\"admin\"}"))
                .isInstanceOf(ValidationException.class);
        // Every allowed prefix is accepted, including endpoints that ship later (404 until then).
        FeedKeyService.CreatedKey all = tileKeys.create(JSON.valueToTree(java.util.Map.of(
                "name", "all", "prefixes", TileKeyPolicy.ALLOWED_PREFIXES, "ttl", "1d")));
        assertThat(all.entity().getPrefixes()).containsExactlyElementsOf(TileKeyPolicy.ALLOWED_PREFIXES);
    }

    @Test
    @DisplayName("restrictions are stored; endpoints, places and excludePrivate:false are refused")
    void restrictions() {
        FeedKeyService.CreatedKey created = create("{\"name\":\"r\",\"prefixes\":[\"/v1/tiles/\"],\"ttl\":\"1d\","
                + "\"restrictions\":{\"sources\":[\"" + SCHEMA_A + "\"],\"bbox\":[0,0,10,10]}}");
        assertThat(created.entity().getRestrictions().sources()).containsExactly(SCHEMA_A);
        assertThat(created.entity().getRestrictions().bbox()).containsExactly(0.0, 0.0, 10.0, 10.0);
        for (String bad : List.of("{\"endpoints\":[\"GET /v1/tiles/**\"]}", "{\"placeIds\":[\"home\"]}",
                "{\"sources\":[\"" + SCHEMA_A + "\"],\"excludePrivate\":false}", "{\"sources\":[\"no-such-source\"]}")) {
            assertThatThrownBy(() -> create("{\"name\":\"r\",\"prefixes\":[\"/v1/tiles/\"],\"ttl\":\"1d\","
                    + "\"restrictions\":" + bad + "}")).as(bad).isInstanceOf(ValidationException.class);
        }
    }

    @Test
    @DisplayName("an active key resolves to a TILE_KEY principal without the private grant; revoked and expired keys do not")
    void resolveRevokeAndExpire() {
        FeedKeyService.CreatedKey created = createDefault();
        PathKeyResolver.Resolution resolution = tileKeys.resolve(created.key());
        assertThat(resolution).isInstanceOfSatisfying(PathKeyResolver.Valid.class, valid -> {
            RequestPrincipal principal = valid.principal();
            assertThat(principal.kind()).isEqualTo(RequestPrincipal.Kind.TILE_KEY);
            assertThat(principal.scope()).isEqualTo(Scope.READ);
            assertThat(principal.privateAccess()).isFalse();
            assertThat(principal.restriction().excludePrivate()).isTrue();
            assertThat(principal.clientKey()).isEqualTo("tilekey:" + created.entity().getId());
            assertThat(valid.prefixes()).containsExactly("/v1/products/", "/v1/changes/stream");
        });
        assertThat(tileKeys.resolve("fsk_unknown")).isInstanceOf(PathKeyResolver.Invalid.class);
        assertThat(tileKeys.resolve(created.key().replace("fsk_", "fsf_"))).isInstanceOf(PathKeyResolver.Invalid.class);

        tileKeys.revoke(created.entity().getId());
        assertThat(tileKeys.resolve(created.key())).isInstanceOf(PathKeyResolver.Gone.class);
        assertThat(repository.findById(created.entity().getId()).orElseThrow().getRevokedAt()).isEqualTo(T);
        assertThatThrownBy(() -> tileKeys.revoke(java.util.UUID.randomUUID())).isInstanceOf(NotFoundException.class);

        FeedKeyService.CreatedKey other = createDefault();
        clock.advance(Duration.ofDays(7).plusSeconds(1));
        assertThat(tileKeys.resolve(other.key())).isInstanceOf(PathKeyResolver.Gone.class);
    }

    @Test
    @DisplayName("listing pages newest first with an opaque cursor; a foreign cursor is INVALID_CURSOR")
    void paging() {
        FeedKeyService.CreatedKey first = createDefault();
        clock.advance(Duration.ofSeconds(1));
        FeedKeyService.CreatedKey second = createDefault();
        clock.advance(Duration.ofSeconds(1));
        FeedKeyService.CreatedKey third = createDefault();
        TileKeyService.Page page1 = tileKeys.page(null, 2);
        assertThat(page1.items()).extracting(ApiTokenEntity::getId)
                .containsExactly(third.entity().getId(), second.entity().getId());
        assertThat(page1.nextCursor()).isNotNull();
        TileKeyService.Page page2 = tileKeys.page(page1.nextCursor(), 2);
        assertThat(page2.items()).extracting(ApiTokenEntity::getId).containsExactly(first.entity().getId());
        assertThat(page2.nextCursor()).isNull();
        assertThatThrownBy(() -> tileKeys.page("bm90LWEtY3Vyc29y", 2)).isInstanceOf(ValidationException.class);
    }
}
