package me.mundotv.mtblockspot.storange;

import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.Location;

public abstract class Regions {
    
    @Nullable
    public abstract Region getRegionByRadiuns(int x, int z);
    
    @Nullable
    public abstract Region getRegionByRadiuns(int x, int z, int r);

    public abstract boolean addRegion(Region r);

    public abstract boolean removeRegion(Region r);

    public abstract boolean updateRegion(Region r);
    
    public abstract boolean loadRegions();
    
    public abstract List<Region> getRegions(String own);
    
    public Region getRegionByRadiuns(Location loc) {
        return getRegionByRadiuns(loc.getBlockX(), loc.getBlockZ());
    }
    
    public Region getRegionByRadiuns(Location loc, int r) {
        return getRegionByRadiuns(loc.getBlockX(), loc.getBlockZ(), r);
    }
}
