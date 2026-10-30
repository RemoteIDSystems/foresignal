package ai.foresignal.briefing;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Picks a voice that speaks an edition's language (prompt NP-003).
 *
 * <p>Kokoro voice names ({@code af_bella}) carry their language in the first letter and the voice's gender in the
 * second (Kokoro-82M {@code VOICES.md}: {@code a} American English, {@code b} British English, {@code j} Japanese,
 * {@code z} Mandarin Chinese, {@code e} Spanish, {@code f} French, {@code h} Hindi, {@code i} Italian, {@code p}
 * Brazilian Portuguese). When the configured Kokoro voice speaks another language than the edition, the voice of the
 * same gender for the edition's language is used instead; a language Kokoro has no voice for keeps the configured one.
 * Other voice names (OpenAI's {@code alloy}, which speak any language) are kept unchanged.
 */
public final class EditionVoice {

    private static final Pattern KOKORO = Pattern.compile("^[abjzefhip][fm]_[a-z]+$");

    /** Kokoro's voice prefix per primary language subtag (English is {@code a}, or {@code b} for {@code en-GB}). */
    private static final Map<String, Character> PREFIX = Map.of("en", 'a', "ja", 'j', "zh", 'z', "es", 'e', "fr", 'f',
            "hi", 'h', "it", 'i', "pt", 'p');

    /** The female and male voice used per Kokoro prefix (French has a female voice only). */
    private static final Map<Character, String[]> VOICES = Map.of(
            'a', new String[] {"af_heart", "am_michael"},
            'b', new String[] {"bf_emma", "bm_george"},
            'j', new String[] {"jf_alpha", "jm_kumo"},
            'z', new String[] {"zf_xiaobei", "zm_yunjian"},
            'e', new String[] {"ef_dora", "em_alex"},
            'f', new String[] {"ff_siwis", "ff_siwis"},
            'h', new String[] {"hf_alpha", "hm_omega"},
            'i', new String[] {"if_sara", "im_nicola"},
            'p', new String[] {"pf_dora", "pm_alex"});

    private EditionVoice() {}

    /**
     * {@code audio} with a voice that speaks {@code language}: unchanged without a language, for a non-Kokoro voice,
     * for a Kokoro voice already in the language, or for a language Kokoro has no voice for.
     *
     * @param audio the briefing's audio option, or null
     * @param language the edition's canonical BCP 47 language, or null
     */
    public static BriefingInput.Audio match(BriefingInput.Audio audio, String language) {
        if (audio == null || language == null) {
            return audio;
        }
        String voice = audio.voice();
        if (!KOKORO.matcher(voice).matches()) {
            return audio;
        }
        Locale locale = Locale.forLanguageTag(language);
        Character wanted = PREFIX.get(locale.getLanguage());
        if (wanted == null) {
            return audio;
        }
        char have = voice.charAt(0);
        if (wanted == 'a') {
            if ("GB".equals(locale.getCountry())) {
                wanted = 'b';
            } else if (have == 'a' || have == 'b') {
                return audio;
            }
        }
        if (have == wanted) {
            return audio;
        }
        String[] choices = VOICES.get(wanted);
        return new BriefingInput.Audio(voice.charAt(1) == 'm' ? choices[1] : choices[0], audio.format());
    }
}
