package net.trilogy.arch.adapter.jira;

import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.jira.JiraStory.InvalidStoryException;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlCapabilitiesContainer;
import net.trilogy.arch.domain.architectureUpdate.YamlE2E;
import net.trilogy.arch.domain.architectureUpdate.YamlEpic;
import net.trilogy.arch.domain.architectureUpdate.YamlFeatureStory;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement;
import net.trilogy.arch.domain.architectureUpdate.YamlFunctionalRequirement.FunctionalRequirementId;
import net.trilogy.arch.domain.architectureUpdate.YamlJira;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.YamlTddContainerByComponent;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static net.trilogy.arch.TestHelper.MANIFEST_PATH_TO_TEST_JIRA_STORY_CREATION;
import static net.trilogy.arch.Util.first;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JiraStoryTest {
    private static YamlArchitectureUpdate getAuFixture() {
        TddContent tddContent1 = new TddContent("content-file-1", "TDD 1 : Component-31.md");
        TddContent tddContent3 = new TddContent("content-file-3", "TDD 3 : Component-404.txt");

        return YamlArchitectureUpdate.prefilledYamlArchitectureUpdateWithBlanks()
                .tddContainersByComponent(List.of(
                        new YamlTddContainerByComponent(
                                new TddComponentReference("31"),
                                null, false,
                                Map.of(
                                        new TddId("TDD 1"), new YamlTdd("TDD 1 text", null).withContent(tddContent1),
                                        new TddId("TDD 2"), new YamlTdd("TDD 2 text", null).withContent(null),
                                        new TddId("[SAMPLE-TDD-ID]"), new YamlTdd("sample tdd text", null))),
                        new YamlTddContainerByComponent(
                                new TddComponentReference("404"),
                                null, true,
                                Map.of(
                                        new TddId("TDD 3"), new YamlTdd("TDD 3 text", null).withContent(tddContent3),
                                        new TddId("TDD 4"), new YamlTdd("TDD 4 text", null).withContent(null)))))
                .capabilityContainer(new YamlCapabilitiesContainer(
                        YamlEpic.blank(),
                        singletonList(
                                new YamlFeatureStory(
                                        "story title",
                                        new YamlJira("", ""),
                                        List.of(new TddId("TDD 1"), new TddId("TDD 2"), new TddId("TDD 3")),
                                        List.of(FunctionalRequirementId.blank()),
                                        YamlE2E.blank()))))
                .build();
    }

    private static YamlArchitectureUpdate getAuWithInvalidComponent() {
        return changeAllTddsToBeUnderComponent("1231231323123", getAuFixture());
    }

    private static YamlArchitectureUpdate getAuWithInvalidRequirement() {
        return getAuFixture().toBuilder().functionalRequirements(
                Map.of(new FunctionalRequirementId("different id than the reference in the story"),
                        new YamlFunctionalRequirement("any text", "any source", List.of())))
                .build();
    }

    private static YamlArchitectureUpdate changeAllTddsToBeUnderComponent(String newComponentId, YamlArchitectureUpdate au) {
        var oldTdds = new HashMap<TddId, YamlTdd>();
        for (var container : au.getTddContainersByComponent()) {
            oldTdds.putAll(container.getTdds());
        }

        final var newComponentWithTdds = new YamlTddContainerByComponent(
                new TddComponentReference(newComponentId),
                null, false,
                oldTdds);

        return au.toBuilder().tddContainersByComponent(List.of(newComponentWithTdds)).build();
    }

    private static JiraStory.JiraTdd asJiraTdd(TddId tddId, YamlTdd tdd, String componentPath) {
            return  new JiraStory.JiraTdd(tddId, tdd,componentPath, tdd.getContent());
    }
    @Test
    public void shouldConstructJiraStory() throws Exception {
        // GIVEN:
        var au = getAuFixture();

        var afterAuArchitecture = getArchitectureAfterAu();
        var beforeAuArchitecture = getArchitectureBeforeAu();
        var featureStory = first(au.getCapabilityContainer().getFeatureStories());


        TddId tddId1 = new TddId("TDD 1");
        YamlTdd tdd1 = au.getTddContainersByComponent().get(0).getTdds().get(tddId1);
        TddId tddId2 = new TddId("TDD 2");
        YamlTdd tdd2 = au.getTddContainersByComponent().get(0).getTdds().get(tddId2);
        TddId tddId3 = new TddId("TDD 3");
        YamlTdd tdd3 = au.getTddContainersByComponent().get(1).getTdds().get(tddId3);

        final var expectedFeatureStoryTddIds = List.of(tddId1, tddId2, tddId3);
        final var expectedFeatureStoryFRs = List.of( new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"));
        final var baseComponentPath = "c4://Internet Banking System/API Application/";
        final JiraStory expected = new JiraStory(
                new YamlFeatureStory("story title",
                        new YamlJira("",""),
                        expectedFeatureStoryTddIds,
                        expectedFeatureStoryFRs,
                        YamlE2E.blank()),
                List.of(asJiraTdd(tddId1,tdd1,baseComponentPath +"Reset Password Controller"),
                        asJiraTdd(tddId2,tdd2,baseComponentPath +"Reset Password Controller"),
                        asJiraTdd(tddId3,tdd3,baseComponentPath +"Sign In Controller")),
                List.of(
                        new JiraStory.JiraFunctionalRequirement(
                                new FunctionalRequirementId("[SAMPLE-REQUIREMENT-ID]"),
                                new YamlFunctionalRequirement(
                                        "[SAMPLE REQUIREMENT TEXT]",
                                        "[SAMPLE REQUIREMENT SOURCE TEXT]",
                                        List.of(new TddId("[SAMPLE-TDD-ID]"))))));
        // WHEN:
        final JiraStory actual = new JiraStory(featureStory, au, beforeAuArchitecture, afterAuArchitecture);

        // THEN:
        assertThat(actual, equalTo(expected));
    }

    @Test(expected = InvalidStoryException.class)
    public void shouldThrowIfInvalidRequirement() throws Exception {
        // GIVEN
        var au = getAuWithInvalidRequirement();
        var featureStory = first(au.getCapabilityContainer().getFeatureStories());

        // WHEN:
        new JiraStory(featureStory, au, getArchitectureBeforeAu(), getArchitectureAfterAu());

        // THEN raise exception.
    }

    @Test(expected = InvalidStoryException.class)
    public void shouldThrowIfComponentHasNoPath() throws Exception {
        // GIVEN
        var au = getAuFixture();
        ArchitectureDataStructure architectureAfterAu = getArchitectureAfterAu();

        architectureAfterAu.getModel().getComponents().forEach(c -> c.setPath((String) null));

        var featureStory = first(au.getCapabilityContainer().getFeatureStories());

        // WHEN
        new JiraStory(featureStory, au, getArchitectureBeforeAu(), architectureAfterAu);

        // THEN
        // Raise Error
    }

    @Test(expected = InvalidStoryException.class)
    public void shouldThrowIfInvalidComponent() throws Exception {
        // GIVEN
        var au = getAuWithInvalidComponent();

        var featureStory = first(au.getCapabilityContainer().getFeatureStories());

        // WHEN
        new JiraStory(featureStory, au, getArchitectureBeforeAu(), getArchitectureAfterAu());

        // THEN
        // Raise Error
    }

    @Test(expected = InvalidStoryException.class)
    public void shouldThrowIfInvalidTdd() throws Exception {
        // GIVEN
        var au = getAuFixture();

        var featureStory = first(au.getCapabilityContainer().getFeatureStories());
        featureStory = featureStory.toBuilder().tddReferences(List.of(new TddId("Invalid TDD ID"))).build();

        // WHEN
        new JiraStory(featureStory, au, getArchitectureBeforeAu(), getArchitectureAfterAu());

        // THEN
        // Raise Error
    }

    private ArchitectureDataStructure getArchitectureBeforeAu() throws Exception {
        final String archAsString = new FilesFacade().readString(TestHelper.getPath(
                getClass(),
                MANIFEST_PATH_TO_TEST_JIRA_STORY_CREATION).toFile().toPath());
        return YAML_OBJECT_MAPPER.readValue(archAsString.replaceAll("29", "404"),
                ArchitectureDataStructure.class);
    }

    private ArchitectureDataStructure getArchitectureAfterAu() throws Exception {
        final String archAsString = new FilesFacade().readString(TestHelper.getPath(
                getClass(),
                MANIFEST_PATH_TO_TEST_JIRA_STORY_CREATION).toFile().toPath());
        return YAML_OBJECT_MAPPER.readValue(archAsString, ArchitectureDataStructure.class);
    }
}
