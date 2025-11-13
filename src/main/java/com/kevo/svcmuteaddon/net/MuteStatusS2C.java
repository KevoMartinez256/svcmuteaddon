package com.kevo.svcmuteaddon.net;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MuteStatusS2C(boolean muted) implements CustomPacketPayload {
    public static final Type<MuteStatusS2C> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("svcmuteaddon", "mute_status"));

    public static final StreamCodec<FriendlyByteBuf, MuteStatusS2C> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    MuteStatusS2C::muted,
                    MuteStatusS2C::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


        public void handle(IPayloadContext ctx) {

                ctx.enqueueWork(() -> com.kevo.svcmuteaddon.client.ClientMuteState.setMuted(this.muted()));
        }
}
