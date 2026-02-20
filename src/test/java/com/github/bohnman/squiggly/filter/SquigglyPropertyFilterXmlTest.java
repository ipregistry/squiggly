package com.github.bohnman.squiggly.filter;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import tools.jackson.dataformat.xml.XmlMapper;
import com.github.bohnman.squiggly.context.provider.SimpleSquigglyContextProvider;
import com.github.bohnman.squiggly.model.Issue;
import com.github.bohnman.squiggly.model.IssueAction;
import com.github.bohnman.squiggly.model.User;
import com.github.bohnman.squiggly.parser.SquigglyParser;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SquigglyPropertyFilterXmlTest {

    private Issue issue;
    private ObjectMapper xmlMapper;
    private ObjectMapper rawXmlMapper;

    @BeforeEach
    public void beforeEachTest() {
        issue = buildIssue();

        rawXmlMapper = XmlMapper.builder()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .build();

        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        xmlMapper = XmlMapper.builder()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .filterProvider(filterProvider)
                .addMixIn(Object.class, SquigglyPropertyFilterMixin.class)
                .build();
    }

    private Issue buildIssue() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("email", "motherofdragons@got.com");
        properties.put("priority", "1");

        Issue issue = new Issue();
        issue.setId("ISSUE-1");
        issue.setIssueSummary("Dragons Need Fed");
        issue.setIssueDetails("I need my dragons fed pronto.");
        User assignee = new User("Jorah", "Mormont");
        issue.setAssignee(assignee);
        issue.setReporter(new User("Daenerys", "Targaryen"));
        issue.setActions(Arrays.asList(
                new IssueAction("COMMENT", "I'm going to let Daario get this one..", assignee),
                new IssueAction("CLOSE", "All set.", new User("Daario", "Naharis"))
        ));
        issue.setProperties(properties);
        return issue;
    }

    @Test
    public void testAnyDeep() {
        filter("**");
        String rawXml = stringifyRaw();
        String filteredXml = stringify();
        JsonNode rawTree = rawXmlMapper.readTree(rawXml);
        JsonNode filteredTree = rawXmlMapper.readTree(filteredXml);
        assertEquals(rawTree, filteredTree);
    }

    @Test
    public void testSingleField() {
        filter("id");
        String xml = stringify();
        JsonNode tree = rawXmlMapper.readTree(xml);
        assertEquals("ISSUE-1", tree.get("id").asText());
        assertNull(tree.get("issueSummary"));
        assertNull(tree.get("assignee"));
    }

    @Test
    public void testMultipleFields() {
        filter("id,issueSummary");
        String xml = stringify();
        JsonNode tree = rawXmlMapper.readTree(xml);
        assertEquals("ISSUE-1", tree.get("id").asText());
        assertEquals("Dragons Need Fed", tree.get("issueSummary").asText());
        assertNull(tree.get("issueDetails"));
        assertNull(tree.get("assignee"));
    }

    @Test
    public void testNestedField() {
        filter("assignee[firstName]");
        String xml = stringify();
        JsonNode tree = rawXmlMapper.readTree(xml);
        assertNotNull(tree.get("assignee"));
        assertEquals("Jorah", tree.get("assignee").get("firstName").asText());
        assertNull(tree.get("assignee").get("lastName"));
        assertNull(tree.get("id"));
    }

    @Test
    public void testEmpty() {
        filter("");
        String xml = stringify();
        JsonNode tree = rawXmlMapper.readTree(xml);
        assertNull(tree.get("id"));
        assertNull(tree.get("issueSummary"));
        assertNull(tree.get("assignee"));
    }

    private void filter(String filter) {
        SquigglyParser parser = new SquigglyParser();
        SimpleSquigglyContextProvider provider = new SimpleSquigglyContextProvider(parser, filter);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(SquigglyPropertyFilter.FILTER_ID, new SquigglyPropertyFilter(provider));

        xmlMapper = xmlMapper.rebuild()
                .filterProvider(filterProvider)
                .build();
    }

    private String stringify() {
        return SquigglyUtils.stringify(xmlMapper, issue);
    }

    private String stringifyRaw() {
        return SquigglyUtils.stringify(rawXmlMapper, issue);
    }
}
