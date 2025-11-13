package com.kevo.svcmuteaddon.mod;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VcModeration {

    // Modo whitelist: solo los "permitidos" pueden hablar
    private static volatile boolean lockdown = false;

    private static final Set<UUID> mutedPlayers   = ConcurrentHashMap.newKeySet();
    private static final Set<String> mutedGroups  = ConcurrentHashMap.newKeySet(); // nombres en lower
    private static final Set<UUID> allowedPlayers = ConcurrentHashMap.newKeySet();
    private static final Set<String> allowedGroups= ConcurrentHashMap.newKeySet();

    public static void setLockdown(boolean on) { lockdown = on; }
    public static boolean isLockdown() { return lockdown; }

    public static void mutePlayer(UUID u) { mutedPlayers.add(u); }
    public static void unmutePlayer(UUID u) { mutedPlayers.remove(u); }

    public static void muteGroup(String g) { if (g != null) mutedGroups.add(g.toLowerCase()); }
    public static void unmuteGroup(String g) { if (g != null) mutedGroups.remove(g.toLowerCase()); }

    public static void allowPlayer(UUID u) { allowedPlayers.add(u); }
    public static void disallowPlayer(UUID u) { allowedPlayers.remove(u); }

    public static void allowGroup(String g) { if (g != null) allowedGroups.add(g.toLowerCase()); }
    public static void disallowGroup(String g) { if (g != null) allowedGroups.remove(g.toLowerCase()); }


    public static Set<UUID> getMutedPlayers() { return Set.copyOf(mutedPlayers); }
    public static Set<String> getMutedGroups() { return Set.copyOf(mutedGroups); }
    public static Set<UUID> getAllowedPlayers() { return Set.copyOf(allowedPlayers); }
    public static Set<String> getAllowedGroups() { return Set.copyOf(allowedGroups); }


    public static boolean shouldMute(UUID sender, String groupNameOrNull) {
        if (sender == null) return false;

        // 1) mute explícito
        if (mutedPlayers.contains(sender)) return true;
        if (groupNameOrNull != null && mutedGroups.contains(groupNameOrNull.toLowerCase())) return true;

        // 2) lockdown: solo whitelisted (jugador o grupo)
        if (lockdown) {
            boolean playerOk = allowedPlayers.contains(sender);
            boolean groupOk  = groupNameOrNull != null && allowedGroups.contains(groupNameOrNull.toLowerCase());
            return !(playerOk || groupOk);
        }
        return false;
    }
}
