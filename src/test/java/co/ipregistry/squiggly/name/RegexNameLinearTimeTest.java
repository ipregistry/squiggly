package co.ipregistry.squiggly.name;

import com.google.re2j.PatternSyntaxException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RegexName} and {@link WildcardName} run on RE2/J (linear-time), not
 * {@code java.util.regex}. These tests pin the two properties that matter:
 * matching stays correct, and a stacked-wildcard pattern — which makes
 * {@code java.util.regex} backtrack for seconds — resolves in milliseconds, so
 * a {@code fields=} filter taken from an untrusted request cannot be turned
 * into a denial of service.
 */
class RegexNameLinearTimeTest {

    @Test
    void matchesAWholeNameLikeBefore() {
        final RegexName ip = new RegexName("ip", Set.of());
        assertTrue(ip.match("ip") >= 0, "regex 'ip' should match field 'ip'");
        assertEquals(-1, ip.match("ipinfo"), "regex 'ip' should not match 'ipinfo' (whole-name match)");
        assertEquals(-1, ip.match("zip"), "regex 'ip' should not match 'zip' (whole-name match)");
    }

    @Test
    void honoursTheCaseInsensitiveFlag() {
        final RegexName ci = new RegexName("ip", Set.of("i"));
        assertTrue(ci.match("IP") >= 0, "case-insensitive 'ip' should match 'IP'");
        assertEquals(-1, new RegexName("ip", Set.of()).match("IP"), "case-sensitive 'ip' should not match 'IP'");
    }

    @Test
    void wildcardStillMatches() {
        final WildcardName w = new WildcardName("loc*");
        assertTrue(w.match("location") >= 0);
        assertEquals(-1, w.match("security"));
    }

    @Test
    void aStackedWildcardPatternResolvesQuicklyInsteadOfBacktracking() {
        // 28 stacked ".*" then a literal the subject never contains: on
        // java.util.regex this backtracks for ~4 s per name; on RE2/J it is
        // immediate. Generous ceiling to stay stable on a loaded CI box.
        final RegexName bomb = new RegexName(".*".repeat(28) + "x", Set.of());
        final String[] names = {"carrier", "company", "connection", "currency", "hostname",
                "ip", "location", "security", "time_zone", "type"};

        final long startNanos = System.nanoTime();
        for (final String name : names) {
            assertEquals(-1, bomb.match(name), "the pattern ends in 'x', no name does");
        }
        final long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;

        assertTrue(elapsedMillis < 1_000,
                "ten matches of a stacked-wildcard pattern should take well under a second on RE2/J, took " + elapsedMillis + " ms");
    }

    @Test
    void aBackreferenceIsRejectedAtCompileTime() {
        // RE2/J cannot run constructs that need backtracking; it refuses them
        // at compile time. Squiggly treats that like any other invalid filter.
        assertThrows(PatternSyntaxException.class, () -> new RegexName("(a)\\1", Set.of()));
    }
}
