package net.trilogy.arch.validation.architectureUpdate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.trilogy.arch.domain.architectureUpdate.Decision;
import net.trilogy.arch.domain.architectureUpdate.EntityReference;
import net.trilogy.arch.domain.architectureUpdate.Tdd;

@ToString
@Getter
@EqualsAndHashCode
public class ValidationError {
    private final ValidationErrorType validationErrorType;
    private final EntityReference element;
    private final String description;

    public static ValidationError forMissingTddReference(Decision.Id entityId) {
        return new ValidationError(
                ValidationErrorType.MISSING_TDD,
                entityId,
                String.format("Decision \"%s\" must have at least one TDD reference.", entityId.getId())
        );
    }

    public static ValidationError forInvalidTddReference(Decision.Id entityId, Tdd.Id tddId) {
        return new ValidationError(
                ValidationErrorType.INVALID_TDD_REFERENCE,
                entityId,
                String.format("Decision \"%s\" contains invalid TDD reference \"%s\".", entityId.getId(), tddId.getId())
        );
    }

    public static ValidationError forTddsWithoutStories(Tdd.Id entityId) {
        return new ValidationError(
                ValidationErrorType.MISSING_CAPABILITY,
                entityId,
                String.format("TDD \"%s\" is not referred to by a story.", entityId.getId())
        );
    }

    private ValidationError(ValidationErrorType validationErrorType, EntityReference element, String description) {
        this.validationErrorType = validationErrorType;
        this.element = element;
        this.description = description;
    }
}
