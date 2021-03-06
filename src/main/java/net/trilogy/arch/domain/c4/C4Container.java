package net.trilogy.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4Container extends Entity implements HasTechnology, HasUrl {
    private String systemId;
    private String systemAlias;
    private String technology;
    private String url;

    @Builder(toBuilder = true)
    public C4Container(@NonNull String id,
                       String alias,
                       C4Path path,
                       @NonNull String name,
                       String description,
                       @Singular Set<C4Tag> tags,
                       @Singular Set<C4Relationship> relationships,
                       String systemId,
                       String systemAlias,
                       String technology,
                       String url) {
        super(id, alias, path, name, description, tags, relationships);
        this.systemId = systemId;
        this.systemAlias = systemAlias;
        this.technology = technology;
        this.url = url;
    }

    public String name() {
        return ofNullable(name)
                .orElse(path.containerName().orElseThrow(()
                        -> new IllegalStateException("Container name couldn't be extracted from " + path)));
    }

    @Override
    public C4Type getType() {
        return C4Type.CONTAINER;
    }

    @Override
    public C4Container shallowCopy() {
        return toBuilder().build();
    }

    public static class C4ContainerBuilder {
        public C4ContainerBuilder path(C4Path path) {
            if (path == null) return this;
            checkArgument(C4Type.CONTAINER.equals(path.type()), format("Path %s is not valid for Container.", path));
            this.path = path;
            return this;
        }
    }
}
