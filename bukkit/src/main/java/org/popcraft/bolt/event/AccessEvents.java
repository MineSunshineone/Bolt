package org.popcraft.bolt.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.popcraft.bolt.Bolt;
import org.popcraft.bolt.data.Permission;
import org.popcraft.bolt.data.protection.BlockProtection;
import org.popcraft.bolt.data.protection.EntityProtection;
import org.popcraft.bolt.data.util.BlockLocation;

import java.util.Optional;

public class AccessEvents implements Listener {
    private final Bolt bolt;

    public AccessEvents(final Bolt bolt) {
        this.bolt = bolt;
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        final Block block = e.getBlock();
        final BlockLocation location = new BlockLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        final Optional<BlockProtection> protection = bolt.getStore().loadBlockProtection(location);
        if (protection.isPresent()) {
            final BlockProtection blockProtection = protection.get();
            if (!bolt.getAccessManager().hasAccess(e.getPlayer(), blockProtection, Permission.BREAK)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent e) {
        final Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }
        final BlockLocation location = new BlockLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        final Optional<BlockProtection> protection = bolt.getStore().loadBlockProtection(location);
        if (protection.isPresent()) {
            final BlockProtection blockProtection = protection.get();
            if (!bolt.getAccessManager().hasAccess(e.getPlayer(), blockProtection, Permission.INTERACT)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent e) {
        final Block block = e.getBlock();
        final BlockLocation location = new BlockLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        final Optional<BlockProtection> protection = bolt.getStore().loadBlockProtection(location);
        if (protection.isPresent()) {
            final BlockProtection blockProtection = protection.get();
            if (!bolt.getAccessManager().hasAccess(e.getPlayer(), blockProtection, Permission.INTERACT)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent e) {
        final Entity entity = e.getEntity();
        final Optional<EntityProtection> protection = bolt.getStore().loadEntityProtection(entity.getUniqueId());
        if (protection.isPresent()) {
            final EntityProtection entityProtection = protection.get();
            if (!(e.getRemover() instanceof final Player player) || !bolt.getAccessManager().hasAccess(player, entityProtection, Permission.KILL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVehicleDestroy(final VehicleDestroyEvent e) {
        final Entity entity = e.getVehicle();
        final Optional<EntityProtection> protection = bolt.getStore().loadEntityProtection(entity.getUniqueId());
        if (protection.isPresent()) {
            final EntityProtection entityProtection = protection.get();
            if (!(e.getAttacker() instanceof final Player player) || !bolt.getAccessManager().hasAccess(player, entityProtection, Permission.KILL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(final PlayerArmorStandManipulateEvent e) {
        final Entity entity = e.getRightClicked();
        final Optional<EntityProtection> protection = bolt.getStore().loadEntityProtection(entity.getUniqueId());
        if (protection.isPresent()) {
            final EntityProtection entityProtection = protection.get();
            if (!bolt.getAccessManager().hasAccess(e.getPlayer(), entityProtection, Permission.INTERACT)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
        final Entity entity = e.getEntity();
        final Optional<EntityProtection> protection = bolt.getStore().loadEntityProtection(entity.getUniqueId());
        if (protection.isPresent()) {
            final EntityProtection entityProtection = protection.get();
            if (!(e.getDamager() instanceof final Player player) || !bolt.getAccessManager().hasAccess(player, entityProtection, Permission.KILL)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        final Entity entity = e.getRightClicked();
        final Optional<EntityProtection> protection = bolt.getStore().loadEntityProtection(entity.getUniqueId());
        if (protection.isPresent()) {
            final EntityProtection entityProtection = protection.get();
            if (!bolt.getAccessManager().hasAccess(e.getPlayer(), entityProtection, Permission.INTERACT)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(final InventoryOpenEvent e) {
        // TODO: Do this better
        if (!(e.getPlayer() instanceof Player player)) {
            return;
        }
        final InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if (inventoryHolder instanceof final BlockInventoryHolder blockInventoryHolder) {
            final Block block = blockInventoryHolder.getBlock();
            final BlockLocation location = new BlockLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
            final Optional<BlockProtection> protection = bolt.getStore().loadBlockProtection(location);
            if (protection.isPresent()) {
                final BlockProtection blockProtection = protection.get();
                if (!bolt.getAccessManager().hasAccess(player, blockProtection, Permission.CONTAINER_ACCESS)) {
                    e.setCancelled(true);
                }
            }
        } else if (inventoryHolder instanceof final Entity entity) {
            final Optional<EntityProtection> protection = bolt.getStore().loadEntityProtection(entity.getUniqueId());
            if (protection.isPresent()) {
                final EntityProtection entityProtection = protection.get();
                if (!bolt.getAccessManager().hasAccess(player, entityProtection, Permission.CONTAINER_ACCESS)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        // TODO: Do this when the other inventory event is handled better (and this one will be pretty similar)
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent e) {
        // TODO: Do this when the other inventory event is handled better (and this one will be pretty similar)
    }

    @EventHandler
    public void onPlayerTakeLecternBook(final PlayerTakeLecternBookEvent e) {
        final Block block = e.getLectern().getBlock();
        final BlockLocation location = new BlockLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
        final Optional<BlockProtection> protection = bolt.getStore().loadBlockProtection(location);
        if (protection.isPresent()) {
            final BlockProtection blockProtection = protection.get();
            if (!bolt.getAccessManager().hasAccess(e.getPlayer(), blockProtection, Permission.CONTAINER_REMOVE)) {
                e.setCancelled(true);
            }
        }
    }
}
