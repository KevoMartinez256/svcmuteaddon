package com.kevo.svcmuteaddon.net;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = "svcmuteaddon")
public final class Net {
    public static final String PROTOCOL_VERSION = "1"; 
    public static final ResourceLocation CHANNEL_ID = ResourceLocation.fromNamespaceAndPath("svcmuteaddon", "main");


    public static void register() { /* No-op: el EventBusSubscriber ya registra el listener */ }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        // playToClient => servidor envía al cliente
        registrar.playToClient(MuteStatusS2C.TYPE, MuteStatusS2C.STREAM_CODEC, (payload, ctx) -> payload.handle(ctx));
    }

    public static void sendMuteStatus(ServerPlayer target, boolean muted) {
        PacketDistributor.sendToPlayer(target, new MuteStatusS2C(muted));
    }
}
