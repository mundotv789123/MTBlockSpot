package mundotv.mtblockspot.config;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class Region {

    private final String own, block, world;
    private final List<String> players;
    private final int x, y, z, r;
    private final RegionOptions options;

    public Region(String own, String block, String world, int x, int y, int z, int r) {
        this.own = own;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.block = block;
        this.players = new ArrayList();
        this.options = new RegionOptions();
    }

    public Region(Player own, BlockSpot block, Location loc) {
        World w = loc.getWorld();
        this.own = own.getName();
        this.world = w != null ? w.getName() : "null";
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
        this.r = block.getRadius();
        this.block = block.getName();
        this.players = new ArrayList();
        this.options = new RegionOptions();
    }

    public Region(String own, String block, String world, int x, int y, int z, int r, List<String> players, RegionOptions options) {
        this.own = own;
        this.block = block;
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.players = players;
        this.options = options;
        this.world = world;
    }

    public boolean inRadius(int x, int z, int r) {
        return (this.x + (this.r + r) >= x && this.x - (this.r + r) <= x) && (this.z + (this.r + r) >= z && this.z - (this.r + r) <= z);
    }

    public boolean hasPermission(String player) {
        return (own.equals(player) || players.contains(player));
    }

    public boolean isBlockLocation(int x, int y, int z) {
        return (this.x == x) && (this.y == y) && (this.z == z);
    }

    public boolean isBlockLocation(Location loc) {
        return isBlockLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public RegionOptions getOptions() {
        return options;
    }

    public String getOwn() {
        return own;
    }

    public String getWorld() {
        return world;
    }

    public String getBlockName() {
        return block;
    }

    public List<String> getPlayers() {
        return players;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getR() {
        return r;
    }

    /* trace */
    private Location sendBlockChange(Player p, Location loc) {
        /*if (loc.getBlock().getType().equals(Material.AIR)) {
            loc.add(0, -10, 0);
        }
        for (int i = 10; (i > 1); i--) {
            if (loc.add(0, 1, 0).getBlock().getType().equals(Material.AIR)) {
                break;
            }
        }

        loc.add(0, -1, 0);*/
        loc.setY(p.getLocation().getY());
        p.sendBlockChange(loc, Bukkit.createBlockData(Material.TORCH));
        return loc;
    }

    public static boolean removeTraceRadiuns(Player p, Plugin main) {
        if (p.hasMetadata("mtspotblock.strace") && !p.getMetadata("mtspotblock.strace").isEmpty()) {
            MetadataValue mv = p.getMetadata("mtspotblock.strace").get(0);
            if (mv.value() != null && (mv.value() instanceof List)) {
                List<Location> locs = (List<Location>) mv.value();
                if (locs != null) {
                    locs.forEach(loc -> {
                        p.sendBlockChange(loc, loc.getBlock().getBlockData());
                    });
                }
            }
            p.removeMetadata("mtspotblock.strace", main);
            return true;
        }
        return false;
    }

    public void traceRadiuns(Player p, Plugin main) {
        removeTraceRadiuns(p, main);

        Location loc = new Location(p.getLocation().getWorld(), x, y, z);

        List<Location> locs = new ArrayList();
        /*locs.add(sendBlockChange(p, loc.clone().add(r, 0, 0)));
        locs.add(sendBlockChange(p, loc.clone().add(0, 0, r)));
        locs.add(sendBlockChange(p, loc.clone().add(-r, 0, 0)));
        locs.add(sendBlockChange(p, loc.clone().add(0, 0, -r)));

        locs.add(sendBlockChange(p, loc.clone().add(r, 0, r)));
        locs.add(sendBlockChange(p, loc.clone().add(-r, 0, -r)));
        locs.add(sendBlockChange(p, loc.clone().add(-r, 0, r)));
        locs.add(sendBlockChange(p, loc.clone().add(r, 0, -r)));

        locs.add(sendBlockChange(p, loc.clone().add(r - 1, 0, r)));
        locs.add(sendBlockChange(p, loc.clone().add(r, 0, r - 1)));

        locs.add(sendBlockChange(p, loc.clone().add(-r + 1, 0, -r)));
        locs.add(sendBlockChange(p, loc.clone().add(-r, 0, -r + 1)));

        locs.add(sendBlockChange(p, loc.clone().add(-r + 1, 0, r)));
        locs.add(sendBlockChange(p, loc.clone().add(-r, 0, r - 1)));

        locs.add(sendBlockChange(p, loc.clone().add(r - 1, 0, -r)));
        locs.add(sendBlockChange(p, loc.clone().add(r, 0, -r + 1)));*/

        for (int i = 0; i < (r * 2) + 1; i++) {
            locs.add(sendBlockChange(p, loc.clone().add(r, 0, i - r)));
            locs.add(sendBlockChange(p, loc.clone().add(-r, 0, i - r)));
            locs.add(sendBlockChange(p, loc.clone().add(i - r, 0, r)));
            locs.add(sendBlockChange(p, loc.clone().add(i - r, 0, -r)));
        }

        p.setMetadata("mtspotblock.strace", new FixedMetadataValue(main, locs));
    }
}
