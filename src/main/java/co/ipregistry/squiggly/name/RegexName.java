package co.ipregistry.squiggly.name;

import com.google.re2j.Pattern;

import java.util.Set;

// RE2/J (com.google.re2j), not java.util.regex: it matches in time linear in the
// input and cannot catastrophically backtrack, so a field filter coming from an
// untrusted request can never hold a worker thread (e.g. a stacked-wildcard
// pattern such as ~.*.*.*...x~). It rejects constructs that need backtracking
// (backreferences, lookahead/lookbehind) at compile time with a
// PatternSyntaxException, which is handled exactly like any other invalid filter.
public class RegexName implements SquigglyName {

    private final String name;
    private final String rawName;
    private final Pattern pattern;

    public RegexName(String name, Set<String> flags) {
        this.name = name;
        this.rawName = name;
        this.pattern = buildPattern(name, flags);
    }

    private Pattern buildPattern(String name, Set<String> flags) {
        int flagMask = 0;

        if (flags != null && !flags.isEmpty()) {
            for (String flag : flags) {
                switch (flag) {
                    case "i":
                        flagMask |= Pattern.CASE_INSENSITIVE;
                        break;
                    default:
                        throw new IllegalArgumentException("Unrecognized flag " + flag + " for pattern " + name);
                }
            }
        }

        return Pattern.compile(name, flagMask);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRawName() {
        return rawName;
    }

    @Override
    public int match(String name) {
        if (pattern.matcher(name).matches()) {
            return rawName.length() + 2;
        }

        return -1;
    }
}
