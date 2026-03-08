package com.example.miniquestsenhancedmod.client;

import com.example.miniquestsenhancedmod.MiniQuestsEnhancedMod;
import com.example.miniquestsenhancedmod.menu.GachaContainerMenu;
import com.example.miniquestsenhancedmod.menu.ModMenus;
import com.example.miniquestsenhancedmod.network.QuestPayloads;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client-only mod class.
 *  – Registers the G keybinding and the GachaContainerScreen for the menu type.
 *  – Polls the key each tick (client only) and sends OpenMenuPayload to server.
 *  – Ticks the post-reward countdown; sends RequestNextQuestPayload when it hits 0.
 */
@Mod(value = MiniQuestsEnhancedMod.MODID, dist = Dist.CLIENT)
public class MiniQuestsEnhancedClient {

    public static final KeyMapping OPEN_QUEST_KEY = new KeyMapping(
            "key.miniquestsenhancedmod.open_quest",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G,
            "key.categories.miniquestsenhancedmod");

    public MiniQuestsEnhancedClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(MiniQuestsEnhancedClient::onRegisterKeyMappings);
        modEventBus.addListener(MiniQuestsEnhancedClient::onRegisterMenuScreens);
        NeoForge.EVENT_BUS.addListener(MiniQuestsEnhancedClient::onClientTick);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_QUEST_KEY);
    }

    private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.GACHA_MENU.get(), GachaContainerScreen::new);
    }

    private static void onClientTick(PlayerTickEvent.Pre event) {
        // CRITICAL: only run on the client/render thread
        if (!event.getEntity().level().isClientSide()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // ── G key → ask server to open the gacha menu ─────────────────────
        while (OPEN_QUEST_KEY.consumeClick()) {
            if (mc.screen == null) {
                PacketDistributor.sendToServer(new QuestPayloads.OpenMenuPayload());
            }
        }

        // ── Countdown tick → when it hits 0, request next quest ───────────
        if (ClientQuestCache.tickCountdown()) {
            // tickCountdown() returned true exactly once (just hit 0)
            PacketDistributor.sendToServer(new QuestPayloads.RequestNextQuestPayload());
        }
    }
}
