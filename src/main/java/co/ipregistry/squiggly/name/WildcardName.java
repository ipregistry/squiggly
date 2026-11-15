package co.ipregistry.squiggly.name;

import com.google.re2j.Pattern;
import org.apache.commons.lang3.StringUtils;

// RE2/J, not java.util.regex: linear-time matching, see RegexName.
public class WildcardName implements SquigglyName {

    private final String name;
    private final String rawName;
    private final Pattern pattern;

    public WildcardName(String name) {
        this.name = name;
        this.rawName = StringUtils.remove(this.name, '*');
        this.pattern = buildPattern();
    }

    private Pattern buildPattern() {
        String[] search = {"*", "?"};
        String[] replace = {".*", ".?"};

        return Pattern.compile("^" + StringUtils.replaceEach(name, search, replace) + "$");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRawName() {
        return name;
    }

    @Override
    public int match(String name) {
        if (pattern.matcher(name).matches()) {
            return rawName.length() + 2;
        }

        return -1;
    }
}
