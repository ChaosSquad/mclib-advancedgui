package net.chaossquad.mclib.advancedgui.dynamic_components;

import me.leoko.advancedgui.utils.components.Component;
import me.leoko.advancedgui.utils.components.GroupComponent;
import net.chaossquad.mclib.advancedgui.AdvancedGUIUtils;
import net.chaossquad.mclib.advancedgui.other.ComponentTreeProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Handles dynamic component creation/deletion for AdvancedGUI.<br/>
 * This does basically the same as {@link DynamicComponentHandler}.
 */
public class DynamicComponentSystem {
    private final ComponentTreeProvider provider;
    private final String targetComponentId;
    private final GroupComponent templateComponents;

    /**
     * Creates a new DynamicComponentSystem.
     * @param provider provider of the AdvancedGUI component tree.
     * @param targetComponentId the component group that contains the dynamic components
     * @param templateComponents the component group that contains the template components
     */
    public DynamicComponentSystem(@NotNull ComponentTreeProvider provider, @NotNull String targetComponentId, @NotNull GroupComponent templateComponents) {
        this.provider = provider;
        this.targetComponentId = targetComponentId;
        this.templateComponents = templateComponents;
    }

    /**
     * Returns the group component that contains all the dynamic components.
     * @return group component that contains all dynamic components
     */
    public final GroupComponent getDynamicComponentGroup() {
        return this.provider.getComponentTree().locate(this.targetComponentId, GroupComponent.class);
    }

    /**
     * Returns a list of all door components.
     * @return list of all door components
     */
    public final List<GroupComponent> getDynamicComponents() {
        GroupComponent doorsComponent = this.getDynamicComponentGroup();
        List<GroupComponent> dynamicComponents = new LinkedList<>();

        for (Component component : List.copyOf(doorsComponent.getComponents())) {
            if (component instanceof GroupComponent) dynamicComponents.add((GroupComponent) component);
        }

        return List.copyOf(dynamicComponents);
    }

    /**
     * Builds the component id of the dynamic component id.<br/>
     * Reverses {@link #getDynamicComponentIdIdFromComponentId(String)}.
     * @param dynamicComponentId dynamic component id
     * @return component id
     */
    public String buildComponentId(@NotNull String dynamicComponentId) {
        return this.getDynamicComponentGroup().getId() + ":" + dynamicComponentId;
    }

    /**
     * Returns the dynamic component id of a component id
     * @param componentId door component id
     * @return door id
     */
    public String getDynamicComponentIdIdFromComponentId(@NotNull String componentId) {
        componentId = componentId.replace(this.getDynamicComponentGroup().getId(), "");
        String[] splitId = componentId.split(":");

        String id = "";
        for (int i = 1; i < splitId.length; i++) {
            id = id + splitId[i];
        }

        return id;
    }

    /**
     * Returns the group component for a specific dynamic component with the specified id
     * @param dynamicComponentId id of the dynamic component that should be found
     * @return dynamic component
     */
    public final GroupComponent getDynamicComponent(String dynamicComponentId) {
        Component component = AdvancedGUIUtils.findComponent(this.getDynamicComponentGroup(), this.buildComponentId(dynamicComponentId));
        return (component instanceof GroupComponent) ? (GroupComponent) component : null;
    }

    /**
     * Returns a subcomponent with the specified id for a specific dynamic component
     * @param dynamicComponentGroup the door component that should be used
     * @param subComponentId component sub id
     * @return component or null if not found
     */
    @ApiStatus.Internal
    private Component getDynamicSubComponent(@NotNull GroupComponent dynamicComponentGroup, @NotNull String subComponentId) {
        return dynamicComponentGroup.locate(dynamicComponentGroup.getId() + ":" + subComponentId);
    }

    /**
     * Returns a subcomponent with the specified id for a specific dynamic component
     * @param dynamicComponentId door id
     * @param subComponentId component sub id
     * @return component or null if not found
     */
    public final Component getDynamicSubComponent(@NotNull String dynamicComponentId, @NotNull String subComponentId) {
        GroupComponent doorComponent = this.getDynamicComponent(dynamicComponentId);
        if (doorComponent == null) return null;
        return this.getDynamicSubComponent(doorComponent, subComponentId);
    }

    // ADD/REMOVE DOORS

    /**
     * Updates the ids of copied components.
     * The specified group component's name must be already updated!
     * @param copy group component the ids should be copied from
     * @param baseId dynamic component group id
     */
    @ApiStatus.Internal
    private void updateCopiedComponentIds(@NotNull String baseId, @NotNull GroupComponent copy) {

        for (Component component : List.copyOf(copy.getComponents())) {

            String componentId = component.getId().replace(this.templateComponents.getId() + ":", "");
            AdvancedGUIUtils.updateComponentId(component, baseId + ":" + componentId);

            if (component instanceof GroupComponent groupComponent) {
                this.updateCopiedComponentIds(baseId, groupComponent);
            }

        }

    }

    /**
     * Creates a full copy of the door template.
     * @param dynamicComponentId door id
     * @return group component for the specified door id
     */
    private GroupComponent updateCopiedComponentIds(@NotNull String dynamicComponentId) {

        GroupComponent copy = this.templateComponents.clone(null);
        AdvancedGUIUtils.updateComponentId(copy, this.buildComponentId(dynamicComponentId));

        this.updateCopiedComponentIds(copy.getId(), copy);

        return copy;
    }

    /**
     * Creates a new dynamic group component tree from the template or returns the existing one if it already exists.
     * @param dynamicComponentId component id
     * @return dynamic group component that was created
     */
    public final GroupComponent addDynamicComponent(@NotNull String dynamicComponentId) {

        // Return component if it already exists
        GroupComponent door = this.getDynamicComponent(dynamicComponentId);
        if (door != null) return door;

        // Create new component from template if it does not exist
        door = this.updateCopiedComponentIds(dynamicComponentId);

        // Add the new component and return it
        this.getDynamicComponentGroup().getComponents().add(door);
        return door;
    }

    /**
     * Removes a dynamic component group with the specified id.
     * @param dynamicComponentId dynamic component id
     */
    public final void removeDynamicComponent(String dynamicComponentId) {
        Component component = AdvancedGUIUtils.findComponent(this.getDynamicComponentGroup(), this.buildComponentId(dynamicComponentId));
        this.getDynamicComponentGroup().getComponents().remove(component);
    }

    // OTHER

    /**
     * Returns the ComponentTreeProvider.
     * @return component tree provider
     */
    public ComponentTreeProvider getProvider() {
        return this.provider;
    }

}
