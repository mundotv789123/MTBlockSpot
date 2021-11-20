package me.mundotv.mtblockspot.events;

import java.util.List;
import me.mundotv.mtblockspot.utils.Messages;
import me.mundotv.mtblockspot.utils.ProtectBlock;
import me.mundotv.mtblockspot.storange.Region;
import me.mundotv.mtblockspot.storange.Regions;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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

    private final Regions regions;
    private final List<ProtectBlock> blocks;

    public RegionListener(Regions regions, List<ProtectBlock> blocks) {
        this.regions = regions;
        this.blocks = blocks;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    protected void onRegionInteract(RegionInteractEvent e) {
        if (!e.getRegion().hasPermission(e.getPlayer().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    protected void onRegionFarm(RegionInteractEvent e) {
        if (!e.isCancelled()) {
            return;
        }
        Block b = e.getBlock();
        if (e.getRegion().getOptions().isFarm() && b != null && (e.getEvent() instanceof PlayerInteractEvent) && (b.getBlockData() instanceof Ageable)) {
            Ageable age = (Ageable) b.getBlockData();
            if (age.getAge() < age.getMaximumAge()) {
                b.getDrops().forEach(i -> {
                    b.getWorld().dropItemNaturally(b.getLocation(), i);
                });
                age.setAge(0);
                b.setBlockData(age);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    protected void onRegionPvp(RegionInteractEvent e) {
        if (e.getEvent() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e.getEvent();
            if (ev.getEntity() instanceof Player) {
                e.setCancelled(!e.getRegion().getOptions().isPvp());
            }
        }
    }

    /* Claim Events */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onBlockPlaceRegionClaim(BlockPlaceEvent e) {
        if (e.getPlayer().hasMetadata("mtspotblock-disabled")) {
            return;
        }
        for (ProtectBlock sb : blocks) {
            if (!sb.isProtectBlock(e.getBlock()) && !sb.isProtectItem(e.getItemInHand())) {
                continue;
            }
            if (sb.getPermission() != null && !e.getPlayer().hasPermission(sb.getPermission())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Messages.NOT_PERMISSION.getMessage());
            }
            Region r = regions.getRegionByRadiuns(e.getBlock().getLocation(), sb.getRadiuns());
            if (r != null) {
                e.setCancelled(true);
                r.traceRadiuns(e.getPlayer()); //beta
                e.getPlayer().sendMessage(Messages.RADIUNS_LIMIT.getMessage());
                return;
            }
            r = new Region(e.getBlock().getLocation(), sb.getRadiuns(), e.getPlayer().getName());
            if (!regions.addRegion(r)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Messages.ERROR.getMessage());
                break;
            }
            r.traceRadiuns(e.getPlayer()); //beta
            e.getPlayer().sendMessage(sb.getMessage().replace("&", "§").replace("\\n", "\n"));
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    protected void onBlockBreakRegionClaim(BlockBreakEvent e) {
        Region r = regions.getRegionByRadiuns(e.getBlock().getLocation());
        if (r == null) {
            return;
        }
        if (r.isBlockLocation(e.getBlock().getLocation())) {
            if (r.getOwn().equals(e.getPlayer().getName())) {
                if (!regions.removeRegion(r)) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(Messages.ERROR.getMessage());
                    return;
                }
                e.getPlayer().sendMessage(Messages.UNCLAIM.getMessage());
                return;
            }
            e.setCancelled(true);
            e.getPlayer().sendMessage(Messages.UNREMOVED.getMessage().replace("{own}", r.getOwn()));
        }
    }

    /* Player Events */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onBlockPlace(BlockPlaceEvent e) {
        Region r = regions.getRegionByRadiuns(e.getBlock().getLocation());
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(e.getPlayer(), r, e);
        rie.setBlock(e.getBlock());
        Bukkit.getPluginManager().callEvent(rie);
        if (rie.isCancelled()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onBlockBreak(BlockBreakEvent e) {
        Region r = regions.getRegionByRadiuns(e.getBlock().getLocation());
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(e.getPlayer(), r, e);
        Bukkit.getPluginManager().callEvent(rie);
        if (rie.isCancelled()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onPlayerInteract(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        Region r = regions.getRegionByRadiuns((b != null ? b.getLocation() : e.getPlayer().getLocation()));
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(e.getPlayer(), r, e);
        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            rie.setBlock(b);
        }
        Bukkit.getPluginManager().callEvent(rie);
        if (rie.isCancelled()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Region r = regions.getRegionByRadiuns(e.getRightClicked().getLocation());
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(e.getPlayer(), r, e);
        Bukkit.getPluginManager().callEvent(rie);
        if (rie.isCancelled()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        Region r = regions.getRegionByRadiuns(e.getRightClicked().getLocation());
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent(e.getPlayer(), r, e);
        Bukkit.getPluginManager().callEvent(rie);
        if (rie.isCancelled()) {
            e.setCancelled(true);
        }
    }

    /* Entity Events */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || (e.getEntity() instanceof Monster)) {
            return;
        }
        Region r = regions.getRegionByRadiuns(e.getEntity().getLocation());
        r = r != null ? r : regions.getRegionByRadiuns(e.getDamager().getLocation());
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent((Player) e.getDamager(), r, e);
        Bukkit.getPluginManager().callEvent(rie);
        if (rie.isCancelled()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    protected void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
        if (!(e.getRemover() instanceof Player)) {
            return;
        }
        Region r = regions.getRegionByRadiuns(e.getEntity().getLocation());
        if (r == null) {
            return;
        }
        RegionInteractEvent rie = new RegionInteractEvent((Player) e.getRemover(), r, e);
        Bukkit.getPluginManager().callEvent(rie);
        if (rie.isCancelled()) {
            e.setCancelled(true);
        }
    }

    /* Outhers Events */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        Block pb = e.getBlock();
        Region pr = regions.getRegionByRadiuns(pb.getX(), pb.getZ());
        for (Block b : e.getBlocks()) {
            Region r = regions.getRegionByRadiuns(b.getX(), b.getZ(), 1);
            if (r != null && (pr == null || !r.getOwn().equals(pr.getOwn()))) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        Block pb = e.getBlock();
        Region pr = regions.getRegionByRadiuns(pb.getX(), pb.getZ());
        for (Block b : e.getBlocks()) {
            Region r = regions.getRegionByRadiuns(b.getX(), b.getZ(), 1);
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
            Region r = regions.getRegionByRadiuns(b.getX(), b.getZ());
            if (r != null) {
                e.blockList().remove(b);
                i--;
            }
        }
    }
}
