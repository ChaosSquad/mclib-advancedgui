package net.chaossquad.mclib.advancedgui.dynamic_components;

import me.leoko.advancedgui.utils.components.Component;
import me.leoko.advancedgui.utils.components.GroupComponent;
import net.chaossquad.mclib.advancedgui.AdvancedGUIUtils;
import net.chaossquad.mclib.advancedgui.other.ComponentTreeProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Handles dynamic component creation/deletion for AdvancedGUI.<br/>
 * This does basically the same as {@link DynamicComponentSystem}.
 */
public class DynamicComponentHandler {
    private final String DYNAMIC_OBJECT_COMPONENT_GROUP_ID;
    private final String TEMPLATE_COMPONENT_GROUP_ID;
    private final DynamicObjectProvider provider;

    /**
     * Creates a DynamicComponentHandler.
     * @param DYNAMIC_OBJECT_COMPONENT_GROUP_ID the component group id of the component that contains the dynamically created components
     * @param TEMPLATE_COMPONENT_GROUP_ID the component group id of the component that contains the template components
     * @param provider the provider that provides the ids of the dynamic objects the DynamicComponentHandler should create dynamic components for.
     */
    public DynamicComponentHandler(
            @NotNull String DYNAMIC_OBJECT_COMPONENT_GROUP_ID,
            @NotNull String TEMPLATE_COMPONENT_GROUP_ID,
            @NotNull DynamicObjectProvider provider
    ) {
        this.provider = provider;
        this.DYNAMIC_OBJECT_COMPONENT_GROUP_ID = DYNAMIC_OBJECT_COMPONENT_GROUP_ID;
        this.TEMPLATE_COMPONENT_GROUP_ID = TEMPLATE_COMPONENT_GROUP_ID;
    }

    // ID BUILDING

    /**
     * Builds the component id string of a dynamic object with the specified id
     * @param dynamicObjectId dynamic objet id
     * @return dynamic object component id
     */
    public String buildDynamicObjectComponentId(String dynamicObjectId) {
        return DYNAMIC_OBJECT_COMPONENT_GROUP_ID + ":" + dynamicObjectId;
    }

    /**
     * Returns the dynamic object id of a dynamic object component id
     * @param componentId door component id
     * @return door id
     */
    public String getDynamicObjectIdFromComponentId(String componentId) {
        componentId = componentId.replace(DYNAMIC_OBJECT_COMPONENT_GROUP_ID + ":", "");
        String[] splitId = componentId.split(":");
        if (splitId.length < 1) return null;
        return splitId[0];
    }

    // LOCATE COMPONENTS

    /**
     * Returns the component that contains all registered dynamic objects
     * @return component that contains all components for the dynamic objects
     */
    public GroupComponent getDynamicObjectsComponent() {
        return this.provider.getComponentTree().locate(DYNAMIC_OBJECT_COMPONENT_GROUP_ID, GroupComponent.class);
    }

    /**
     * Returns a list of all components of the dynamic objects.
     * @return list of all components of dynamic objects
     */
    public List<GroupComponent> getDynamicObjectComponents() {
        GroupComponent dynamicObjectsComponent = this.getDynamicObjectsComponent();
        List<GroupComponent> dynamicObjectComponent = new LinkedList<>();

        for (Component component : List.copyOf(dynamicObjectsComponent.getComponents())) {
            if (component instanceof GroupComponent groupComponent) dynamicObjectComponent.add(groupComponent);
        }

        return List.copyOf(dynamicObjectComponent);
    }

    /**
     * Returns the component for a specific dynamic object with the specified dynamic object id
     * @param dynamicObjectId id of the dynamic object that should be found
     * @return dynamic object component
     */
    public GroupComponent getDynamicObjectComponent(String dynamicObjectId) {
        Component component = AdvancedGUIUtils.findComponent(this.getDynamicObjectsComponent(), this.buildDynamicObjectComponentId(dynamicObjectId));
        return (component instanceof GroupComponent groupComponent) ? groupComponent : null;
    }

    /**
     * Returns a subcomponent with the specified id for a specific dynamic object
     * @param dynamicObjectComponent the dynamic object component that should be used
     * @param componentId component sub id
     * @return component or null if not found
     */
    @ApiStatus.Internal
    private Component getDoorSubComponent(GroupComponent dynamicObjectComponent, String componentId) {
        return AdvancedGUIUtils.findComponent(dynamicObjectComponent, dynamicObjectComponent.getId() + ":" + componentId);
    }

