package mundotv.mtblockspot.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import mundotv.mtblockspot.config.Region;

public abstract class RegionDatabase {
    
    protected List<Region> regions;
    
    public Region getRegionByRadius(int x, int z, int r, String world) {
        for (Region rg:regions) {
            if (!rg.getWorld().equals(world)) {
                continue;
            }
            if (rg.inRadius(x, z, r)) {
                return rg;
            }
        }
        return null;
    }
    
    public List<Region> getRegionsByOwn(String own) {
        List<Region> rgs = new ArrayList();
        regions.stream().filter(rg -> (rg.getOwn().equals(own))).forEachOrdered(rg -> {
            rgs.add(rg);
        });
        return rgs;
    }
    
    public abstract boolean addRegion(Region r);
    public abstract boolean removeRegion(Region r);
    public abstract boolean updateRegion(Region r);
    public abstract void loadRegions() throws IOException;
}
