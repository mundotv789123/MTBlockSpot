package mundotv.mtblockspot.listener;

import mundotv.mtblockspot.MTMain;
import mundotv.mtblockspot.config.BlockSpot;
import mundotv.mtblockspot.config.Region;
import mundotv.mtblockspot.events.RegionClaimEvent;
import mundotv.mtblockspot.events.RegionInteractEvent;
import mundotv.mtblockspot.events.RegionUnclaimEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class RegionListener implements Listener {

    private final MTMain main;

    public RegionListener(MTMain main) {
        this.main = main;
    }

    private Region getRegionByLocation(Location loc, int r) {
        return this.main.getDatabase().getRegionByRadius(loc.getBlockX(), loc.getBlockZ(), r, loc.getWorld().getName());
    }

    private Region getRegionByLocation(Location loc) {
        return this.getRegionByLocation(loc, 0);
    }
    
    /* Claim Events */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onBlockPlaceRegionClaim(BlockPlaceEvent e) {
        for (BlockSpot sb : main.getBlocks()) {
            if (sb.isOnlyBlock()) {
                if (!sb.getMaterial().equals(e.getBlock().getType())) {
                    continue;
                }
            } else if (!e.getItemInHand().isSimilar(sb.getItem())) {
                continue;
            }
            if (e.getPlayer().hasMetadata("mtspotblock-disabled")) {
                if (!sb.isOnlyBlock()) {
                    e.setCancelled(true);
                }
                return;
            }
            Location loc = e.getBlock().getLocation();
            RegionClaimEvent rce = new RegionClaimEvent(e.getPlayer(), sb, e.getBlock());
            Bukkit.getPluginManager().callEvent(rce);
            e.setCancelled(rce.isCancelled());
            if (!rce.isCancelled()) {
                Region r = new Region(e.getPlayer().getName(), sb.getName(), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), sb.getRadius());
                e.setCancelled(!this.main.getDatabase().addRegion(r));
            }
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    protected void onBlockBreakRegionClaim(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        Region r = this.getRegionByLocation(loc);
        if (r != null && r.isBlockLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
            RegionUnclaimEvent rue = new RegionUnclaimEvent(e.getPlayer(), r, e, loc);
            Bukkit.getPluginManager().callEvent(rue);
            e.setCancelled(true);
            if (!rue.isCancelled()) {
                if (!this.main.getDatabase().removeRegion(r)) {
                    return;
                }
                for (BlockSpot sp : main.getBlocks()) {
                    if (sp.getName().equals(r.getBlockName())) {
                        e.getBlock().setType(Material.AIR);
                        e.getBlock().getWorld().dropItemNaturally(loc, sp.getItem());
                        return;
                    }
                }
                e.getBlock().getDrops().forEach(drop -> {
                    e.getBlock().getWorld().dropItemNaturally(loc, drop);
                });
            }
        }
    }

    /* Player Events */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onBlockPlace(BlockPlaceEvent e) {
        Location loc = e.getBlock().getLocation();
        Region r = this.getRegionByLocation(loc);
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(e.getPlayer(), r, e, loc);
        Bukkit.getPluginManager().callEvent(rie);
        e.setCancelled(rie.isCancelled());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onBlockBreak(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        Region r = this.getRegionByLocation(loc);
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(e.getPlayer(), r, e, loc);
        Bukkit.getPluginManager().callEvent(rie);
        e.setCancelled(rie.isCancelled());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onPlayerInteract(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        Location loc = (b == null ? e.getPlayer().getLocation() : b.getLocation());
        Region r = this.getRegionByLocation(loc);
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(e.getPlayer(), r, e, loc);
        Bukkit.getPluginManager().callEvent(rie);
        e.setCancelled(rie.isCancelled());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Location loc = e.getRightClicked().getLocation();
        Region r = this.getRegionByLocation(loc);
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(e.getPlayer(), r, e, loc);
        Bukkit.getPluginManager().callEvent(rie);
        e.setCancelled(rie.isCancelled());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        Location loc = e.getRightClicked().getLocation();
        Region r = this.getRegionByLocation(loc);
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(e.getPlayer(), r, e, loc);
        Bukkit.getPluginManager().callEvent(rie);
        e.setCancelled(rie.isCancelled());
    }

    /* Entity Events */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) {
            return;
        }
        Location loc = e.getEntity().getLocation();
        Location dloc = e.getDamager().getLocation();
        Region r = this.getRegionByLocation(loc);
        r = r != null ? r : this.getRegionByLocation(dloc);
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(((Player) e.getDamager()), r, e, loc);
        Bukkit.getPluginManager().callEvent(rie);
        e.setCancelled(rie.isCancelled());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player)) {
            return;
        }
        Location loc = e.getEntity().getLocation();
        Region r = this.getRegionByLocation(loc);
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent((Player) e.getRemover(), r, e, loc);
        Bukkit.getPluginManager().callEvent(rie);
        e.setCancelled(rie.isCancelled());
    }

    /* Outhers Events */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        Location loc = e.getBlock().getLocation();
        Region pr = this.getRegionByLocation(loc, 1);
        for (Block b : e.getBlocks()) {
            Location bloc = b.getLocation();
            Region r = this.getRegionByLocation(bloc, 1);
            if (r != null && (pr == null || !r.getOwn().equals(pr.getOwn()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        Location loc = e.getBlock().getLocation();
        Region pr = this.getRegionByLocation(loc);
        for (Block b : e.getBlocks()) {
            Location bloc = b.getLocation();
            Region r = this.getRegionByLocation(bloc, 1);
            if (r != null && (pr == null || !r.getOwn().equals(pr.getOwn()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        for (int i = 0; i < e.blockList().size(); i++) {
            Block b = e.blockList().get(i);
            Location loc = b.getLocation();
            Region r = this.getRegionByLocation(loc);
            if (r != null) {
                e.blockList().remove(b);
                i--;
            }
        }
    }
}
