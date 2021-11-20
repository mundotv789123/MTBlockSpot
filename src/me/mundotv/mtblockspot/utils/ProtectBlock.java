package me.mundotv.mtblockspot.utils;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ProtectBlock {

    private final Material blockId;
    private final String message, permission, name;
    private final List<String> lore;
    private final int radiuns;

    public ProtectBlock(Material blockId, String message, String permission, @Nullable String name, @Nullable List<String> lore, @Nullable int radiuns) {
        this.blockId = blockId;
        this.message = message;
        this.permission = permission;
        this.name = name;
        this.lore = lore;
        this.radiuns = radiuns;
    }
    
    public boolean isProtectItem(ItemStack item) {
        if (lore == null && name == null) {
            return item.getType().equals(blockId);
        }
        return this.getItem().isSimilar(item);
    }
    
    public boolean isProtectBlock(Block block) {
        if (lore == null && name == null) {
            return block.getType().equals(blockId);
        }
        return false;
    }
    
    public ItemStack getItem() {
        ItemStack item = new ItemStack(blockId, 1);
        ItemMeta meta = item.getItemMeta();
        if (this.name != null) {
            meta.setDisplayName(this.name.replace("&", "§"));
        }
        if (this.lore != null) {
            meta.setLore(new ArrayList());
            this.lore.forEach(str -> {
                meta.getLore().add(str.replace("&", "§"));
            });
        }
        item.setItemMeta(meta);
        return item;
    }

    public Material getBlockId() {
        return blockId;
    }

    public String getMessage() {
        return message;
    }

    public int getRadiuns() {
        return radiuns;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }
}
