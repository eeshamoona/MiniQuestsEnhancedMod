package com.example.miniquestsenhancedmod.attachment;

import com.example.miniquestsenhancedmod.MiniQuestsEnhancedMod;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Registers the Data Attachment type used to track a player's current quest.
 * The attachment stores the ResourceLocation of the quest item as a plain String
 * (e.g. "minecraft:diamond"). An empty string means "no active quest".
 */
public class QuestAttachments {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MiniQuestsEnhancedMod.MODID);

    /**
     * The current quest item's registry key, stored per-player.
     * Defaults to an empty string (= no quest).
     */
    public static final Supplier<AttachmentType<String>> CURRENT_QUEST =
            ATTACHMENT_TYPES.register("current_quest", () ->
                    AttachmentType.builder(() -> "")
                            .serialize(Codec.STRING)
                            .build()
            );

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
