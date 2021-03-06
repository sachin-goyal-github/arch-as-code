package net.trilogy.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class C4Component extends Entity implements HasTechnology, HasUrl {
    protected String containerId;
    protected String containerAlias;
    @NonNull
    protected String technology;
    protected String url;

    @JsonProperty(value = "src-mappings")
    protected List<String> srcMappings = emptyList();

    @Builder(toBuilder = true)
    public C4Component(@NonNull String id,
                       String alias,
                       C4Path path,
                       @NonNull String name,
                       String description,
                       @Singular Set<C4Tag> tags,
                       @Singular Set<C4Relationship> relationships,
                       String containerId,
                       String containerAlias,
                       String technology,
                       String url,
                       List<String> srcMappings) {
        super(id, alias, path, name, description, tags, relationships);
        this.containerId = containerId;
        this.containerAlias = containerAlias;
        this.technology = technology;
        this.url = url;
        this.srcMappings = ofNullable(srcMappings).orElse(emptyList());
    }

    public String name() {
        return ofNullable(name).orElse(path.componentName().orElseThrow(() -> new IllegalStateException("Component name could not be derived.")));
    }

    @Override
    public C4Type getType() {
        return C4Type.COMPONENT;
    }

    @Override
    public C4Component shallowCopy() {
        return toBuilder().build();
    }
}
