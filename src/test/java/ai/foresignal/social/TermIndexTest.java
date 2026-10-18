package ai.foresignal.social;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.error.FieldError;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The social term index (prompt 135): case and diacritic folding, word boundaries in Latin, Arabic, Hebrew and
 * Cyrillic script (and none in unspaced scripts), hashtags, accounts, channels, exclude terms, platform and cue-only
 * filters, and place, region and entity names expanded into scopes.
 */
class TermIndexTest {

    private static TermIndex.Match match(TermIndex index, String text) {
        return index.match(new TermIndex.Input("bluesky", text, List.of(), "someone", null, "stream"));
    }

    private static TermIndex single(String lang, String... terms) {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("terms", Map.of(lang, List.of(terms)));
        return new TermIndex(List.of(new TermIndex.TopicEntry("t", SocialTopicBlock.parse(block, null), Map.of(),
                Map.of(), Map.of())));
    }

    @Test
    @DisplayName("folding: case, diacritics, sharp s, final sigma, Arabic marks and tatweel, punctuation as spaces")
    void folding() {
        assertThat(TextFolding.fold("Bab el‑Mandeb!")).isEqualTo("bab el mandeb");
        assertThat(TextFolding.fold("Ḥodeidah — ÉTÉ")).isEqualTo("hodeidah ete");
        assertThat(TextFolding.fold("Straße")).isEqualTo("strasse");
        assertThat(TextFolding.fold("ΟΔΟΣ")).isEqualTo(TextFolding.fold("οδος"));
        assertThat(TextFolding.fold("الحُدَيْـدة")).isEqualTo(TextFolding.fold("الحديده"));
        assertThat(TextFolding.fold("أمريكا")).isEqualTo(TextFolding.fold("امريكا"));
        assertThat(TextFolding.fold("שָׁלוֹם")).isEqualTo("שלום");
        assertThat(TextFolding.fold(null)).isEmpty();
    }

    @Test
    @DisplayName("Latin: a phrase matches on word boundaries, never inside a word")
    void latinBoundaries() {
        TermIndex index = single("en", "Bab el-Mandeb", "Houthi");
        assertThat(match(index, "Tanker hit near BAB EL MANDEB today").matched()).isTrue();
        assertThat(match(index, "the houthis said").matched()).isFalse();
        assertThat(match(index, "Houthi forces").matched()).isTrue();
        assertThat(match(index, "#Houthi").matched()).as("a hashtag in the text is its word").isTrue();
        assertThat(match(index, "babel mandeb").matched()).isFalse();
    }

    @Test
    @DisplayName("Arabic: whole words and words with proclitics (و، ال، بال) match; a longer word does not")
    void arabicBoundaries() {
        TermIndex index = single("ar", "الحوثي");
        assertThat(match(index, "قال الحوثي اليوم").matched()).isTrue();
        assertThat(match(index, "والحوثي قال").matched()).isTrue();
        TermIndex hodeidah = single("ar", "حديدة");
        assertThat(match(hodeidah, "ميناء بالحديدة").matched()).as("بال + term").isTrue();
        assertThat(match(index, "الحوثيين").matched()).as("a longer word is not the term").isFalse();
    }

    @Test
    @DisplayName("Hebrew: prefix letters (ו، ה، ב، ל، מ، ש، כ) attach; Cyrillic keeps boundaries")
    void hebrewAndCyrillic() {
        TermIndex hebrew = single("he", "עזה");
        assertThat(match(hebrew, "הפגזה בעזה הלילה").matched()).isTrue();
        assertThat(match(hebrew, "ומעזה יצאו").matched()).isTrue();
        assertThat(match(hebrew, "עזהלה").matched()).isFalse();
        TermIndex russian = single("ru", "Харьков");
        assertThat(match(russian, "Удар по ХАРЬКОВ вечером").matched()).isTrue();
        assertThat(match(russian, "Харьковский район").matched()).isFalse();
    }

    @Test
    @DisplayName("unspaced scripts: a Han term matches inside a sentence")
    void unspaced() {
        TermIndex index = single("zh", "台湾海峡");
        assertThat(match(index, "今天台湾海峡有演习").matched()).isTrue();
    }

