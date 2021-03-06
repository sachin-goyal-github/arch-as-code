package net.trilogy.arch.commands.architectureUpdate;

import net.trilogy.arch.CommandTestBase;
import org.eclipse.jgit.api.Git;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static java.nio.file.Files.delete;
import static java.nio.file.Files.move;
import static net.trilogy.arch.TestHelper.ROOT_PATH_TO_TEST_AU_VALIDATION_E2E;
import static net.trilogy.arch.TestHelper.execute;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class AuValidateCommandTest extends CommandTestBase {
    private File rootDir;
    private Path auDir;
    private Git git;

    @Before
    public void setUp() throws Exception {
        var buildDir = new File(getClass().getResource(ROOT_PATH_TO_TEST_AU_VALIDATION_E2E).getPath());

        rootDir = buildDir.toPath().resolve("git").toFile();
        auDir = rootDir.toPath().resolve("architecture-updates/");

        copyDirectory(buildDir, rootDir);
        git = Git.init().setDirectory(rootDir).call();

        setUpRealisticGitRepository();
    }

    @After
    public void tearDown() throws Exception {
        deleteDirectory(rootDir);
    }

    private void setUpRealisticGitRepository() throws Exception {
        move(
                rootDir.toPath().resolve("master-branch-product-architecture.yml"),
                rootDir.toPath().resolve("product-architecture.yml"));
        git.add().addFilepattern("product-architecture.yml").call();
        git.commit().setMessage("add architecture to master").call();

        git.add().addFilepattern("architecture-updates").call();
        git.commit().setMessage("add AU yamls").call();

        git.checkout().setCreateBranch(true).setName("au").call();
        delete(rootDir.toPath().resolve("product-architecture.yml"));
        move(
                rootDir.toPath().resolve("au-branch-product-architecture.yml"),
                rootDir.toPath().resolve("product-architecture.yml"));
        git.add().addFilepattern("product-architecture.yml").call();
        git.commit().setMessage("change architecture in au").call();
    }

    @Test
    public void shouldBeFullyValid() {
        var auPath = auDir.resolve("blank/").toString();

        final var status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(dummyOut.getLog(), containsString("Success, no errors found."));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
    }

    @Test
    public void shouldBeFullyValidWithTddContent() {
        final var auPath = auDir.resolve("tdd-content-valid/").toString();

        final var status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(dummyOut.getLog(), containsString("Success, no errors found."));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
    }

    @Test
    public void shouldBeTDDValid() {
        final var auPath = auDir.resolve("invalid-capabilities/").toString();

        final var status = execute("architecture-update", "validate", "-b", "master", "-t", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(dummyOut.getLog(), containsString("Success, no errors found."));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
    }

    @Test
    public void shouldBeStoryValid() {
        final var auPath = auDir.resolve("invalid-tdds/").toString();

        final var status = execute("architecture-update", "validate", "-b", "master", "-s", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(dummyOut.getLog(), containsString("Success, no errors found."));
        collector.checkThat(dummyErr.getLog(), equalTo(""));
    }

    @Test
    public void shouldPresentErrorsNicely() {
        final var auPath = auDir.resolve("both-invalid/").toString();

        final var status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(
                dummyErr.getLog(),
                equalTo("" +
                        "Decision Missing TDD:\n" +
                        "    Decision \"[SAMPLE-DECISION-ID]\" must have at least one TDD reference.\n" +
                        "Invalid Component Reference:\n" +
                        "    Component id \"[INVALID-COMPONENT-ID]\" does not exist.\n" +
                        "Story Missing TDD:\n" +
                        "    Story \"[SAMPLE FEATURE STORY TITLE]\" must have at least one TDD reference.\n" +
                        "Missing Capability:\n" +
                        "    TDD \"[SAMPLE-TDD-ID]\" needs to be referenced in a story.\n" +
                        ""));
    }

    @Test
    public void shouldBeFullyInvalid() {
        final var auPath = auDir.resolve("both-invalid/").toString();

        final var status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(),
                containsString("Decision \"[SAMPLE-DECISION-ID]\" must have at least one TDD reference."));
        collector.checkThat(dummyErr.getLog(),
                containsString("TDD \"[SAMPLE-TDD-ID]\" needs to be referenced in a story."));
        collector.checkThat(dummyErr.getLog(),
                containsString("Component id \"[INVALID-COMPONENT-ID]\" does not exist."));
    }

    @Test
    public void shouldBeTddContentInvalid() {
        final var auPath = auDir.resolve("tdd-content-invalid/").toString();

        final var status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(),
                containsString("TDD content file \"TDD 1.0 : Component-38.md\" matching Component id \"38\" and TDD \"TDD 1.0\" will override existing TDD text."));
    }

    @Test
    public void shouldBeTddInvalid() {
        final var auPath = auDir.resolve("both-invalid/").toString();

        final var status = execute("au", "validate", "-b", "master", "--TDDs", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(),
                containsString("Decision \"[SAMPLE-DECISION-ID]\" must have at least one TDD reference."));
        collector.checkThat(dummyErr.getLog(),
                not(containsString("TDD \"[SAMPLE-TDD-ID]\" needs to be referenced in a story.")));
        collector.checkThat(dummyErr.getLog(),
                containsString("Component id \"[INVALID-COMPONENT-ID]\" does not exist."));
    }

    @Test
    public void shouldBeStoryInvalid() {
        final var auPath = auDir.resolve("both-invalid/").toString();

        final var status = execute("au", "validate", "-b", "master", "--stories", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(dummyErr.getLog(),
                not(containsString("Component id \"[INVALID-COMPONENT-ID]\" does not exist.")));
        collector.checkThat(dummyErr.getLog(),
                not(containsString("Decision \"[SAMPLE-DECISION-ID]\" must have at least one TDD reference.")));
        collector.checkThat(dummyErr.getLog(),
                containsString("TDD \"[SAMPLE-TDD-ID]\" needs to be referenced in a story."));
    }

    @Test
    public void shouldFindErrorsAcrossGitBranches() {
        final var auPath = auDir.resolve("invalid-deleted-component/").toString();

        final var status = execute("architecture-update", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyErr.getLog(), containsString("Deleted component id \"deleted-component-invalid\" is invalid. (Checked architecture in \"master\" branch.)"));
    }

    @Test
    public void shouldHandleIfGitReaderFails() {
        final var auPath = auDir.resolve("architecture-updates/blank/").toString();

        final var status = execute("architecture-update", "validate", "-b", "invalid", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyErr.getLog(), containsString("Unable to load 'invalid' branch architecture\nError: net.trilogy.arch.adapter.git.GitInterface$BranchNotFoundException"));
    }

    @Test
    public void shouldHandleIfUnableToLoadArchitecture() {
        final var auPath = auDir.resolve("architecture-updates/blank/").toString();

        final var status = execute("architecture-update", "validate", "-b", "master", auPath, rootDir.getAbsolutePath() + "invalid");

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyErr.getLog(), containsString("Error: java.nio.file.NoSuchFileException"));
    }

    @Test
    public void shouldFindAUStructureErrors() {
        final var auPath = auDir.resolve("invalid-structure/").toString();

        final var status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));

        collector.checkThat(
                dummyErr.getLog(),
                containsString("Duplicate field '[SAMPLE-TDD-ID]'"));
    }

    @Test
    public void shouldFindAUSchemaErrors() {
        final var auPath = auDir.resolve("invalid-schema/").toString();

        final var status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(
                dummyErr.getLog(),
                containsString("Unrecognized field \"notext\""));
    }

    @Test
    public void shouldPassAUSchemaValidation() {
        final var auPath = auDir.resolve("valid-schema/").toString();

        final var status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(dummyErr.getLog(), equalTo(""));
        collector.checkThat(status, equalTo(0));
    }

    @Test
    public void shouldBeInValidForNoPR() {
        final var auPath = auDir.resolve("noPR/").toString();

        final var status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(dummyOut.getLog(), equalTo(""));
        collector.checkThat(
                dummyErr.getLog(),
                containsString("No-Pr is combined with another TDD"));
    }
}
