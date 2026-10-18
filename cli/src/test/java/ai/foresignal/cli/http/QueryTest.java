package ai.foresignal.cli.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Query building and encoding. */
class QueryTest {

    @Test
    void skipsNullsRepeatsArraysAndEncodes() {
        Query q = Query.of().add("a", null).add("b", " ").add("q", "red sea & more").addAll("source", List.of("x", "y"))
                .flag("live", false).flag("touches", true).add("n", 5);
        assertEquals("q=red%20sea%20%26%20more&source=x&source=y&touches=true&n=5", q.encode());
    }

    @Test
    void setReplacesAndCopyIsIndependent() {
        Query q = Query.of().add("cursor", "a");
        Query copy = q.copy().set("cursor", "b");
        assertEquals("cursor=a", q.encode());
        assertEquals("cursor=b", copy.encode());
        assertTrue(Query.of().set("x", null).isEmpty());
    }

    @Test
    void componentEncodesPathSegments() {
        assertEquals("us%2F7000%20x", Query.component("us/7000 x"));
    }
}
