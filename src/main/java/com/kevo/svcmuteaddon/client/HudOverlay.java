package com.kevo.svcmuteaddon.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = "svcmuteaddon", value = Dist.CLIENT)
public class HudOverlay {
    private static final ResourceLocation MUTED_ICON = ResourceLocation.fromNamespaceAndPath("svcmuteaddon", "textures/gui/mic_muted.png");


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!ClientMuteState.isMuted()) return;

        GuiGraphics g = event.getGuiGraphics();
        var mc = Minecraft.getInstance();


        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        

        float scale = 1.5f; // 16 * 1.5 = 24px
        int newSize = (int)(16 * scale);
        

        int drawX = (screenWidth - newSize) / 2;
        int drawY = screenHeight - 50; 

        RenderSystem.enableBlend();
        g.pose().pushPose();
        g.pose().translate(drawX, drawY, 0);
        g.pose().scale(scale, scale, 1f);

        g.blit(MUTED_ICON, 0, 0, 0, 0, 16, 16, 16, 16);
        g.pose().popPose();
        RenderSystem.disableBlend();
    }
}
