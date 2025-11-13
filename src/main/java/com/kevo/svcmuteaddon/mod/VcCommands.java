package com.kevo.svcmuteaddon.mod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class VcCommands {

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("vcmod")
            // lockdown on/off
            .then(Commands.literal("lockdown").requires(src -> src.hasPermission(2))
                .then(Commands.literal("on")
                    .executes(ctx -> setLock(ctx.getSource(), true)))
                .then(Commands.literal("off")
                    .executes(ctx -> setLock(ctx.getSource(), false))))

            // mute/unmute jugador
            .then(Commands.literal("mute").requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", StringArgumentType.word())
                    .executes(ctx -> mutePlayer(ctx.getSource(),
                            StringArgumentType.getString(ctx, "player")))))
            .then(Commands.literal("unmute").requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", StringArgumentType.word())
                    .executes(ctx -> unmutePlayer(ctx.getSource(),
                            StringArgumentType.getString(ctx, "player")))))

            // list: mostrar estado completo
            .then(Commands.literal("list").executes(ctx -> listState(ctx.getSource())))

            // mute/unmute grupo por nombre (grupo de SVC)
            .then(Commands.literal("mutegroup").requires(src -> src.hasPermission(2))
                .then(Commands.argument("group", StringArgumentType.word())
                    .executes(ctx -> { VcModeration.muteGroup(StringArgumentType.getString(ctx, "group"));
                        msg(ctx.getSource(), "Group muted."); return 1; })))
            .then(Commands.literal("unmutegroup").requires(src -> src.hasPermission(2))
                .then(Commands.argument("group", StringArgumentType.word())
                    .executes(ctx -> { VcModeration.unmuteGroup(StringArgumentType.getString(ctx, "group"));
                        msg(ctx.getSource(), "Group unmuted."); return 1; })))

            // allow / disallow (whitelist de oradores)
            .then(Commands.literal("allow").requires(src -> src.hasPermission(2))
                .then(Commands.literal("player")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> allowPlayer(ctx.getSource(),
                                StringArgumentType.getString(ctx, "player"), true))))
                .then(Commands.literal("group")
                    .then(Commands.argument("group", StringArgumentType.word())
                        .executes(ctx -> { VcModeration.allowGroup(StringArgumentType.getString(ctx, "group"));
                            msg(ctx.getSource(), "Group allowed to speak."); return 1; }))))
            .then(Commands.literal("disallow").requires(src -> src.hasPermission(2))
                .then(Commands.literal("player")
                    .then(Commands.argument("player", StringArgumentType.word())
                        .executes(ctx -> allowPlayer(ctx.getSource(),
                                StringArgumentType.getString(ctx, "player"), false))))
                .then(Commands.literal("group")
                    .then(Commands.argument("group", StringArgumentType.word())
                        .executes(ctx -> { VcModeration.disallowGroup(StringArgumentType.getString(ctx, "group"));
                            msg(ctx.getSource(), "Group removed from allowed list."); return 1; }))))
        );
    }

    private static int setLock(CommandSourceStack src, boolean on) {
        VcModeration.setLockdown(on);
        msg(src, on ? "Lockdown ENABLED: only allowed players can speak." : "Lockdown DISABLED.");
        return 1;
    }

    private static int mutePlayer(CommandSourceStack src, String name) {
        UUID u = resolveUuid(src, name);
        if (u == null) { msg(src, "Could not resolve UUID for " + name); return 0; }
        VcModeration.mutePlayer(u);
        
        var sp = src.getServer().getPlayerList().getPlayer(u);
        if (sp != null) {
            // Mensaje al jugador
            sp.sendSystemMessage(Component.literal("[VCMod] You have been muted."));
            // Notificar al cliente para mostrar icono
            com.kevo.svcmuteaddon.net.Net.sendMuteStatus(sp, true);
        }
        
        msg(src, "Muted player: " + name);
        return 1;
    }

    private static int unmutePlayer(CommandSourceStack src, String name) {
        UUID u = resolveUuid(src, name);
        if (u == null) { msg(src, "Could not resolve UUID for " + name); return 0; }
        VcModeration.unmutePlayer(u);
        
        var sp = src.getServer().getPlayerList().getPlayer(u);
        if (sp != null) {
            // Mensaje al jugador
            sp.sendSystemMessage(Component.literal("[VCMod] You have been unmuted."));
            // Notificar al cliente para ocultar icono
            com.kevo.svcmuteaddon.net.Net.sendMuteStatus(sp, false);
        }
        
        msg(src, "Unmuted player: " + name);
        return 1;
    }

    private static int allowPlayer(CommandSourceStack src, String name, boolean add) {
        UUID u = resolveUuid(src, name);
        if (u == null) { msg(src, "Could not resolve UUID for " + name); return 0; }
        if (add) {
            VcModeration.allowPlayer(u);
            msg(src, "Player allowed to speak: " + name);
        } else {
            VcModeration.disallowPlayer(u);
            msg(src, "Player removed from allowed list: " + name);
        }
        return 1;
    }

    /** Resuelve el UUID del jugador por nombre usando la ProfileCache del server. */
    private static UUID resolveUuid(CommandSourceStack src, String name) {
        var srv = src.getServer();
        var prof = srv.getProfileCache().get(name).orElse(null);
        return (prof == null) ? null : prof.getId();
    }

    private static int listState(CommandSourceStack src) {
        var mutedUuids = VcModeration.getMutedPlayers();
        var mutedGroups = VcModeration.getMutedGroups();
        var allowedUuids = VcModeration.getAllowedPlayers();
        var allowedGroups = VcModeration.getAllowedGroups();

        msg(src, "Lockdown: " + (VcModeration.isLockdown() ? "ON" : "OFF"));

        // Nombres legibles para conectados; si no, UUID corto
        java.util.function.Function<UUID, String> fmt = (uuid) -> {
            var sp = src.getServer().getPlayerList().getPlayer(uuid);
            if (sp != null) return sp.getScoreboardName();
            var prof = src.getServer().getProfileCache().get(uuid).orElse(null);
            return prof != null ? prof.getName() + " (" + uuid.toString().substring(0, 8) + ")" : uuid.toString().substring(0, 8);
        };

        msg(src, "Muted players: " + (mutedUuids.isEmpty() ? "-" :
                String.join(", ", mutedUuids.stream().map(fmt).toList())));
        msg(src, "Muted groups: " + (mutedGroups.isEmpty() ? "-" :
                String.join(", ", mutedGroups)));
        msg(src, "Allowed players: " + (allowedUuids.isEmpty() ? "-" :
                String.join(", ", allowedUuids.stream().map(fmt).toList())));
        msg(src, "Allowed groups: " + (allowedGroups.isEmpty() ? "-" :
                String.join(", ", allowedGroups)));

        return 1;
    }

    private static void msg(CommandSourceStack src, String m) {
        src.sendSuccess(() -> Component.literal("[VCMod] " + m), true);
    }
}
