package mundotv.mtblockspot.events;

import mundotv.mtblockspot.config.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class RegionInteractEvent extends PlayerEvent implements Cancellable {
    
    private final static HandlerList handles = new HandlerList();
    private boolean cancel;
    private final Event event;
    private final Region region;
    private final Location location;
    

    public RegionInteractEvent(Player who, Region region, Event event, Location location) {
        super(who);
        this.region = region;
        this.event = event;
        this.location = location;
    }

    public Region getRegion() {
        return region;
    }

    public Event getEvent() {
        return event;
    }

    public Location getLocation() {
        return location;
    }
    
    @Override
    public HandlerList getHandlers() {
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
    
    public static HandlerList getHandlerList() {
        return handles;
    }
}
