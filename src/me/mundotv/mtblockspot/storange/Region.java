package me.mundotv.mtblockspot.storange;

import java.util.ArrayList;
import java.util.List;
import me.mundotv.mtblockspot.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class Region {

    private int id = 0;
    private final int posX, posY, posZ, posR;
    private final String own;
    private final List<String> players;
    private final Options options;

    public Region(int id, int posX, int posY, int posZ, int posR, String own, List<String> players, Options options) {
        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.posR = posR;
        this.own = own;
        this.players = players;
        this.options = options;
    }

    public Region(int posX, int posY, int posZ, int posR, String own) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.posR = posR;
        this.own = own;
        this.players = new ArrayList();
        this.options = new Options();
    }

    public Region(Location loc, int posR, String own) {
        this.posX = loc.getBlockX();
        this.posY = loc.getBlockY();
        this.posZ = loc.getBlockZ();
        this.posR = posR;
        this.own = own;
        this.players = new ArrayList();
        this.options = new Options();
    }

    /* functions */
    public boolean inRadiuns(int x, int z) {
        return (posX + posR >= x && posX - posR <= x) && 
               (posZ + posR >= z && posZ - posR <= z);
    }

    public boolean inRadiuns(int x, int z, int r) {
        return (posX + (posR + r) >= x && posX - (posR + r) <= x) && 
               (posZ + (posR + r) >= z && posZ - (posR + r) <= z);
    }

    public boolean hasPermission(String player) {
        return (own.equals(player) || players.contains(player));
    }

    public boolean isBlockLocation(Location loc) {
        return loc.getBlockX() == posX && loc.getBlockY() == posY && loc.getBlockZ() == posZ;
    }
    
    /* beta */
    public Location sendBlockChange(Player p, Location loc) {
        
        if (loc.getBlock().getType().equals(Material.AIR)) {
            loc.add(0, -10, 0);
        }
        for (int i = 10; (i > 1); i--) {
            if (loc.add(0, 1, 0).getBlock().getType().equals(Material.AIR)) {
                break;
            }
        }
        
        loc.add(0, -1, 0);
        p.sendBlockChange(loc, Bukkit.createBlockData(Material.SEA_LANTERN));
        
        
        return loc;
    }
    
    public void traceRadiuns(Player p) {
        if (p.hasMetadata("mtspotblock.strace")) {
            MetadataValue mv = p.getMetadata("mtspotblock.strace").get(0);
            List<Location> locs = (List<Location>) mv.value();
            locs.forEach(loc -> {
                p.sendBlockChange(loc, loc.getBlock().getBlockData());
            });
            p.removeMetadata("mtspotblock.strace", Main.getInstance());
        }
        
        Location loc = new Location(p.getLocation().getWorld(), posX, posY, posZ);
        int r = posR;
        
        List<Location> locs = new ArrayList();
        locs.add(sendBlockChange(p, loc.clone().add(r, 0, 0)));
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
        locs.add(sendBlockChange(p, loc.clone().add(r, 0, -r + 1)));
        
        p.setMetadata("mtspotblock.strace", new FixedMetadataValue(Main.getInstance(), locs));
    }
    
    /* getters */
    public int getId() {
        return id;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getPosZ() {
        return posZ;
    }

    public int getPosR() {
        return posR;
    }

    public String getOwn() {
        return own;
    }

    public List<String> getPlayers() {
        return players;
    }

    public Options getOptions() {
        return options;
    }
    
    /* setter */
    public void setId(int id) {
        this.id = id;
    }
    
    public static class Options {
        private boolean pvp = false, farm = true;

        public void setFarm(boolean farm) {
            this.farm = farm;
        }

        public void setPvp(boolean pvp) {
            this.pvp = pvp;
        }

        public boolean isFarm() {
            return farm;
        }

        public boolean isPvp() {
            return pvp;
        }
    }
}
