package mundotv.mtblockspot.listener;

import mundotv.mtblockspot.MTMain;
import mundotv.mtblockspot.config.BlockSpot;
import mundotv.mtblockspot.config.Region;
import mundotv.mtblockspot.events.PlayerRegionInteractEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class RegionListener implements Listener {

    private final MTMain main;

    public RegionListener(MTMain main) {
        this.main = main;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    protected void onBlockPlaceRegionClaim(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (p.hasMetadata("mtspotblock-disabled")) {
            return;
        }
        /* verificando se é bloco de proteção */
        BlockSpot bs = getBlockSpot(e.getItemInHand(), e.getBlock().getType());
        if (bs == null) {
            return;
        }
        e.setCancelled(true);
        
        /* verificando blocos próximo */
        Location loc = e.getBlock().getLocation();
        Region r = main.getDatabase().getRegionByRadius(loc, bs.getRadius());
        if (r != null) {
            r.traceRadiuns(p, main);
            p.sendMessage("§cBlocos próximos");
            return;
        }

        /* verificando permissões */
        if (!p.hasPermission("mtblockspot.blocks." + bs.getName())) {
            p.sendMessage("§cVocê não pode usar esse bloco de proteção");
            e.setCancelled(true);
            return;
        }

        /* verificando limite de blocos */
        int limit = getBlocksLimit(p);
        if (limit == 0 || (limit > 0 && limit <= main.getDatabase().getRegionsByOwn(p.getName()).size())) {
            p.sendMessage("§cLimite de blocos ultrapassados");
            return;
        }

        /* protegendo área */
        r = new Region(p, bs, loc);
        if (this.main.getDatabase().addRegion(r)) {
            r.traceRadiuns(p, main);
            e.setCancelled(false);
            p.sendMessage("§aProtegido!");
        }
    }

    @EventHandler(ignoreCancelled = true)
    protected void onBlockBreakRegionClaim(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        Region r = main.getDatabase().getRegionByRadius(loc, 0);
        if (r == null || !r.isBlockLocation(loc)) {
            return;
        }
        e.setCancelled(true);

        Player p = e.getPlayer();

        /* verificando permissão */
        if (!r.getOwn().equals(p.getName())) {
            e.getPlayer().sendMessage("§cVocê não pode fazer isso!");
            return;
        }

        /* removendo proteção */
        if (!this.main.getDatabase().removeRegion(r)) {
            return;
        }
        Region.removeTraceRadiuns(p, main);
        BlockSpot sp = getBlockSpot(r.getBlockName());
        if (sp != null) {
            e.getBlock().setType(Material.AIR);
            e.getBlock().getWorld().dropItemNaturally(loc, sp.getItem());
            return;
        }
        e.getBlock().getDrops().forEach(drop -> {
            e.getBlock().getWorld().dropItemNaturally(loc, drop);
        });
    }

    @EventHandler
    protected void onPlayerRegionInteractEvent(PlayerRegionInteractEvent e) {
        /* verificando pvp */
        Player p = e.getPlayer();
        Region.removeTraceRadiuns(p, main);
        if (e.getEvent() instanceof EntityDamageByEntityEvent) {
            e.setCancelled(true);
            EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e.getEvent();
            if (ee.getEntity() instanceof Monster) {
                e.setCancelled(false);
            }
            if (ee.getEntity() instanceof Player) {
                if (e.getRegion().getOptions().isPvp()) {
                    e.setCancelled(false);
                } else {
                    p.sendMessage("pvp off!");
                }
            }
            return;
        }

        /* verificando farm */
        if (e.getEvent() instanceof PlayerInteractEvent) {
            PlayerInteractEvent ie = (PlayerInteractEvent) e.getEvent();
            Block b = ie.getClickedBlock();
            if (b == null) {
                return;
            }
            if (!(b.getBlockData() instanceof Ageable) /*&& !farms_blocks.contains(b.getType().toString())*/) {
                if (e.getRegion().isBlockLocation(b.getLocation())) {
                    e.getRegion().traceRadiuns(p, main);
                }
                return;
            }

            if (!e.isCancelled()) {
                return;
            }

            if (!ie.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                return;
            }

            if (!e.getRegion().getOptions().isFarm()) {
                p.sendMessage("Coleita de plantação desativada!");
                return;
            }
            Ageable age = (Ageable) b.getBlockData();
            if (age.getAge() < age.getMaximumAge()) {
                return;
            }
            for (ItemStack item : b.getDrops()) {
                b.getWorld().dropItemNaturally(b.getLocation(), item);
            }
            age.setAge(0);
            b.setBlockData(age);
            return;
        }
        if (e.isCancelled()) {
            p.sendMessage("Protegido!");
        }

    }

    /* utils */
    private BlockSpot getBlockSpot(ItemStack item, Material material) {
        for (BlockSpot b : main.getBlocks()) {
            if (b.isOnlyBlock()) {
                if (b.getMaterial().equals(material)) {
                    return b;
                }
                continue;
            }
            if (item.isSimilar(b.getItem())) {
                return b;
            }
        }
        return null;
    }

    private BlockSpot getBlockSpot(String name) {
        for (BlockSpot b : main.getBlocks()) {
            if (b.getName().equals(name)) {
                return b;
            }
        }
        return null;
    }

    public int getBlocksLimit(Player p) {
        if (!p.hasPermission("mtblockspot.limit") || p.hasPermission("mtblockspot.limit.*")) {
            return -1;
        }
        for (PermissionAttachmentInfo pai : p.getEffectivePermissions()) {
            if (!pai.getPermission().startsWith("mtblockspot.limit") || pai.getPermission().equals("mtblockspot.limit")) {
                continue;
            }
            String[] ps = pai.getPermission().split("\\.");
            try {
                return Integer.parseInt(ps[ps.length - 1]);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return 0;
    }
}
