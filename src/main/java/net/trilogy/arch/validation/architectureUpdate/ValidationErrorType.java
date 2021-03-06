package net.trilogy.arch.validation.architectureUpdate;

import lombok.Getter;

import static net.trilogy.arch.validation.architectureUpdate.ValidationStage.STORY;
import static net.trilogy.arch.validation.architectureUpdate.ValidationStage.TDD;

public enum ValidationErrorType {
    INVALID_TDD_REFERENCE_IN_DECISION_OR_REQUIREMENT("Invalid TDD Reference in Decision or Requirement", TDD),
    INVALID_FUNCTIONAL_REQUIREMENT_REFERENCE_IN_STORY("Invalid Functional Requirement Reference in Story", STORY),
    MISSING_CAPABILITY("Missing Capability", STORY),
    DECISION_MISSING_TDD("Decision Missing TDD", TDD),
    STORY_MISSING_TDD("Story Missing TDD", STORY),
    MISSING_FUNCTIONAL_REQUIREMENTS("Story Missing Functional Requirement", STORY),
    TDD_WITHOUT_CAUSE("TDD without cause", TDD),
    INVALID_TDD_REFERENCE_IN_STORY("Invalid TDD Reference in Story", STORY),
    INVALID_COMPONENT_REFERENCE("Invalid Component Reference", TDD),
    INVALID_DELETED_COMPONENT_REFERENCE("Invalid Deleted Component Reference", TDD),
    DUPLICATE_TDD_ID("Duplicate TDD ID", TDD),
    DUPLICATE_COMPONENT_ID("Duplicate Component ID", TDD),
    LINK_NOT_AVAILABLE("Link value is N/A", TDD),
    NO_PR_COMBINED_WITH_ANOTHER_TDD("No-Pr is combined with another TDD", TDD),
    COMPONENT_ID_NOT_MATCHING_PATH("ComponentId is not matching component path", TDD),
    AMBIGUOUS_TDD_CONTENT_REFERENCE("Ambiguous TDD content reference", TDD),
    MULTIPLE_TDD_CONTENT_FILES_REFERENCE_TDD("Multiple TDD content files associated with one TDD", TDD),
    OVERRIDDEN_BY_TDD_CONTENT_FILE("TDD content overridden by file", TDD);

    @Getter
    private final String label;
    @Getter
    private final ValidationStage stage;

    ValidationErrorType(String label, ValidationStage stage) {
        this.label = label;
        this.stage = stage;
    }

    @Override
    public String toString() {
        return label;
    }
}
