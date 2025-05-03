package net.chaossquad.mclib.advancedgui.provider;

import me.leoko.advancedgui.utils.Direction;
import me.leoko.advancedgui.utils.GuiLocation;
import me.leoko.advancedgui.utils.GuiSize;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A WallGUIProvider which manages its own item frame entities.
 */
public class EntityManagingWallGUIProvider extends WallGUIProvider {
    private final List<ItemFrame> itemFrames;

    /**
     * Creates a new EntityManagingWallGUIProvider.
     * @param layoutId layout id
     * @param location location
     * @param direction direction
     * @param interactionRadius interaction radius
     */
    public EntityManagingWallGUIProvider(@NotNull String layoutId, @NotNull Location location, @NotNull Direction direction, int interactionRadius) {
        this(layoutId, new GuiLocation(location, direction), interactionRadius);
    }

    /**
     * Creates a new EntityManagingWallGUIProvider.
     * @param layoutId layout id
     * @param guiLocation gui location
     * @param interactionRadius interaction radius
     */
    public EntityManagingWallGUIProvider(@NotNull String layoutId, @NotNull GuiLocation guiLocation, int interactionRadius) {
        super(layoutId, guiLocation, interactionRadius);
        this.itemFrames = new ArrayList<>();
    }

    // INSTANCE

    @Override
    public void register() {
        if (this.isRegistered()) return;
        this.spawnItemFrames();
        super.register();
    }

    @Override
    public void unregister() {
        super.unregister();
        this.clearItemFrames();
    }

    // ITEM FRAME ENTITIES

    /**
     * Clears the item frame entities.
     * <p/>
     * To prevent issues, the GUI will also be unregistered if it is registered.
     */
    private void clearItemFrames() {
        if (this.isRegistered()) this.unregister();
        for (ItemFrame itemFrame : List.copyOf(this.itemFrames)) {
            itemFrame.remove();
            this.itemFrames.remove(itemFrame);
        }
    }

    private void spawnItemFrames() {

        if (!this.itemFrames.isEmpty()) this.clearItemFrames();

        GuiLocation location = this.getLocation();
        Direction direction = location.getDirection();
        GuiSize size = this.getLayout().getSize();

        Vector basePoint = new Vector(location.getX(), location.getY(), location.getZ());

        for (int x = 0; x < size.getWidth(); x++) {

            for (int y = 0; y < size.getHeight(); y++) {
                Vector currentLocation = basePoint.clone();

                currentLocation.add(direction.getXDirection().clone().multiply(x));
                currentLocation.add(direction.getYDirection().clone().multiply(y));

                this.spawnItemFrame(currentLocation);
            }

        }

    }

    private void spawnItemFrame(Vector vector) {
        World world = this.getLocation().getWorld();

        ItemFrame itemFrame = world.spawn(vector.toLocation(world), ItemFrame.class);
        itemFrame.setFixed(true);
        itemFrame.setInvulnerable(true);
        itemFrame.setRotation(this.getLocation().getDirection().getFrameOrientation());
        itemFrame.setItemDropChance(0);

        this.itemFrames.add(itemFrame);
    }

    /**
     * Returns the item frames.
     * @return list of item frames
     */
    public final List<ItemFrame> getItemFrames() {
        return List.copyOf(this.itemFrames);
    }

}
