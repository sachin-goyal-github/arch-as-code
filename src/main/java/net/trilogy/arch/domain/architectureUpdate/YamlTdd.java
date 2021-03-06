package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;
import static lombok.AccessLevel.PRIVATE;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class YamlTdd {
    private static final String SAMPLE_TDD_TEXT = "[SAMPLE TDD TEXT LONG TEXT FORMAT]\nLine 2\nLine 3";
    private static final String SAMPLE_BLANK_TDD_ID = "[SAMPLE-TDD-ID]";
    private static final String SAMPLE_NO_PR_TDD_ID = "no-PR";
    private static final String SAMPLE_BLANK_TDD_COMPONENT_REFERENCE = "[SAMPLE-COMPONENT-ID]";

    @JsonProperty(value = "text")
    private final String text;
    // TODO: Why isn't this a JDK Path object?
    @JsonProperty(value = "file")
    private final String file;

    @Getter
    @JsonIgnore
    private final TddContent content;

    @JsonCreator(mode = PROPERTIES)
    public YamlTdd(@JsonProperty("text") String text,
                   @JsonProperty("file") String file) {
        this(text, file, null);
    }

    public static YamlTdd blank() {
        return new YamlTdd(SAMPLE_TDD_TEXT, null);
    }

    public YamlTdd withContent(final TddContent tddContent) {
        // TODO: Needs throught on why caller would pass `null` here
        return new YamlTdd(text, tddContent == null ? null : tddContent.getFilename(), tddContent);
    }

    public String getDetails() {
        return null == content ? text : content.getContent();
    }

    public static class TddId extends YamlId implements EntityReference {

        public TddId(String id) {
            super(id);
        }

        public static TddId blank() {
            return new TddId(SAMPLE_BLANK_TDD_ID);
        }

        public static TddId noPr() {
            return new TddId(SAMPLE_NO_PR_TDD_ID);
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class TddComponentReference implements EntityReference {
        @JsonValue
        @Getter
        private final String id;

        public static TddComponentReference blank() {
            return new TddComponentReference(SAMPLE_BLANK_TDD_COMPONENT_REFERENCE);
        }

        @Override
        public String toString() {
            // TODO: Implies that `toString` is used in business logic; it is
            //       intended for debugging
            return id;
        }
    }
}
