package com.example.miniquestsenhancedmod.command;

import com.example.miniquestsenhancedmod.QuestData;
import com.example.miniquestsenhancedmod.attachment.QuestAttachments;
import com.example.miniquestsenhancedmod.network.QuestPayloads;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestCommand {

    /** Easy, obtainable items — great for testing. */
    private static final List<Item> EASY_ITEMS = List.of(
            Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.STICK, Items.COBBLESTONE, Items.STONE,
            Items.SMOOTH_STONE, Items.ANDESITE,
            Items.DIORITE, Items.GRANITE, Items.TUFF, Items.DEEPSLATE, Items.DIRT, Items.COARSE_DIRT, Items.ROOTED_DIRT,
            Items.MUD, Items.SAND, Items.RED_SAND, Items.GRAVEL, Items.FLINT, Items.CLAY_BALL,
            Items.COAL, Items.CHARCOAL, Items.IRON_NUGGET, Items.GOLD_NUGGET, Items.INK_SAC, Items.LEATHER,
            Items.FEATHER, Items.EGG, Items.BONE, Items.STRING, Items.SPIDER_EYE, Items.ROTTEN_FLESH, Items.APPLE,
            Items.BREAD, Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN, Items.COOKED_MUTTON,
            Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS, Items.WHEAT,
            Items.SUGAR_CANE, Items.BAMBOO, Items.KELP, Items.CACTUS, Items.SWEET_BERRIES, Items.GLOW_BERRIES,
            Items.MILK_BUCKET, Items.MOSS_BLOCK, Items.POINTED_DRIPSTONE, Items.WHITE_WOOL, Items.GLASS,
            Items.RAW_IRON, Items.RAW_COPPER, Items.OAK_SAPLING, Items.LANTERN, Items.SCAFFOLDING);

    private static final Random RANDOM = new Random();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("quest")
                        .then(Commands.literal("start")
                                .requires(src -> src.hasPermission(0))
                                .executes(QuestCommand::startQuest)));
    }

    private static int startQuest(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can use this command."));
            return 0;
        }
        assignQuestToPlayer(player);
        return 1;
    }

    /**
     * Generates a random multi-item quest and assigns it to the player.
     * Called by both /quest start and the auto-quest handler.
     *
     * Quest type weights:
     * 70% → 1 item type (qty 1–5)
     * 20% → 2 item types (qty 1–3 each)
     * 10% → 3 item types (qty 1–2 each)
     */
    public static void assignQuestToPlayer(ServerPlayer player) {
        int roll = RANDOM.nextInt(10);
        int numTypes = (roll < 7) ? 1 : (roll < 9) ? 2 : 3;

        // Pick unique items
        List<Item> pool = new ArrayList<>(EASY_ITEMS);
        List<QuestData.QuestItem> requirements = new ArrayList<>();
        for (int i = 0; i < numTypes; i++) {
            int idx = RANDOM.nextInt(pool.size());
            Item chosen = pool.remove(idx);
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(chosen);

            int maxQty = (numTypes == 1) ? 5 : (numTypes == 2) ? 3 : 2;
            int qty = 1 + RANDOM.nextInt(maxQty);

            requirements.add(new QuestData.QuestItem(key.toString(), qty, 0));
        }

        QuestData quest = new QuestData(List.copyOf(requirements));
        String encoded = quest.encode();

        player.setData(QuestAttachments.CURRENT_QUEST, encoded);

        // Build announcement
        StringBuilder msg = new StringBuilder("§6Quest started! Deliver: §e");
        for (int i = 0; i < requirements.size(); i++) {
            QuestData.QuestItem req = requirements.get(i);
            Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(req.itemKey()))
                    .orElse(net.minecraft.world.item.Items.AIR);
            String name = item.getDefaultInstance().getHoverName().getString();
            if (i > 0)
                msg.append(i == requirements.size() - 1 ? " §6+ §e" : ", ");
            msg.append(req.required()).append("× ").append(name);
        }
        player.sendSystemMessage(Component.literal(msg.toString()));

        PacketDistributor.sendToPlayer(player, new QuestPayloads.QuestSyncPayload(encoded));
    }
}