    /**
     * Returns a subcomponent with the specified id for a specific dynamic object
     * @param dynamicObjectId dynamic object id
     * @param componentId component sub id
     * @return component or null if not found
     */
    public Component getDoorSubComponent(String dynamicObjectId, String componentId) {
        GroupComponent dynamicObjectComponent = this.getDynamicObjectComponent(dynamicObjectId);
        if (dynamicObjectComponent == null) return null;
        return this.getDoorSubComponent(dynamicObjectComponent, componentId);
    }

    // ADD/REMOVE

    @ApiStatus.Internal
    private void copyFromTemplateRecursiveComponentIdUpdate(GroupComponent group, String id) {
        String templateId = group.getId();

        AdvancedGUIUtils.updateComponentId(group, id);

        for (Component component : group.getComponents()) {

            AdvancedGUIUtils.updateComponentId(component, group.getId() + ":" + component.getId().replace(templateId + ":", ""));

            if (component instanceof GroupComponent groupComponent) {
                this.copyFromTemplateRecursiveComponentIdUpdate(groupComponent, groupComponent.getId());
            }

        }

    }

    /**
     * Creates a full copy of the door template.
     * @param dynamicObjectId door id
     * @return group component for the specified door id
     */
    private GroupComponent copyFromTemplate(String dynamicObjectId) {

        GroupComponent copy = this.provider.getComponentTree().locate(TEMPLATE_COMPONENT_GROUP_ID, GroupComponent.class);
        if (copy == null) return null;
        copy = copy.clone(null).clone(null);

        String dynamicComponentId = this.buildDynamicObjectComponentId(dynamicObjectId);
        this.copyFromTemplateRecursiveComponentIdUpdate(copy, dynamicComponentId);

        return copy;
    }

    /**
     * Adds a new dynamic object component or returns the existing one if it already exists.
     * @param dynamicObjectId dynamic object id
     * @return group component of the specified door
     */
    @SuppressWarnings("UnusedReturnValue")
    public GroupComponent addDynamicObjectComponent(String dynamicObjectId) {
        GroupComponent dynamicObjectComponent = this.getDynamicObjectComponent(dynamicObjectId);
        if (dynamicObjectComponent != null) return dynamicObjectComponent;

        dynamicObjectComponent = this.copyFromTemplate(dynamicObjectId);

        this.getDynamicObjectsComponent().getComponents().add(dynamicObjectComponent);
        return dynamicObjectComponent;
    }

    /**
     * Removes a door with the specified door id
     * @param dynamicObjectId door id
     */
    public void removeDynamicObjectComponent(String dynamicObjectId) {
        Component component = AdvancedGUIUtils.findComponent(this.getDynamicObjectsComponent(), this.buildDynamicObjectComponentId(dynamicObjectId));
        this.getDynamicObjectsComponent().getComponents().remove(component);
    }

    // SYNC

    /**
     * Synchronizes the dynamic components with the object ids provides by the DynamicObjectProvider.<br/>
     * This has to be called by the user of this (for example in a Bukkit task) class and does the job for which this class is supposed to do.
     */
    public void syncDoorComponents() {

        for (GroupComponent dynamicObjectComponent : this.getDynamicObjectComponents()) {
            String dynamicObjectId = this.getDynamicObjectIdFromComponentId(dynamicObjectComponent.getId());

            if (dynamicObjectId == null) {
                this.getDynamicObjectsComponent().getComponents().remove(dynamicObjectComponent);
                continue;
            }

            if (!this.provider.getDynamicObjectIds().contains(dynamicObjectId)) {
                this.getDynamicObjectsComponent().getComponents().remove(dynamicObjectComponent);
                continue;
            }

        }

        Set<String> dynamicObjects = this.provider.getDynamicObjectIds();
        for (String dynamicObjectId : dynamicObjects) {

            GroupComponent doorComponent = this.getDynamicObjectComponent(dynamicObjectId);

            if (doorComponent == null) {
                this.addDynamicObjectComponent(dynamicObjectId);
                continue;
            }

        }

    }

    // INNER CLASSES

    /**
     * Provides ids of the dynamic objects.
     */
    public interface DynamicObjectProvider extends ComponentTreeProvider {

        /**
         * Provides ids of the dynamic objects.
         * @return set of dynamic object ids
         */
        Set<String> getDynamicObjectIds();

    }

}
