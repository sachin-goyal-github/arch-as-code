package net.nahknarmi.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ViewSet;
import lombok.NonNull;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Path;
import net.nahknarmi.arch.domain.c4.view.C4ComponentView;
import net.nahknarmi.arch.domain.c4.view.ModelMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

public class ComponentContextViewEnhancer extends BaseViewEnhancer<ComponentView, C4ComponentView> {
    private static Logger log = LoggerFactory.getLogger(ComponentContextViewEnhancer.class);

    @Override
    public List<C4ComponentView> getViews(ArchitectureDataStructure dataStructure) {
        return dataStructure.getViews().getComponentViews();
    }

    @Override
    public ComponentView createView(Workspace workspace, C4ComponentView componentView) {
        ViewSet viewSet = workspace.getViews();
        @NonNull C4Path containerPath = componentView.getContainerPath();
        String systemName = containerPath.systemName();
        String containerName = containerPath.containerName()
                .orElseThrow(() -> new IllegalStateException("Container name not found in path " + containerPath));
        Model workspaceModel = workspace.getModel();
        SoftwareSystem softwareSystem = workspaceModel.getSoftwareSystemWithName(systemName);
        Container container = softwareSystem.getContainerWithName(containerName);

        return viewSet.createComponentView(container, componentView.getKey(), componentView.getDescription());
    }

    public Consumer<C4Path> addEntity(ModelMediator modelMediator, ComponentView view) {
        return entityPath -> {

            switch (entityPath.type()) {
                case person:
                    view.add(modelMediator.person(entityPath));
                    break;
                case system:
                    view.add(modelMediator.softwareSystem(entityPath));
                    break;
                case container:
                    view.add(modelMediator.container(entityPath));
                    break;
                case component:
                    Container container = view.getContainer();
                    Component component = modelMediator.component(entityPath);

                    if (component.getContainer().equals(container)) {
                        view.add(component);
                    } else {
                        log.warn("Only components belonging to " + container + " can be added to view (" + component + " not added.).");
                    }
                    break;
                default:
                    throw new IllegalStateException("Unsupported type " + entityPath.type());
            }
        };
    }
}