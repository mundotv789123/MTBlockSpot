package mundotv.mtblockspot.listener;

import mundotv.mtblockspot.config.Region;
import mundotv.mtblockspot.database.RegionDatabase;
import mundotv.mtblockspot.events.PlayerRegionInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {

    private final RegionDatabase database;

    public PlayerListener(RegionDatabase database) {
        this.database = database;
    }

    /* Player Block */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockPlace(BlockPlaceEvent e) {
        e.setCancelled(interact(e.getPlayer(), e, e.getBlock().getLocation()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(interact(e.getPlayer(), e, e.getBlock().getLocation()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onPlayerInteract(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        if (b != null) {
            e.setCancelled(interact(e.getPlayer(), e, b.getLocation()));
        }
    }

    @EventHandler
    protected void on(PlayerBucketFillEvent e) {
        e.setCancelled(interact(e.getPlayer(), e, e.getBlock().getLocation()));
    }
    
    @EventHandler
    protected void on(PlayerBucketEmptyEvent e) {
        e.setCancelled(interact(e.getPlayer(), e, e.getBlock().getLocation()));
    }
    
    /* Player Entity */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        e.setCancelled(interact(e.getPlayer(), e, e.getRightClicked().getLocation()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        e.setCancelled(interact(e.getPlayer(), e, e.getRightClicked().getLocation()));
    }

    /* Entity */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            e.setCancelled(interact((Player) e.getDamager(), e, e.getEntity().getLocation()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    protected void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player) {
            e.setCancelled(interact((Player) e.getRemover(), e, e.getEntity().getLocation()));
        }
    }

    /* Region */
    @EventHandler(priority = EventPriority.LOWEST)
    protected void onPlayerRegionInteractEvent(PlayerRegionInteractEvent e) {
        if (e.getRegion() == null) {
            return;
        }
        if (!e.getRegion().hasPermission(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }

    /* Piston Events */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        Location loc = e.getBlock().getLocation();
        Region pr = database.getRegionByRadius(loc, 1);
        for (Block b : e.getBlocks()) {
            Region r = database.getRegionByRadius(b.getLocation(), 1);
            if (r == null) {
                continue;
            }
            if (r.isBlockLocation(b.getLocation())) {
                e.setCancelled(true);
                return;
            }
            if ((pr == null || !r.getOwn().equals(pr.getOwn()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        Location loc = e.getBlock().getLocation();
        Region pr = database.getRegionByRadius(loc, 0);
        for (Block b : e.getBlocks()) {
            Region r = database.getRegionByRadius(b.getLocation(), 1);
            if (r == null) {
                continue;
            }
            if (r.isBlockLocation(b.getLocation())) {
                e.setCancelled(true);
                return;
            }
            if ((pr == null || !r.getOwn().equals(pr.getOwn()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    /* Explode Event */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityExplode(EntityExplodeEvent e) {
        for (int i = 0; i < e.blockList().size(); i++) {
            Block b = e.blockList().get(i);
            Location loc = b.getLocation();
            Region r = database.getRegionByRadius(loc, 0);
            if (r != null) {
                e.blockList().remove(b);
                i--;
            }
        }
    }

    private boolean interact(Player who, Event e, Location loc) {
        Region r = database.getRegionByRadius(loc, 0);

        if (r == null && (e instanceof EntityDamageByEntityEvent)) {
            loc = who.getLocation();
            r = database.getRegionByRadius(loc, 0);
        }
        if (r == null) {
            return false;
        }
        
        PlayerRegionInteractEvent event = new PlayerRegionInteractEvent(r, e, who);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

}
