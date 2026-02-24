package co.ipregistry.squiggly.metric;

import co.ipregistry.squiggly.filter.SquigglyPropertyFilter;
import co.ipregistry.squiggly.metric.source.CompositeSquigglyMetricsSource;
import co.ipregistry.squiggly.metric.source.SquigglyMetricsSource;
import co.ipregistry.squiggly.parser.SquigglyParser;
import co.ipregistry.squiggly.bean.BeanInfoIntrospector;
import com.google.common.collect.Maps;
import net.jcip.annotations.ThreadSafe;

import java.util.SortedMap;

/**
 * Provides API for obtaining various metrics in the squiggly libraries, such as cache statistics.
 */
@ThreadSafe
public class SquigglyMetrics {

    private static final SquigglyMetricsSource METRICS_SOURCE;

    static {
        METRICS_SOURCE = new CompositeSquigglyMetricsSource(
                SquigglyParser.getMetricsSource(),
                SquigglyPropertyFilter.getMetricsSource(),
                BeanInfoIntrospector.getMetricsSource()
        );
    }

    private SquigglyMetrics() {
    }

    /**
     * Gets the metrics as a map whose keys are the metric name and whose values are the metric values.
     *
     * @return map
     */
    public static SortedMap<String, Object> asMap() {
        SortedMap<String, Object> metrics = Maps.newTreeMap();
        METRICS_SOURCE.applyMetrics(metrics);
        return metrics;
    }
}
