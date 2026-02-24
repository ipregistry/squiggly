package co.ipregistry.squiggly.context;

import co.ipregistry.squiggly.name.AnyDeepName;
import co.ipregistry.squiggly.parser.SquigglyNode;
import co.ipregistry.squiggly.parser.SquigglyParser;
import net.jcip.annotations.NotThreadSafe;

import java.util.Collections;
import java.util.List;

/**
 * Squiggly context that loads the parsed nodes on demand.
 */
@NotThreadSafe
public class LazySquigglyContext implements SquigglyContext {

    private final Class beanClass;
    private final String filter;
    private List<SquigglyNode> nodes;
    private final SquigglyParser parser;

    public LazySquigglyContext(Class beanClass, SquigglyParser parser, String filter) {
        this.beanClass = beanClass;
        this.parser = parser;
        this.filter = filter;
    }

    @Override
    public Class getBeanClass() {
        return beanClass;
    }

    private static final List<SquigglyNode> INCLUDE_ALL = Collections.singletonList(
            new SquigglyNode(AnyDeepName.get(), Collections.emptyList(), false, false, false));

    @Override
    public List<SquigglyNode> getNodes() {
        if (nodes == null) {
            try {
                nodes = parser.parse(filter);
            } catch (Exception e) {
                nodes = INCLUDE_ALL;
            }
        }

        return nodes;
    }

    @Override
    public String getFilter() {
        return filter;
    }
}
