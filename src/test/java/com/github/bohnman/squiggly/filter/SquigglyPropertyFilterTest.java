package com.github.bohnman.squiggly.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.std.SimpleFilterProvider;
import com.github.bohnman.squiggly.config.SquigglyConfig;
import com.github.bohnman.squiggly.context.provider.SimpleSquigglyContextProvider;
import com.github.bohnman.squiggly.model.*;
import com.github.bohnman.squiggly.parser.SquigglyParser;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("Duplicates")
public class SquigglyPropertyFilterTest {

    public static final String BASE_PATH = "com/github/bohnman/squiggly/SquigglyPropertyFilterTest";
    private Issue issue;
    private ObjectMapper objectMapper;
    private boolean init = false;
    private ObjectMapper rawObjectMapper = JsonMapper.builder()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .build();

    @BeforeEach
    public void beforeEachTest() {
        if (!init) {
            issue = buildIssue();
            init = true;
        }

        // Rebuild objectMapper for each test with a fresh filter provider
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        objectMapper = JsonMapper.builder()
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

    private void assertJsonEquals(String expected, String actual) {
        JsonNode expectedTree = rawObjectMapper.readTree(expected);
        JsonNode actualTree = rawObjectMapper.readTree(actual);
        assertEquals(expectedTree, actualTree);
    }

    @Test
    public void testAnyDeep() {
        filter("**");
        assertJsonEquals(stringifyRaw(), stringify());
    }

    @Test
    public void testAnyShallow() {
        filter("*");
        // * includes base fields but not @FullView fields (entityType, user)
        assertJsonEquals(
                "{\"id\":\"ISSUE-1\",\"issueSummary\":\"Dragons Need Fed\",\"issueDetails\":\"I need my dragons fed pronto.\"," +
                "\"reporter\":{\"firstName\":\"Daenerys\",\"lastName\":\"Targaryen\"}," +
                "\"assignee\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\"}," +
                "\"actions\":[{\"id\":null,\"type\":\"COMMENT\",\"text\":\"I'm going to let Daario get this one..\"},{\"id\":null,\"type\":\"CLOSE\",\"text\":\"All set.\"}]," +
                "\"properties\":{\"email\":\"motherofdragons@got.com\",\"priority\":\"1\"}}",
                stringify());
    }

    @Test
    public void testBaseView() {
        filter("base");
        // base view includes only base fields, no @FullView fields
        assertJsonEquals(
                "{\"id\":\"ISSUE-1\",\"issueSummary\":\"Dragons Need Fed\",\"issueDetails\":\"I need my dragons fed pronto.\"," +
                "\"reporter\":{\"firstName\":\"Daenerys\",\"lastName\":\"Targaryen\"}," +
                "\"assignee\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\"}}",
                stringify());
    }

    @Test
    public void testFullView() {
        filter("full");
        // full view includes base + @FullView fields (actions, properties), but not @FullView nested (user, entityType)
        assertJsonEquals(
                "{\"id\":\"ISSUE-1\",\"issueSummary\":\"Dragons Need Fed\",\"issueDetails\":\"I need my dragons fed pronto.\"," +
                "\"reporter\":{\"firstName\":\"Daenerys\",\"lastName\":\"Targaryen\"}," +
                "\"assignee\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\"}," +
                "\"actions\":[{\"id\":null,\"type\":\"COMMENT\",\"text\":\"I'm going to let Daario get this one..\"},{\"id\":null,\"type\":\"CLOSE\",\"text\":\"All set.\"}]," +
                "\"properties\":{\"email\":\"motherofdragons@got.com\",\"priority\":\"1\"}}",
                stringify());
    }

    @Test
    public void testEmpty() {
        filter("");
        assertEquals("{}", stringify());
    }

    @Test
    public void testSingleField() {
        filter("id");
        assertJsonEquals("{\"id\":\"" + issue.getId() + "\"}", stringify());
    }

    @Test
    public void testMultipleFields() {
        filter("id,issueSummary");
        assertJsonEquals("{\"id\":\"" + issue.getId() + "\",\"issueSummary\":\"" + issue.getIssueSummary() + "\"}", stringify());
    }

    @Test
    public void testRegex() {
        filter("~iss[a-z]e.*~");
        assertJsonEquals("{\"issueSummary\":\"" + issue.getIssueSummary() + "\",\"issueDetails\":\"" + issue.getIssueDetails() + "\"}", stringify());
    }

    @Test
    public void testRegexCaseInsensitive() {
        filter("~iss[a-z]esumm.*~i");
        assertJsonEquals("{\"issueSummary\":\"" + issue.getIssueSummary() + "\"}", stringify());
    }

    @Test
    public void testRegexTraditional() {
        filter("/iss[a-z]e.*/");
        assertJsonEquals("{\"issueSummary\":\"" + issue.getIssueSummary() + "\",\"issueDetails\":\"" + issue.getIssueDetails() + "\"}", stringify());
    }

    @Test
    public void testWildCardSingle() {
        filter("issueSummar?");
        assertJsonEquals("{\"issueSummary\":\"" + issue.getIssueSummary() + "\"}", stringify());
    }

    @Test
    public void testWildCardStart() {
        filter("issue*");
        assertJsonEquals("{\"issueSummary\":\"" + issue.getIssueSummary() + "\",\"issueDetails\":\"" + issue.getIssueDetails() + "\"}", stringify());
    }

    @Test
    public void testWildCardEnd() {
        filter("*d");
        assertJsonEquals("{\"id\":\"" + issue.getId() + "\"}", stringify());
    }

    @Test
    public void testWildCardMiddle() {
        filter("*ue*");
        assertJsonEquals("{\"issueSummary\":\"" + issue.getIssueSummary() + "\",\"issueDetails\":\"" + issue.getIssueDetails() + "\"}", stringify());
    }


    @Test
    public void testDotPath() {
        filter("id,actions.user.firstName");
        assertJsonEquals("{\"id\":\"ISSUE-1\",\"actions\":[{\"user\":{\"firstName\":\"Jorah\"}},{\"user\":{\"firstName\":\"Daario\"}}]}", stringify());
    }

    @Test
    public void testNegativeDotPath() {
        filter("id,-actions.user.firstName");
        assertJsonEquals("{\"id\":\"ISSUE-1\",\"actions\":[{\"id\":null,\"type\":\"COMMENT\",\"text\":\"I'm going to let Daario get this one..\",\"user\":{\"lastName\":\"Mormont\"}},{\"id\":null,\"type\":\"CLOSE\",\"text\":\"All set.\",\"user\":{\"lastName\":\"Naharis\"}}]}", stringify());
    }

    @Test
    public void testNegativeDotPaths() {
        filter("-actions.user.firstName,-actions.user.lastName");
        assertJsonEquals("{\"id\":\"ISSUE-1\",\"issueSummary\":\"Dragons Need Fed\",\"issueDetails\":\"I need my dragons fed pronto.\",\"reporter\":{\"firstName\":\"Daenerys\",\"lastName\":\"Targaryen\"},\"assignee\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\"},\"actions\":[{\"id\":null,\"type\":\"COMMENT\",\"text\":\"I'm going to let Daario get this one..\",\"user\":{}},{\"id\":null,\"type\":\"CLOSE\",\"text\":\"All set.\",\"user\":{}}]}", stringify());
    }

    @Test
    public void testNestedDotPath() {
        filter("id,actions.user[firstName],issueSummary");
        assertJsonEquals("{\"id\":\"ISSUE-1\",\"issueSummary\":\"Dragons Need Fed\",\"actions\":[{\"user\":{\"firstName\":\"Jorah\"}},{\"user\":{\"firstName\":\"Daario\"}}]}", stringify());

        filter("id,actions.user[]");
        assertJsonEquals("{\"id\":\"ISSUE-1\",\"actions\":[{\"user\":{}},{\"user\":{}}]}", stringify());
    }

    @Test
    public void testDeepNestedDotPath() {
        filter("id,items.items[items.id]");
        assertJsonEquals("{\"id\":\"ITEM-1\",\"items\":[{\"items\":[{\"items\":[{\"id\":\"ITEM-4\"}]}]}]}", stringify(Item.testItem()));

        filter("id,items.items[items.items[id]]");
        assertJsonEquals("{\"id\":\"ITEM-1\",\"items\":[{\"items\":[{\"items\":[{\"items\":[{\"id\":\"ITEM-5\"}]}]}]}]}", stringify(Item.testItem()));

        filter("id,items.items[-items.id]");
        assertJsonEquals("{\"id\":\"ITEM-1\",\"items\":[{\"items\":[{\"id\":\"ITEM-3\",\"name\":\"Milkshake\",\"items\":[{\"name\":\"Hoverboard\",\"items\":[{\"id\":\"ITEM-5\",\"name\":\"Binoculars\",\"items\":[]}]}]}]}]}", stringify(Item.testItem()));

        filter("id,items.items[items[-id,-name],id]");
        assertJsonEquals("{\"id\":\"ITEM-1\",\"items\":[{\"items\":[{\"id\":\"ITEM-3\",\"items\":[{\"items\":[{\"id\":\"ITEM-5\",\"name\":\"Binoculars\",\"items\":[]}]}]}]}]}", stringify(Item.testItem()));

        fileTest("company-list.json", "deep-nested-01-filter.txt", "deep-nested-01-expected.json");
        fileTest("task-list.json", "deep-nested-02-filter.txt", "deep-nested-02-expected.json");
        fileTest("task-list.json", "deep-nested-03-filter.txt", "deep-nested-03-expected.json");
    }

    @Test
    public void testOtherView() {
        filter("other");
        // other view includes base + @OtherView fields (actions), but not @FullView-only fields
        // Note: user is @FullView only, so excluded. properties is @OtherView, so excluded (it's view1 not other)
        assertJsonEquals(
                "{\"id\":\"ISSUE-1\",\"issueSummary\":\"Dragons Need Fed\",\"issueDetails\":\"I need my dragons fed pronto.\"," +
                "\"reporter\":{\"firstName\":\"Daenerys\",\"lastName\":\"Targaryen\"}," +
                "\"assignee\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\"}," +
                "\"actions\":[{\"id\":null,\"type\":\"COMMENT\",\"text\":\"I'm going to let Daario get this one..\"},{\"id\":null,\"type\":\"CLOSE\",\"text\":\"All set.\"}]}",
                stringify());
    }

    @Test
    public void testNestedEmpty() {
        filter("assignee[]");
        assertJsonEquals("{\"assignee\":{}}", stringify());
    }

    @Test
    public void testAssignee() {
        filter("assignee");
        assertJsonEquals("{\"assignee\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\"}}", stringify());
    }

    @Test
    public void testNestedSingle() {
        filter("assignee[firstName]");
        assertJsonEquals("{\"assignee\":{\"firstName\":\"" + issue.getAssignee().getFirstName() + "\"}}", stringify());
    }

    @Test
    public void testNestedMultiple() {
        filter("actions[type,text]");
        assertJsonEquals("{\"actions\":[{\"type\":\"" + issue.getActions().get(0).getType() + "\",\"text\":\"" + issue.getActions().get(0).getText() + "\"},{\"type\":\"" + issue.getActions().get(1).getType() + "\",\"text\":\"" + issue.getActions().get(1).getText() + "\"}]}", stringify());
    }

    @Test
    public void testMultipleNestedSingle() {
        filter("(reporter,assignee)[lastName]");
        assertJsonEquals("{\"reporter\":{\"lastName\":\"" + issue.getReporter().getLastName() + "\"},\"assignee\":{\"lastName\":\"" + issue.getAssignee().getLastName() + "\"}}", stringify());
    }

    @Test
    public void testNestedMap() {
        filter("properties[priority]");
        assertJsonEquals("{\"properties\":{\"priority\":\"" + issue.getProperties().get("priority") + "\"}}", stringify());
    }

    @Test
    public void testDeepNested() {
        filter("actions[user[lastName]]");
        assertJsonEquals("{\"actions\":[{\"user\":{\"lastName\":\"" + issue.getActions().get(0).getUser().getLastName() + "\"}},{\"user\":{\"lastName\":\"" + issue.getActions().get(1).getUser().getLastName() + "\"}}]}", stringify());
    }

    @Test
    public void testSameParent() {
        filter("assignee[firstName],assignee[lastName]");
        assertJsonEquals("{\"assignee\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\"}}", stringify());

        filter("assignee.firstName,assignee.lastName");
        assertJsonEquals("{\"assignee\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\"}}", stringify());

        filter("actions.user[firstName],actions.user[lastName]");
        assertJsonEquals("{\"actions\":[{\"user\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\"}},{\"user\":{\"firstName\":\"Daario\",\"lastName\":\"Naharis\"}}]}", stringify());
    }

    @Test
    public void testFilterExcludesBaseFieldsInView() {
        boolean original = SquigglyConfig.isFilterImplicitlyIncludeBaseFieldsInView();

        try {
            setFieldValue(SquigglyConfig.class, "filterImplicitlyIncludeBaseFieldsInView", false);
            filter("view1");
            assertJsonEquals("{\"properties\":" + stringifyRaw(issue.getProperties()) + "}", stringify());
        } finally {
            setFieldValue(SquigglyConfig.class, "filterImplicitlyIncludeBaseFieldsInView", original);
        }
    }

    @Test
    public void testPropagateViewToNestedFilters() {
        boolean original = SquigglyConfig.isFilterPropagateViewToNestedFilters();

        try {
            setFieldValue(SquigglyConfig.class, "filterPropagateViewToNestedFilters", true);
            filter("full");
            assertJsonEquals(stringifyRaw(), stringify());
        } finally {
            setFieldValue(SquigglyConfig.class, "filterPropagateViewToNestedFilters", original);
        }
    }

    @Test
    public void testPropertyAddNonAnnotatedFieldsToBaseView() {
        boolean original = SquigglyConfig.isPropertyAddNonAnnotatedFieldsToBaseView();

        try {
            setFieldValue(SquigglyConfig.class, "propertyAddNonAnnotatedFieldsToBaseView", false);
            filter("base");
            assertEquals("{}", stringify());
        } finally {
            setFieldValue(SquigglyConfig.class, "propertyAddNonAnnotatedFieldsToBaseView", original);
        }
    }

    @Test
    public void testFilterSpecificty() {
        filter("**,reporter[lastName,entityType]");
        String raw = stringifyRaw();
        // Verify reporter only has lastName and entityType (firstName excluded)
        JsonNode rawTree = rawObjectMapper.readTree(raw);
        JsonNode actualTree = rawObjectMapper.readTree(stringify());
        // All fields same as raw, except reporter is missing firstName
        assertEquals(rawTree.get("id"), actualTree.get("id"));
        assertEquals(rawTree.get("actions"), actualTree.get("actions"));
        assertEquals(rawTree.get("assignee"), actualTree.get("assignee"));
        assertEquals(rawTree.get("properties"), actualTree.get("properties"));
        assertEquals(rawTree.get("reporter").get("lastName"), actualTree.get("reporter").get("lastName"));
        assertEquals(null, actualTree.get("reporter").get("firstName"));

        filter("**,repo*[lastName,entityType],repo*[firstName,entityType]");
        assertJsonEquals(raw, stringify());

        filter("**,reporter[lastName,entityType],repo*[firstName,entityType]");
        actualTree = rawObjectMapper.readTree(stringify());
        assertEquals(null, actualTree.get("reporter").get("firstName"));
        assertEquals(rawTree.get("reporter").get("lastName"), actualTree.get("reporter").get("lastName"));

        filter("**,repo*[firstName,entityType],rep*[lastName,entityType]");
        actualTree = rawObjectMapper.readTree(stringify());
        assertEquals(rawTree.get("reporter").get("firstName"), actualTree.get("reporter").get("firstName"));
        assertEquals(null, actualTree.get("reporter").get("lastName"));

        filter("**,reporter[firstName,entityType],reporter[lastName,entityType]");
        assertJsonEquals(raw, stringify());
    }

    @Test
    public void testFilterExclusion() {
        filter("**,reporter[-firstName]");
        assertJsonEquals(
                "{\"id\":\"ISSUE-1\",\"issueSummary\":\"Dragons Need Fed\",\"issueDetails\":\"I need my dragons fed pronto.\"," +
                "\"reporter\":{\"lastName\":\"Targaryen\"}," +
                "\"assignee\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\",\"entityType\":\"User\"}," +
                "\"actions\":[{\"id\":null,\"type\":\"COMMENT\",\"text\":\"I'm going to let Daario get this one..\",\"user\":{\"firstName\":\"Jorah\",\"lastName\":\"Mormont\",\"entityType\":\"User\"}}," +
                "{\"id\":null,\"type\":\"CLOSE\",\"text\":\"All set.\",\"user\":{\"firstName\":\"Daario\",\"lastName\":\"Naharis\",\"entityType\":\"User\"}}]," +
                "\"properties\":{\"email\":\"motherofdragons@got.com\",\"priority\":\"1\"}}",
                stringify());
    }

    @Test
    public void testJsonUnwrapped() {
        filter("innerText");
        assertJsonEquals("{\"innerText\":\"innerValue\"}", stringify(new Outer("outerValue", "innerValue")));
    }

    @Test
    public void testPropertyWithDash() {
        filter("full-name");
        assertJsonEquals("{\"full-name\":\"Fred Flintstone\"}", stringify(new DashObject("ID-1", "Fred Flintstone")));
    }

    private void setFieldValue(Class<?> ownerClass, String fieldName, boolean value) {
        try {
            Field field = ownerClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(null, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private String filter(String filter) {
        SquigglyParser parser = new SquigglyParser();
        SimpleSquigglyContextProvider provider = new SimpleSquigglyContextProvider(parser, filter);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(SquigglyPropertyFilter.FILTER_ID, new SquigglyPropertyFilter(provider));

        objectMapper = objectMapper.rebuild()
                .filterProvider(filterProvider)
                .build();

        return filter;
    }

    private String stringify() {
        return stringify(issue);
    }

    private String stringify(Object object) {
        return SquigglyUtils.stringify(objectMapper, object);
    }


    private String stringifyRaw() {
        return stringifyRaw(issue);
    }

    private String stringifyRaw(Object object) {
        return SquigglyUtils.stringify(rawObjectMapper, object);
    }

    private void fileTest(String inputFile, String filterFile, String expectedFile) {
        String input = readFile(BASE_PATH + "/input/" + inputFile);
        String filter = readFile(BASE_PATH + "/tests/" + filterFile);
        String expected = readFile(BASE_PATH + "/tests/" + expectedFile);

        Object inputObject = rawObjectMapper.readValue(input, Object.class);
        Object expectedObject = rawObjectMapper.readValue(expected, Object.class);

        filter(sanitizeFilter(filter));
        assertJsonEquals(stringifyRaw(expectedObject), stringify(inputObject));
    }

    private String readFile(String path) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);

        if (resource == null) {
            throw new IllegalArgumentException("path " + path + " does not exist");
        }

        try {
            return new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String sanitizeFilter(String filter) {
        String[] lines = filter.split("\n");
        StringBuilder builder = new StringBuilder(filter.length());

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("#")) {
                continue;
            }

            builder.append(line.replaceAll("\\s", ""));
        }

        return builder.toString();
    }

    @Test
    public void testInvalidFilterReturnsFullObject() {
        filter("connection,location2[2[e[2");
        assertJsonEquals(stringifyRaw(), stringify());
    }

    @Test
    public void testInvalidFilterUnclosedBracket() {
        filter("a[b[c");
        assertJsonEquals(stringifyRaw(), stringify());
    }

    private static class DashObject {

        private String id;

        @JsonProperty("full-name")
        private String fullName;

        public DashObject() {
        }

        public DashObject(String id, String fullName) {
            this.id = id;
            this.fullName = fullName;
        }

        public String getId() {
            return id;
        }

        public String getFullName() {
            return fullName;
        }
    }
}
