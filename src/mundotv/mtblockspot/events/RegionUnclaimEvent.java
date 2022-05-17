package mundotv.mtblockspot.events;

import mundotv.mtblockspot.config.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class RegionUnclaimEvent extends RegionInteractEvent{
    public RegionUnclaimEvent(Player who, Region region, Event event, Location location) {
        super(who, region, event, location);
    }
}
