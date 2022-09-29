package org.popcraft.bolt.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.popcraft.bolt.protection.BlockProtection;
import org.popcraft.bolt.protection.EntityProtection;
import org.popcraft.bolt.util.defaults.DefaultProtectionType;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public final class BukkitAdapter {
    private BukkitAdapter() {
    }

    public static BlockProtection createPrivateBlockProtection(final Block block, final UUID owner) {
        return createBlockProtection(block, owner, DefaultProtectionType.PRIVATE.type());
    }

    public static BlockProtection createBlockProtection(final Block block, final UUID owner, final String type) {
        return new BlockProtection(UUID.randomUUID(), owner, type, new HashMap<>(), block.getType().toString(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public static BlockLocation blockLocation(final Block block) {
        return new BlockLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public static BlockLocation blockLocation(final BlockState blockState) {
        return new BlockLocation(blockState.getWorld().getName(), blockState.getX(), blockState.getY(), blockState.getZ());
    }

    public static BlockLocation blockLocation(final Location location) {
        Objects.requireNonNull(location.getWorld());
        return new BlockLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockLocation blockLocation(final BlockProtection blockProtection) {
        return new BlockLocation(blockProtection.getWorld(), blockProtection.getX(), blockProtection.getY(), blockProtection.getZ());
    }

    public static EntityProtection createPrivateEntityProtection(final Entity entity, final UUID owner) {
        return createEntityProtection(entity, owner, DefaultProtectionType.PRIVATE.type());
    }

    public static EntityProtection createEntityProtection(final Entity entity, final UUID owner, final String type) {
        return new EntityProtection(entity.getUniqueId(), owner, type, new HashMap<>(), entity.getType().toString());
    }
}
