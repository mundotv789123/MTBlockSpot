package mundotv.mtblockspot.listener;

import java.util.List;
import mundotv.mtblockspot.MTMain;
import mundotv.mtblockspot.config.Region;
import mundotv.mtblockspot.events.RegionClaimEvent;
import mundotv.mtblockspot.events.RegionInteractEvent;
import mundotv.mtblockspot.events.RegionUnclaimEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class SpotListener implements Listener {

    private final MTMain main;
    private final List<String> worlds;
    private final List<String> farms_blocks;
    private final List<String> interact_blocks;
    private final int maxWidth, minWidth;

    public SpotListener(MTMain main, List<String> worlds, int maxWidth, int minWidth, List<String> farms_blocks, List<String> interact_blocks) {
        this.main = main;
        this.worlds = worlds;
        this.maxWidth = maxWidth;
        this.minWidth = minWidth;
        this.farms_blocks = farms_blocks;
        this.interact_blocks = interact_blocks;
    }

    private boolean playerHasLimit(Player p, int limit) {
        if (!p.hasPermission("mtblockspot.limit")) {
            return false;
        }
        if (p.hasPermission("mtblockspot.limit.*")) {
            return true;
        }
        for (PermissionAttachmentInfo pai : p.getEffectivePermissions()) {
            if (pai.getPermission().startsWith("mtblockspot.limit") && !pai.getPermission().equals("mtblockspot.limit")) {
                String[] ps = pai.getPermission().split("\\.");
                if (ps[ps.length - 1].equals("*")) {
                    return true;
                }
                try {
                    int l = Integer.parseInt(ps[ps.length - 1]);
                    return l > limit;
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onRegionClaim(RegionClaimEvent e) {
        Player p = e.getPlayer();
        World world = e.getBlock().getLocation().getWorld();

        if (world == null || !worlds.contains(world.getName())) {
            p.sendMessage("§cVocê não pode proteger esse mundo!");
            e.setCancelled(true);
            return;
        }

        if (!p.hasPermission("mtblockspot.blocks." + e.getBlocksp().getName())) {
            p.sendMessage("§cSem permissão!");
            e.setCancelled(true);
            return;
        }

        if (!playerHasLimit(p, main.getDatabase().getRegionsByOwn(p.getName()).size())) {
            p.sendMessage("§cLimite ultrapassado!");
            e.setCancelled(true);
            return;
        }

        Location bloc = e.getBlock().getLocation();
        Region r = main.getDatabase().getRegionByRadius(bloc.getBlockX(), bloc.getBlockZ(), e.getBlocksp().getRadius(), bloc.getWorld().getName());
        if (r != null) {
            p.sendMessage("§cBlocos próximos");
            e.setCancelled(true);
            return;
        }
        p.sendMessage("§aProtegido!");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onRegionUnclaim(RegionUnclaimEvent e) {
        Player p = e.getPlayer();
        if (!e.getRegion().getOwn().equals(e.getPlayer().getName())) {
            p.sendMessage("n pode fazer isso!");
            e.setCancelled(true);
            return;
        }
        Region.removeTraceRadiuns(p, main);
        p.sendMessage("§cDesprotegido!");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onRegionInteract(RegionInteractEvent e) {
        Player p = e.getPlayer();
        int y = e.getLocation().getBlockY();
        if (e.getPlayer().hasMetadata("mtspotblock-admin") || y > maxWidth || y < minWidth) {
            return;
        }
        if (e.getEvent() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e.getEvent();
            if (ee.getEntity() instanceof Monster) {
                return;
            }
            if (ee.getEntity() instanceof Player) {
                e.setCancelled(!e.getRegion().getOptions().isPvp());
                if (e.isCancelled()) {
                    p.sendMessage("§cO pvp está desativado aqui!");
                    if (e.getRegion().getOwn().equals(p.getName())) {
                        p.sendMessage("§aUse §e/bs pvp on §apara ativar");
                    }
                }
                return;
            }
        }
        if (!e.getRegion().hasPermission(e.getPlayer().getName())) {
            e.setCancelled(true);
            if (e.getRegion().getOptions().isFarm()) {
                Block b = null;
                if (e.getEvent() instanceof BlockBreakEvent) {
                    BlockBreakEvent ev = (BlockBreakEvent) e.getEvent();
                    b = ev.getBlock();
                } else if (e.getEvent() instanceof PlayerInteractEvent) {
                    PlayerInteractEvent ev = (PlayerInteractEvent) e.getEvent();
                    if (ev.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                        if (ev.getClickedBlock() == null) {
                            return;
                        }
                        if (interact_blocks.contains(ev.getClickedBlock().getType().toString()) && !p.isSneaking()) {
                            e.setCancelled(false);
                        }
                        return;
                    }
                    if (ev.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                        b = ev.getClickedBlock();
                    }
                }
                if (b != null && (b.getBlockData() instanceof Ageable) && !farms_blocks.contains(b.getType().toString())) {
                    Ageable age = (Ageable) b.getBlockData();
                    if (age.getAge() == age.getMaximumAge()) {
                        for (ItemStack item : b.getDrops()) {
                            b.getWorld().dropItemNaturally(b.getLocation(), item);
                        }
                        age.setAge(0);
                        b.setBlockData(age);
                    }
                    return;
                }
                //traceRadiuns(p, e.getRegion().getX(), e.getRegion().getY(), e.getRegion().getZ(), e.getRegion().getR());
                p.sendMessage("§cÁrea protegida!");
            }
        }
    }
}
