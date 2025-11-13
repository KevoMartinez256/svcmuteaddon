package com.kevo.svcmuteaddon;

import com.kevo.svcmuteaddon.mod.VcCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;        
import net.neoforged.neoforge.event.RegisterCommandsEvent; 

@Mod("svcmuteaddon")
public class VcMod {
    public VcMod() {

        com.kevo.svcmuteaddon.net.Net.register();
    }


    @EventBusSubscriber(modid = "svcmuteaddon")
    public static class Events {
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent e) {
            VcCommands.register(e.getDispatcher());
        }

        @SubscribeEvent
        public static void onJoin(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e) {
            if (!(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)) return;
            boolean muted = com.kevo.svcmuteaddon.mod.VcModeration.getMutedPlayers().contains(sp.getUUID());
            com.kevo.svcmuteaddon.net.Net.sendMuteStatus(sp, muted);
        }
    }
}