    @Test
    @DisplayName("hashtags, accounts and channels match without terms; excludes veto the topic")
    void hashtagsAccountsChannelsExcludes() {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("hashtags", List.of("#RedSea"));
        block.put("accounts", List.of(Map.of("platform", "bluesky", "handle", "@watcher.bsky.social")));
        block.put("channels", List.of(Map.of("platform", "reddit", "type", "subreddit", "id", "r/shipping")));
        block.put("terms", Map.of("en", List.of("tanker")));
        block.put("exclude", List.of("video game"));
        TermIndex index = new TermIndex(List.of(new TermIndex.TopicEntry("red-sea", SocialTopicBlock.parse(block,
                null), Map.of(), Map.of(), Map.of())));
        assertThat(index.match(new TermIndex.Input("bluesky", "nothing here", List.of("redsea"), null, null,
                "stream")).topicIds()).containsExactly("red-sea");
        assertThat(index.match(new TermIndex.Input("bluesky", "hello", List.of(), "watcher.bsky.social", null,
                "stream")).matched()).isTrue();
        assertThat(index.match(new TermIndex.Input("x", "hello", List.of(), "watcher.bsky.social", null, "stream"))
                .matched()).as("an account is per network").isFalse();
        assertThat(index.match(new TermIndex.Input("reddit", "hello", List.of(), null, "Shipping", "search"))
                .matched()).isTrue();
        assertThat(match(index, "a tanker in a video game").matched()).as("exclude").isFalse();
        assertThat(match(index, "a tanker off Yemen").matched()).isTrue();
    }

    @Test
    @DisplayName("platform subsets and cue-only topics")
    void platformsAndMode() {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("terms", Map.of("en", List.of("tanker")));
        block.put("platforms", List.of("mastodon"));
        Map<String, Object> cue = new LinkedHashMap<>();
        cue.put("terms", Map.of("en", List.of("drill")));
        cue.put("mode", "cue-only");
        TermIndex index = new TermIndex(List.of(
                new TermIndex.TopicEntry("a", SocialTopicBlock.parse(block, null), Map.of(), Map.of(), Map.of()),
                new TermIndex.TopicEntry("b", SocialTopicBlock.parse(cue, null), Map.of(), Map.of(), Map.of())));
        assertThat(match(index, "tanker").matched()).isFalse();
        assertThat(index.match(new TermIndex.Input("mastodon", "tanker", List.of(), null, null, "stream"))
                .matched()).isTrue();
        assertThat(match(index, "a drill").matched()).isFalse();
        assertThat(index.match(new TermIndex.Input("bluesky", "a drill", List.of(), null, null, "cue")).topicIds())
                .containsExactly("b");
    }

    @Test
    @DisplayName("place, region and entity names are terms with their own scopes; regions attribute coverage")
    void expansion() {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("terms", Map.of("en", List.of("convoy")));
        TermIndex index = new TermIndex(List.of(new TermIndex.TopicEntry("t", SocialTopicBlock.parse(block, null),
                Map.of("hodeidah-port", List.of("Hodeidah", "Al Hudaydah")), Map.of("YE", List.of("Yemen")),
                Map.of("01HZZZZZZZZZZZZZZZZZZZZZZZ", List.of("MV Example")))));
        TermIndex.Match m = match(index, "Strike reported at Al-Hudaydah port, Yemen");
        assertThat(m.topicIds()).containsExactly("t");
        assertThat(m.scopes()).containsExactly("place:hodeidah-port", "topic:t");
        assertThat(m.regionCodes()).containsExactly("YE");
        assertThat(match(index, "the mv example sails").scopes()).contains("entity:01HZZZZZZZZZZZZZZZZZZZZZZZ");
        assertThat(TermIndex.queryTerms(index.topics().get(0))).contains("convoy", "Hodeidah", "Yemen", "MV Example");
    }

    @Test
    @DisplayName("block validation: limits, channel types, region codes and the mode")
    void validation() {
        List<FieldError> errors = new ArrayList<>();
        List<String> many = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            many.add("term" + i);
        }
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("terms", Map.of("en", many));
        block.put("channels", List.of(Map.of("platform", "reddit", "type", "forum", "id", "x")));
        block.put("regionCodes", List.of("yemen"));
        block.put("mode", "sometimes");
        SocialTopicBlock.parse(block, errors);
        assertThat(errors).extracting(FieldError::field).contains("social.terms", "social.channels[0]",
                "social.regionCodes[0]", "social.mode");
        SocialTopicBlock ok = SocialTopicBlock.parse(Map.<String, Object>of("hashtags", List.of("#a")), null);
        assertThat(ok.hashtags()).containsExactly("a");
        assertThat(ok.toMap()).containsEntry("mode", "standing");
    }

    @Test
    @DisplayName("the automaton finds overlapping patterns")
    void automaton() {
        AhoCorasick<String> ac = new AhoCorasick<>(Map.of("he", "1", "she", "2", "hers", "3"));
        assertThat(ac.search("ushers")).extracting(AhoCorasick.Hit::value).containsExactlyInAnyOrder("1", "2", "3");
    }
}
