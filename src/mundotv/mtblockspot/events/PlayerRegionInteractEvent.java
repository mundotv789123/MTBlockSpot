package mundotv.mtblockspot.events;

import mundotv.mtblockspot.config.Region;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerRegionInteractEvent extends PlayerEvent implements Cancellable {

    private final static HandlerList handles = new HandlerList();
    
    private final Region region;
    private final Event event;
    
    private boolean cancel;

    public PlayerRegionInteractEvent(Region region, Event event, Player who) {
        super(who);
        this.region = region;
        this.event = event;
    }
    
    @Override
    public  HandlerList getHandlers() {
        return handles;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean bln) {
        this.cancel = bln;
    }

    public Region getRegion() {
        return region;
    }

    public Event getEvent() {
        return event;
    }
    
    public static HandlerList getHandlerList() {
        return handles;
    }
}
