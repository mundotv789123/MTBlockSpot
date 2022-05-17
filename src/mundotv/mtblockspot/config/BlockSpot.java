package mundotv.mtblockspot.config;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockSpot {

    private final Material material;
    private final String name;
    private final int radius;
    private String displayName;
    private List<String> lore;

    public String getName() {
        return name;
    }

    public BlockSpot(Material material, String name, int radius) {
        this.material = material;
        this.name = name;
        this.radius = radius;
    }

    public ItemStack getItem() {
        ItemStack item = new ItemStack(this.material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        if (this.displayName != null) {
            meta.setDisplayName(displayName.replace("&", "§"));
        }
        if (this.lore != null && !this.lore.isEmpty()) {
            List<String> lores = new ArrayList();
            this.lore.forEach(str -> {
                lores.add(str.replace("&", "§"));
            });
            meta.setLore(lores);
        }
        item.setItemMeta(meta);
        return item;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isOnlyBlock() {
        return (lore == null && displayName == null);
    }
}
