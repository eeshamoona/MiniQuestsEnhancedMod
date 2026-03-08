package com.example.randomquest.network;

import com.example.randomquest.RandomQuestMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * All network payloads for the RandomQuest mod.
 *
 * C→S:
 *   OpenMenuPayload     – player pressed G
 *   SubmitQuestPayload  – player clicked SUBMIT
 *   RequestNextQuestPayload – countdown hit 0, auto-assign next quest
 *
 * S→C:
 *   QuestSyncPayload    – server sends current quest key (empty = no quest)
 *   GachaResultPayload  – server sends rolled reward after SUBMIT
 */
public class QuestPayloads {

    // ── C→S: open the gacha menu ──────────────────────────────────────────
    public record OpenMenuPayload() implements CustomPacketPayload {
        public static final Type<OpenMenuPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RandomQuestMod.MODID, "open_menu"));
        public static final StreamCodec<FriendlyByteBuf, OpenMenuPayload> STREAM_CODEC =
                StreamCodec.unit(new OpenMenuPayload());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ── C→S: submit the quest (item is in input slot) ─────────────────────
    public record SubmitQuestPayload() implements CustomPacketPayload {
        public static final Type<SubmitQuestPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RandomQuestMod.MODID, "submit_quest"));
        public static final StreamCodec<FriendlyByteBuf, SubmitQuestPayload> STREAM_CODEC =
                StreamCodec.unit(new SubmitQuestPayload());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ── C→S: countdown hit 0, request next auto-quest ─────────────────────
    public record RequestNextQuestPayload() implements CustomPacketPayload {
        public static final Type<RequestNextQuestPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RandomQuestMod.MODID, "request_next_quest"));
        public static final StreamCodec<FriendlyByteBuf, RequestNextQuestPayload> STREAM_CODEC =
                StreamCodec.unit(new RequestNextQuestPayload());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ── S→C: sync current quest key ──────────────────────────────────────
    public record QuestSyncPayload(String questItemKey) implements CustomPacketPayload {
        public static final Type<QuestSyncPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RandomQuestMod.MODID, "quest_sync"));
        public static final StreamCodec<FriendlyByteBuf, QuestSyncPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, QuestSyncPayload::questItemKey,
                        QuestSyncPayload::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ── S→C: reward rolled after submission ──────────────────────────────
    public record GachaResultPayload(ItemStack reward) implements CustomPacketPayload {
        public static final Type<GachaResultPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RandomQuestMod.MODID, "gacha_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, GachaResultPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ItemStack.OPTIONAL_STREAM_CODEC, GachaResultPayload::reward,
                        GachaResultPayload::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    // ── Registration ──────────────────────────────────────────────────────
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(QuestPayloads::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar(RandomQuestMod.MODID);

        reg.playToServer(OpenMenuPayload.TYPE,        OpenMenuPayload.STREAM_CODEC,        QuestNetworkHandler::handleOpenMenu);
        reg.playToServer(SubmitQuestPayload.TYPE,     SubmitQuestPayload.STREAM_CODEC,     QuestNetworkHandler::handleSubmit);
        reg.playToServer(RequestNextQuestPayload.TYPE, RequestNextQuestPayload.STREAM_CODEC, QuestNetworkHandler::handleRequestNextQuest);
        reg.playToClient(QuestSyncPayload.TYPE,       QuestSyncPayload.STREAM_CODEC,       QuestNetworkHandler::handleQuestSync);
        reg.playToClient(GachaResultPayload.TYPE,     GachaResultPayload.STREAM_CODEC,     QuestNetworkHandler::handleGachaResult);
    }
}
