package net.chaossquad.mclib.advancedgui;

import me.leoko.advancedgui.utils.GuiPoint;
import me.leoko.advancedgui.utils.GuiWallInstance;
import me.leoko.advancedgui.utils.components.Component;
import me.leoko.advancedgui.utils.components.GroupComponent;
import me.leoko.advancedgui.utils.interactions.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Utilities for the AdvancedGUI plugin.
 * Requires that the AdvancedGUI plugin is installed.
 */
public final class AdvancedGUIUtils {

    private AdvancedGUIUtils() {}

    /**
     * Updates the component id of the specified component via reflection since there is no way to clone a component with changed id.
     * @param component the component the name should be updated from
     * @param id the new id
     * @return success
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean updateComponentId(Component component, String id) {
        try {
            Field idFIeld = Component.class.getDeclaredField("id");
            idFIeld.setAccessible(true);
            idFIeld.set(component, id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Search for a specific component without searching all child components.
     * @param component group component
     * @param id component id to search for
     * @return found component or null when no result
     */
    public static Component findComponent(GroupComponent component, String id) {

        for (Component c : List.copyOf(component.getComponents())) {
            if (c.getId().equals(id)) return c;
        }

        return null;
    }

    /**
     * Replaces the component tree of an {@link Interaction} with a copy the specified {@link GroupComponent}.
     * For safety reasons, this method will clone the specified component tree and set the interaction of the component tree to the target interaction.
     * @param target target Interaction
     * @param componentTree GroupComponent to set
     * @return success
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean setInteractionComponentTree(@NotNull Interaction target, @NotNull GroupComponent componentTree) {

        try {
            Field field = Interaction.class.getDeclaredField("componentTree");
            field.setAccessible(true);

            field.set(target, componentTree.clone(target));

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * Checks if the player can interact with the gui. It uses the vanilla interaction radius for that.<br/>
     * This is necessary because AdvancedGUI does not check if a player is near enough to interact with the gui.
     * @param instance gui instance the player has interacted with
     * @param point the point where the player has clicked
     * @param player the player that has interacted
     * @param checkForBlocks if this method should do a raytrace to check if blocks are between the player and the gui
     * @return true = player can interact
     */
    public static boolean canWallGuiBeInteractPhysically(@NotNull GuiWallInstance instance, @NotNull GuiPoint point, @NotNull Player player, boolean checkForBlocks) {

        Vector interactedLocation = guiPointToWorldLocation(instance, point);
        Vector playerLocation = player.getEyeLocation().toVector().clone();

        double distance = interactedLocation.distance(playerLocation);

        double maxDistance;
        switch (player.getGameMode()) {
            case SURVIVAL, ADVENTURE -> maxDistance = 4.5;
            case CREATIVE -> maxDistance = 5.0;
            default -> maxDistance = 0;
        }

        if (distance > maxDistance) {
            return false;
        }

        if (checkForBlocks) {
            RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation().clone(), player.getEyeLocation().getDirection().clone(), distance);
            if (result != null) return false;
        }

        return true;
    }

    /**
     * Converts the pixel location on a wall gui to the location in the world.
     * @param instance gui instance
     * @param point location on the gui
     * @return location in the world
     */
    public static @NotNull Vector guiPointToWorldLocation(@NotNull GuiWallInstance instance, @NotNull GuiPoint point) {

        // click point and screen sizes
        double x = point.getX();
        double y = point.getY();
        double screenMaxX = instance.getLayout().getSize().getWidth() * 128d;
        double screenMaxY = instance.getLayout().getSize().getHeight() * 128d;

        // Direction vectors
        Vector xDir = instance.getLocation().getDirection().getXDirection();
        Vector yDir = instance.getLocation().getDirection().getYDirection();

        // Size
        Vector worldWidth = xDir.clone().multiply(instance.getLayout().getSize().getWidth());
        Vector worldHeight = yDir.clone().multiply(instance.getLayout().getSize().getHeight());

        // World relative position
        Vector relativeWorldPositionX = worldWidth.clone().multiply(x / screenMaxX);
        Vector relativeWorldPositionY = worldHeight.clone().multiply(y / screenMaxY);
        Vector relativeWorldPosition = relativeWorldPositionX.clone().add(relativeWorldPositionY);

        // World absolute position
        return getGUIOriginLocation(instance).add(relativeWorldPosition);
    }

    /**
     * Returns the location vector of the GUI coordinate system origin in world coordinates.
     * @param instance gui instance
     * @return origin
     */
    public static @NotNull Vector getGUIOriginLocation(@NotNull GuiWallInstance instance) {
        return instance.getLocation().toVector().clone().add(instance.getLocation().getDirection().getGuiSupportVector());
    }

}
