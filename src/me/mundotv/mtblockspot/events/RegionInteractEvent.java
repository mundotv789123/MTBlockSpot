package me.mundotv.mtblockspot.events;

import javax.annotation.Nullable;
import me.mundotv.mtblockspot.storange.Region;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class RegionInteractEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Event event;
    private final Region region;
    protected Block block;
    protected boolean cancel;

    public RegionInteractEvent(Player who, Region region, Event event) {
        super(who);
        this.region = region;
        this.event = event;
    }
    
    public Region getRegion() {
        return region;
    }

    public Event getEvent() {
        return event;
    }
    
    @Nullable
    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
    
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean bln) {
        this.cancel = bln;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
