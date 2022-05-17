package mundotv.mtblockspot.events;

import mundotv.mtblockspot.config.BlockSpot;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class RegionClaimEvent extends PlayerEvent implements Cancellable {

    private final static HandlerList handles = new HandlerList();
    private boolean cancel;
    private final BlockSpot blocksp;
    private final Block block;
    
    public RegionClaimEvent(Player who, BlockSpot blocksp, Block block) {
        super(who);
        this.blocksp = blocksp;
        this.block = block;
    }

    public BlockSpot getBlocksp() {
        return blocksp;
    }

    public Block getBlock() {
        return block;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handles;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean bln) {
        this.cancel = bln;
    }
    
    public static HandlerList getHandlerList() {
        return handles;
    }
}
