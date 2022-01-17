package org.popcraft.bolt.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.popcraft.bolt.BoltPlugin;
import org.popcraft.bolt.util.Action;
import org.popcraft.bolt.util.BoltComponents;
import org.popcraft.bolt.util.PlayerMeta;
import org.popcraft.bolt.util.BukkitAdapter;

public class DebugEvents implements Listener {
    private final BoltPlugin plugin;

    public DebugEvents(final BoltPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        final PlayerMeta playerMeta = plugin.getBolt().getPlayerMeta(player.getUniqueId());
        if (playerMeta.hasAction(Action.DEBUG)) {
            final Block clicked = e.getClickedBlock();
            if (clicked != null) {
                plugin.getBolt().getStore().loadBlockProtection(BukkitAdapter.blockLocation(clicked)).ifPresentOrElse(blockProtection -> {
                    BoltComponents.sendMessage(player, "A block is protected here");
                    BoltComponents.sendMessage(player, blockProtection.toString());
                }, () -> BoltComponents.sendMessage(player, "A block isn't protected here"));
            }
            playerMeta.removeAction(Action.DEBUG);
        }
    }
}
