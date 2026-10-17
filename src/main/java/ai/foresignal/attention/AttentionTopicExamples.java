package ai.foresignal.attention;

import java.util.List;

/**
 * The five example attention topics shipped disabled (prompt 117). Every Wikipedia article was confirmed to have
 * pageview data on {@code en.wikipedia} on 2026-10-16; the GDELT queries put OR'd terms in parentheses, as the DOC
 * 2.0 API requires.
 */
public final class AttentionTopicExamples {

    private AttentionTopicExamples() {}

    private static AttentionTopicInput.WikiArticle en(String article) {
        return new AttentionTopicInput.WikiArticle("en.wikipedia", article);
    }

    /** The examples, in id order. */
    public static final List<AttentionTopicInput> ALL = List.of(
            new AttentionTopicInput("gulf-energy-infrastructure", "Gulf energy infrastructure",
                    "Attacks on and outages of Gulf oil and gas facilities.",
                    "(Abqaiq OR \"Ras Tanura\" OR \"Ras Laffan\" OR refinery) (attack OR drone OR strike OR outage)",
                    List.of(en("Abqaiq"), en("Ras_Tanura"), en("Ras_Laffan")),
                    List.of("Abqaiq", "Ras Tanura", "Ras Laffan", "refinery", "pipeline", "LNG terminal"),
                    List.of("SA", "QA", "AE", "KW", "BH"), List.of("energy", "infrastructure"), false, null),
            new AttentionTopicInput("red-sea-shipping", "Red Sea shipping",
                    "Shipping disruption in the Red Sea and the Bab el-Mandeb strait.",
                    "(\"Red Sea\" OR \"Bab el-Mandeb\") (shipping OR tanker OR vessel OR Houthi)",
                    List.of(en("Bab-el-Mandeb"), en("Red_Sea"), en("Houthis")),
                    List.of("Red Sea", "Bab el-Mandeb", "Houthi", "tanker", "container ship", "Suez"),
                    List.of("YE", "ER", "DJ", "SA", "EG"), List.of("shipping", "maritime-security"), false, null),
            new AttentionTopicInput("strait-of-hormuz", "Strait of Hormuz",
                    "Tanker traffic, seizures and military activity in the Strait of Hormuz.",
                    "\"Strait of Hormuz\"",
                    List.of(en("Strait_of_Hormuz")),
                    List.of("Strait of Hormuz", "Hormuz", "tanker seizure", "Bandar Abbas"),
                    List.of("IR", "OM", "AE"), List.of("energy", "shipping", "maritime-security"), false, null),
            new AttentionTopicInput("taiwan-strait", "Taiwan Strait",
                    "Military activity and tension across the Taiwan Strait.",
                    "\"Taiwan Strait\"",
                    List.of(en("Taiwan_Strait")),
                    List.of("Taiwan Strait", "PLA exercise", "ADIZ"),
                    List.of("TW", "CN"), List.of("military"), false, null),
            new AttentionTopicInput("ukraine-front", "Ukraine front",
                    "Fighting along the front line in Ukraine.",
                    "Ukraine (frontline OR offensive OR shelling)",
                    List.of(en("Russian_invasion_of_Ukraine")),
                    List.of("Ukraine", "front line", "offensive", "shelling", "drone strike"),
                    List.of("UA", "RU"), List.of("conflict"), false, null));
}
