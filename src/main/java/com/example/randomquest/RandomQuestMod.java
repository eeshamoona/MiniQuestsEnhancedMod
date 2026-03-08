package com.example.randomquest;

import com.example.randomquest.attachment.QuestAttachments;
import com.example.randomquest.command.QuestCommand;
import com.example.randomquest.item.ModItems;
import com.example.randomquest.menu.ModMenus;
import com.example.randomquest.network.QuestPayloads;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

@Mod(RandomQuestMod.MODID)
public class RandomQuestMod {
    public static final String MODID = "randomquest";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RandomQuestMod(IEventBus modEventBus, ModContainer modContainer) {
        QuestAttachments.register(modEventBus);
        QuestPayloads.register(modEventBus);
        ModMenus.register(modEventBus);

        // Register our custom items with the game's item registry.
        // This must happen on the mod event bus (not the common NeoForge bus).
        ModItems.register(modEventBus);

        // Add our items to the Creative Mode inventory tab(s).
        // BuildCreativeModeTabContentsEvent fires once per tab during startup —
        // we hook into INGREDIENTS so the token shows up there.
        modEventBus.addListener(this::onBuildCreativeTab);

        // Give the player a Quest Token automatically when they log in.
        // This fires on the common NeoForge bus (server-side event).
        // NOTE: This is for testing — remove or gate behind a flag for release.
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    /**
     * Adds our custom items to the Creative Mode tabs.
     *
     * How creative tabs work:
     *   - BuildCreativeModeTabContentsEvent fires for EVERY tab at startup.
     *   - We filter by tab key so we only add items to the tab(s) we care about.
     *   - CreativeModeTabs.INGREDIENTS is the "materials/components" tab (gems, ingots, etc.)
     *     which is the most natural home for a token/currency item.
     */
    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(new ItemStack(ModItems.GOLD_TOKEN.get()));
            event.accept(new ItemStack(ModItems.SILVER_TOKEN.get()));
            event.accept(new ItemStack(ModItems.BRONZE_TOKEN.get()));
        }
    }

    /**
     * Gives the player 1 of each Quest Token every time they log in.
     *
     * TODO: For dev purposes, we give all tokens every time the player logs in.
     * Make sure to remove this before creating a production build!
     */
    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            ItemStack goldToken = new ItemStack(ModItems.GOLD_TOKEN.get(), 1);
            ItemStack silverToken = new ItemStack(ModItems.SILVER_TOKEN.get(), 1);
            ItemStack bronzeToken = new ItemStack(ModItems.BRONZE_TOKEN.get(), 1);
            event.getEntity().getInventory().add(goldToken);
            event.getEntity().getInventory().add(silverToken);
            event.getEntity().getInventory().add(bronzeToken);
            LOGGER.info("[RandomQuest] Gave {} the testing tokens on login.", event.getEntity().getName().getString());
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        QuestCommand.register(event.getDispatcher());
    }
}
