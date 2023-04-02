package org.popcraft.bolt.command.impl;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.popcraft.bolt.BoltPlugin;
import org.popcraft.bolt.access.Access;
import org.popcraft.bolt.command.Arguments;
import org.popcraft.bolt.command.BoltCommand;
import org.popcraft.bolt.lang.Translation;
import org.popcraft.bolt.source.Source;
import org.popcraft.bolt.source.SourceType;
import org.popcraft.bolt.util.Action;
import org.popcraft.bolt.util.BoltComponents;
import org.popcraft.bolt.util.BoltPlayer;
import org.popcraft.bolt.util.BukkitAdapter;
import org.popcraft.bolt.util.SchedulerUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.popcraft.bolt.lang.Translator.translate;

public class EditCommand extends BoltCommand {
    public EditCommand(BoltPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof final Player player)) {
            BoltComponents.sendMessage(sender, Translation.COMMAND_PLAYER_ONLY);
            return;
        }
        if (arguments.remaining() < 3) {
            BoltComponents.sendMessage(sender, Translation.COMMAND_NOT_ENOUGH_ARGS);
            return;
        }
        final BoltPlayer boltPlayer = plugin.player(player);
        final boolean adding = "add".equalsIgnoreCase(arguments.next());
        boltPlayer.setAction(new Action(Action.Type.EDIT, Boolean.toString(adding)));
        final String accessType = arguments.next();
        final Access access = plugin.getBolt().getAccessRegistry().getAccessByType(accessType).orElse(null);
        if (access == null) {
            BoltComponents.sendMessage(
                    sender,
                    Translation.EDIT_ACCESS_INVALID,
                    Placeholder.unparsed(Translation.Placeholder.ACCESS_TYPE, accessType)
            );
            return;
        }
        final String sourceType = arguments.next();
        if (sourceType == null || !plugin.getBolt().getSourceTypeRegistry().sourceTypes().contains(sourceType)) {
            BoltComponents.sendMessage(
                    sender,
                    Translation.EDIT_SOURCE_INVALID,
                    Placeholder.unparsed(Translation.Placeholder.SOURCE_TYPE, String.valueOf(sourceType))
            );
            return;
        }
        String identifier;
        while ((identifier = arguments.next()) != null) {
            final CompletableFuture<Source> editFuture;
            final String finalIdentifier = identifier;
            if (SourceType.PLAYER.equals(sourceType)) {
                editFuture = BukkitAdapter.findOrLookupProfileByName(identifier).thenApply(profile -> {
                    if (profile.uuid() != null) {
                        return Source.player(profile.uuid());
                    } else {
                        SchedulerUtil.schedule(plugin, player, () -> BoltComponents.sendMessage(
                                player,
                                Translation.PLAYER_NOT_FOUND,
                                Placeholder.unparsed(Translation.Placeholder.PLAYER, finalIdentifier)
                        ));
                        return null;
                    }
                });
            } else if (SourceType.PASSWORD.equals(sourceType)) {
                editFuture = CompletableFuture.completedFuture(Source.password(identifier));
            } else {
                editFuture = CompletableFuture.completedFuture(Source.of(sourceType, identifier));
            }
            editFuture.thenAccept(source -> SchedulerUtil.schedule(plugin, player, () -> {
                if (source != null) {
                    boltPlayer.getModifications().put(source, access.type());
                    if (boltPlayer.isTrusting() && !boltPlayer.isTrustingSilently()) {
                        BoltComponents.sendMessage(
                                player,
                                adding ? Translation.TRUST_ADD : Translation.TRUST_REMOVE,
                                Placeholder.unparsed(Translation.Placeholder.SOURCE_TYPE, source.getType()),
                                Placeholder.unparsed(Translation.Placeholder.SOURCE_IDENTIFIER, finalIdentifier)
                        );
                    }
                }
            }));
        }
        if (!boltPlayer.isTrusting()) {
            BoltComponents.sendMessage(
                    player,
                    Translation.CLICK_ACTION,
                    plugin.isUseActionBar(),
                    Placeholder.unparsed(Translation.Placeholder.ACTION, translate(Translation.EDIT))
            );
        }
    }

    @Override
    public List<String> suggestions(Arguments arguments) {
        if (arguments.remaining() == 0) {
            return Collections.emptyList();
        }
        arguments.next();
        if (arguments.remaining() == 0) {
            return List.of("add", "remove");
        }
        arguments.next();
        if (arguments.remaining() == 0) {
            return plugin.getBolt().getAccessRegistry().accessTypes();
        }
        final String sourceType = arguments.next();
        if (arguments.remaining() == 0) {
            return plugin.getBolt().getSourceTypeRegistry().sourceTypes().stream().toList();
        }
        if (!SourceType.PLAYER.equals(sourceType)) {
            return Collections.emptyList();
        }
        final Set<String> alreadyAdded = new HashSet<>();
        String added;
        while ((added = arguments.next()) != null) {
            alreadyAdded.add(added);
        }
        return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).filter(name -> !alreadyAdded.contains(name)).toList();
    }
}
