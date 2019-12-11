package net.nahknarmi.arch.transformation;

import com.structurizr.Workspace;
import com.structurizr.documentation.AutomaticDocumentationTemplate;
import com.structurizr.documentation.DecisionStatus;
import com.structurizr.documentation.Documentation;
import net.nahknarmi.arch.model.ArchitectureDataStructure;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.structurizr.documentation.DecisionStatus.Deprecated;
import static com.structurizr.documentation.DecisionStatus.*;
import static com.structurizr.documentation.Format.Markdown;
import static java.util.Optional.ofNullable;

public class ArchitectureDataStructureTransformer {

    public Workspace toWorkSpace(ArchitectureDataStructure dataStructure) throws IOException {
        Workspace workspace = new Workspace(dataStructure.getName(), dataStructure.getDescription());
        workspace.setId(dataStructure.getId());

        addDocumentation(workspace, dataStructure);
        addDecisions(workspace, dataStructure);

        return workspace;
    }

    private void addDocumentation(Workspace workspace, ArchitectureDataStructure dataStructure) throws IOException {
        URL resource = getClass().getResource(String.format("/architecture/products/%s/documentation/", dataStructure.getName().toLowerCase()));
        new AutomaticDocumentationTemplate(workspace).addSections(new File(resource.getPath()));
    }

    private void addDecisions(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getDecisions() != null) {
            Documentation documentation = workspace.getDocumentation();
            dataStructure
                    .getDecisions()
                    .forEach(d -> documentation.addDecision(d.getId(), d.getDate(), d.getTitle(), getDecisionStatus(d.getStatus()), Markdown, d.getContent()));
        }
    }

    private DecisionStatus getDecisionStatus(String status) {
        DecisionStatus decisionStatus;
        switch (ofNullable(status).orElse(Proposed.name()).toLowerCase()) {
            case "accepted":
                decisionStatus = Accepted;
                break;
            case "superseded":
                decisionStatus = Superseded;
                break;
            case "deprecated":
                decisionStatus = Deprecated;
                break;
            case "rejected":
                decisionStatus = Rejected;
                break;
            default:
                decisionStatus = Proposed;
                break;
        }
        return decisionStatus;
    }

}
