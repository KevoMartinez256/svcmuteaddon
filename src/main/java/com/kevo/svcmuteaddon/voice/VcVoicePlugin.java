package com.kevo.svcmuteaddon.voice;

import com.kevo.svcmuteaddon.mod.VcModeration;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;

@ForgeVoicechatPlugin
public class VcVoicePlugin implements VoicechatPlugin {

    public static volatile VoicechatServerApi VC_API;

    @Override
    public String getPluginId() {

        return "svcmuteaddon";
    }

    @Override
    public void initialize(VoicechatApi api) {

        System.out.println("[SVCMuteAddon] initialize() – Voicechat API inicializada");
    }

    @Override
    public void registerEvents(EventRegistration reg) {
        System.out.println("[SVCMuteAddon] registerEvents()");

        reg.registerEvent(VoicechatServerStartedEvent.class, e -> {
            VC_API = e.getVoicechat();
            System.out.println("[SVCMuteAddon] VoicechatServerStarted");
        });

        reg.registerEvent(VoicechatServerStoppedEvent.class, e -> {
            VC_API = null;
            System.out.println("[SVCMuteAddon] VoicechatServerStopped");
        });

        reg.registerEvent(MicrophonePacketEvent.class, e -> {
            var conn = e.getSenderConnection();
            if (conn == null || conn.getPlayer() == null) {
                System.out.println("[SVCMuteAddon] MicPkt sin conexión/jugador");
                return;
            }

            var senderUuid = conn.getPlayer().getUuid(); 
            var groupName  = (conn.getGroup() == null) ? null : conn.getGroup().getName();

            System.out.println("[SVCMuteAddon] MicPkt de " + senderUuid +
                    " en " + (groupName == null ? "(proximidad)" : groupName) +
                    " – cancellable=" + e.isCancellable());

            if (!e.isCancellable()) return;

            if (VcModeration.shouldMute(senderUuid, groupName)) {
                System.out.println("[SVCMuteAddon] >>> CANCELADO paquete de " + senderUuid);
                e.cancel(); 
            }
        });
    }
}
