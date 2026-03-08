package com.example.miniquestsenhancedmod.network;

import com.example.miniquestsenhancedmod.GachaRewards;
import com.example.miniquestsenhancedmod.QuestData;
import com.example.miniquestsenhancedmod.attachment.QuestAttachments;
import com.example.miniquestsenhancedmod.client.ClientQuestCache;
import com.example.miniquestsenhancedmod.client.GachaContainerScreen;
import com.example.miniquestsenhancedmod.command.QuestCommand;
import com.example.miniquestsenhancedmod.menu.GachaContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class QuestNetworkHandler {

    // ── C→S: open the gacha menu ──────────────────────────────────────────
    public static void handleOpenMenu(QuestPayloads.OpenMenuPayload payload,
            IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            String encoded = player.getData(QuestAttachments.CURRENT_QUEST);
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new GachaContainerMenu(id, inv, p),
                            Component.translatable("container.miniquestsenhancedmod.gacha")),
                    buf -> buf.writeUtf(encoded));
        });
    }

    // ── C→S: player clicked SUBMIT ────────────────────────────────────────
    public static void handleSubmit(QuestPayloads.SubmitQuestPayload payload,
            IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            if (!(player.containerMenu instanceof GachaContainerMenu menu))
                return;

            ItemStack inputStack = menu.getInputItem();
            if (inputStack.isEmpty())
                return;

            // Parse quest and try to satisfy a requirement
            String encoded = player.getData(QuestAttachments.CURRENT_QUEST);
            QuestData quest = QuestData.parse(encoded);
            if (quest.isEmpty())
                return;

            QuestData.SubmitResult result = quest.trySubmit(inputStack.getItem(), inputStack.getCount());
            if (result == null) {
                player.sendSystemMessage(Component.literal("§cThat item isn't needed for this quest."));
                return;
            }

            // Consume exactly what was needed; leave any excess in the slot
            int leftover = inputStack.getCount() - result.consumed();
            if (leftover > 0) {
                ItemStack remainder = inputStack.copy();
                remainder.setCount(leftover);
                menu.getSlot(0).set(remainder);
            } else {
                menu.clearInput();
            }

            // Save updated quest data
            QuestData updatedQuest = result.updated();
            player.setData(QuestAttachments.CURRENT_QUEST, updatedQuest.encode());

            // Sync progress to client
            PacketDistributor.sendToPlayer(player, new QuestPayloads.QuestSyncPayload(updatedQuest.encode()));
            player.containerMenu.broadcastChanges();

            // Check if quest is fully complete
            if (updatedQuest.isComplete()) {
                int difficulty = updatedQuest.items().size();
                ItemStack reward = GachaRewards.roll(difficulty);
                menu.setReward(reward);
                player.containerMenu.broadcastChanges();

                player.setData(QuestAttachments.CURRENT_QUEST, "");
                player.sendSystemMessage(Component.literal("§a✔ Quest complete! Claim your reward."));

                PacketDistributor.sendToPlayer(player, new QuestPayloads.GachaResultPayload(reward.copy()));
                PacketDistributor.sendToPlayer(player, new QuestPayloads.QuestSyncPayload(""));
            } else {
                // Partial progress message
                String itemName = inputStack.getItem().getDefaultInstance().getHoverName().getString();
                player.sendSystemMessage(Component.literal(
                        "§6Submitted: §e" + itemName + " §6(" +
                                updatedQuest.completedCount() + "/" + updatedQuest.totalCount() + " done)"));
            }
        });
    }

    // ── C→S: countdown hit 0 ─────────────────────────────────────────────
    public static void handleRequestNextQuest(QuestPayloads.RequestNextQuestPayload payload,
            IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            QuestCommand.assignQuestToPlayer(player);
        });
    }

    // ── S→C: sync quest string ────────────────────────────────────────────
    public static void handleQuestSync(QuestPayloads.QuestSyncPayload payload,
            IPayloadContext context) {
        context.enqueueWork(() -> ClientQuestCache.setQuestString(payload.questItemKey()));
    }

    // ── S→C: reward ready — trigger sparkle ──────────────────────────────
    public static void handleGachaResult(QuestPayloads.GachaResultPayload payload,
            IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof GachaContainerScreen screen) {
                screen.onRewardReceived(payload.reward());
            }
            ClientQuestCache.startCountdown();
        });
    }
}
