package net.chaossquad.mclib.advancedgui.synchronization;

import me.leoko.advancedgui.utils.Direction;
import me.leoko.advancedgui.utils.GuiLocation;
import me.leoko.advancedgui.utils.components.GroupComponent;
import me.leoko.advancedgui.utils.interactions.Interaction;
import net.chaossquad.mclib.advancedgui.AdvancedGUIUtils;
import net.chaossquad.mclib.advancedgui.other.ComponentTreeProvider;
import net.chaossquad.mclib.advancedgui.provider.WallGUIProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A specific type of WallGUIProvider that synchronizes the displayed content for all players.
 */
public class SynchronizedScreen extends WallGUIProvider implements ComponentTreeProvider {
    private final GroupComponent componentTree;

    /**
     * Create a new SynchronizedScreen.
     * @param layoutId layout id
     * @param location location
     * @param direction direction
     * @param interactionRadius interaction radius
     */
    public SynchronizedScreen(String layoutId, Location location, Direction direction, int interactionRadius) {
        this(layoutId, new GuiLocation(location, direction), interactionRadius);
    }

    /**
     * Create a new SynchronizedScreen.
     * @param layoutId layout id
     * @param location gui location
     * @param interactionRadius interaction radius
     */
    public SynchronizedScreen(@NotNull String layoutId, @NotNull GuiLocation location, int interactionRadius) {
        super(layoutId, location, interactionRadius);
        this.componentTree = this.getLayout().getTemplateComponentTree().clone(null);
    }

    /**
     * Synchronizes the player's components with the current components.
     * NEVER CALL THIS WHEN BEING INSIDE A CLICK ACTION OR ANY OTHER METHOD CALLED BY ADVANCEDGUI!
     * THIS WILL CAUSE A ConcurrentModificationException!
     * @param player The player the components should be updated for
     */
    public void updatePlayerScreens(Player player) {

        if (player == null) {
            return;
        }

        if (this.getInstance() == null) {
            return;
        }

        Interaction interaction = this.getInstance().getInteraction(player);

        if (interaction == null) {
            return;
        }

        AdvancedGUIUtils.setInteractionComponentTree(interaction, this.componentTree.clone(interaction));

    }

    /**
     * Synchronizes the player components with the current components.
     * NEVER CALL THIS WHEN BEING INSIDE A CLICK ACTION OR ANY OTHER METHOD CALLED BY ADVANCEDGUI!
     * THIS WILL CAUSE A ConcurrentModificationException!
     */
    public void updatePlayerScreens() {

        for (Player player : List.copyOf(this.getLocation().getWorld().getPlayers())) {
            this.updatePlayerScreens(player);
        }

    }

    /**
     * Returns the component tree to allow editing the components of this synchronized screen.
     * @return component tree to modify components
     */
    public GroupComponent getComponentTree() {
        return this.componentTree;
    }

}
