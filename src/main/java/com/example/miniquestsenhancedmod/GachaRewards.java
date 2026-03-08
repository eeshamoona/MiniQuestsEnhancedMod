package com.example.miniquestsenhancedmod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weighted gacha reward pool. Rolled server-side on quest submission.
 *
 * Tiers:
 * Common (~60%) – basic consumables / materials
 * Uncommon (~30%) – mid-tier crafting items
 * Rare (~10%) – diamonds, emeralds, saddle
 */
public final class GachaRewards {

    public record RewardEntry(ItemStack stack, int effort, int skip) {
        public int weight() {
            // Formula from 2-Question Framework: 10 / (Effort + Skip)
            return Math.max(1, 10 / (effort + skip));
        }
    }

    public static final List<RewardEntry> COMMON_POOL = List.of(
            new RewardEntry(new ItemStack(Items.EXPERIENCE_BOTTLE, 16), 3, 2),
            new RewardEntry(new ItemStack(Items.IRON_BLOCK, 1), 3, 3),
            new RewardEntry(new ItemStack(Items.FIREWORK_ROCKET, 32), 2, 2),
            new RewardEntry(new ItemStack(com.example.miniquestsenhancedmod.item.ModItems.BRONZE_TOKEN.get(), 1), 1,
                    1));

    public static final List<RewardEntry> UNCOMMON_POOL = List.of(
            new RewardEntry(new ItemStack(Items.GOLDEN_APPLE, 2), 3, 3),
            new RewardEntry(new ItemStack(Items.ENDER_PEARL, 2), 3, 4),
            new RewardEntry(new ItemStack(Items.BLAZE_ROD, 4), 4, 4),
            new RewardEntry(new ItemStack(Items.EMERALD_BLOCK, 2), 4, 3),
            new RewardEntry(new ItemStack(com.example.miniquestsenhancedmod.item.ModItems.SILVER_TOKEN.get(), 1), 3,
                    2));

    public static final List<RewardEntry> RARE_EPIC_POOL = List.of(
            new RewardEntry(new ItemStack(Items.DIAMOND_BLOCK, 1), 5, 5),
            new RewardEntry(new ItemStack(Items.NETHERITE_INGOT, 1), 5, 5),
            new RewardEntry(new ItemStack(Items.TOTEM_OF_UNDYING, 1), 5, 5),
            new RewardEntry(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1), 5, 5),
            new RewardEntry(new ItemStack(Items.SHULKER_BOX, 1), 5, 5),
            new RewardEntry(new ItemStack(com.example.miniquestsenhancedmod.item.ModItems.GOLD_TOKEN.get(), 1), 5, 4));

    private static final int COMMON_WEIGHT = COMMON_POOL.stream().mapToInt(RewardEntry::weight).sum();
    private static final int UNCOMMON_WEIGHT = UNCOMMON_POOL.stream().mapToInt(RewardEntry::weight).sum();
    private static final int RARE_EPIC_WEIGHT = RARE_EPIC_POOL.stream().mapToInt(RewardEntry::weight).sum();

    /** All reward stacks, used client-side for the spin animation scroll. */
    public static final List<ItemStack> ALL_STACKS = java.util.stream.Stream
            .of(COMMON_POOL, UNCOMMON_POOL, RARE_EPIC_POOL)
            .flatMap(List::stream).map(e -> e.stack().copy()).toList();

    /**
     * Thread-safe: roll a random reward based on quest difficulty (number of input
     * types). Returns a fresh copy.
     */
    public static ItemStack roll(int difficulty) {
        double chance = ThreadLocalRandom.current().nextDouble();
        List<RewardEntry> selectedPool;
        int totalWeight;

        if (difficulty <= 1) {
            // Difficulty 1: 20% Uncommon, 80% Common
            if (chance < 0.20) {
                selectedPool = UNCOMMON_POOL;
                totalWeight = UNCOMMON_WEIGHT;
            } else {
                selectedPool = COMMON_POOL;
                totalWeight = COMMON_WEIGHT;
            }
        } else if (difficulty == 2) {
            // Difficulty 2: 50% Uncommon, 50% Common
            if (chance < 0.50) {
                selectedPool = UNCOMMON_POOL;
                totalWeight = UNCOMMON_WEIGHT;
            } else {
                selectedPool = COMMON_POOL;
                totalWeight = COMMON_WEIGHT;
            }
        } else {
            // Difficulty 3+: 20% Rare/Epic, 80% Uncommon
            if (chance < 0.20) {
                selectedPool = RARE_EPIC_POOL;
                totalWeight = RARE_EPIC_WEIGHT;
            } else {
                selectedPool = UNCOMMON_POOL;
                totalWeight = UNCOMMON_WEIGHT;
            }
        }

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;
        for (RewardEntry entry : selectedPool) {
            cumulative += entry.weight();
            if (roll < cumulative) {
                return entry.stack().copy();
            }
        }
        return new ItemStack(Items.BREAD, 8); // fallback
    }

    /** Overload for fallback/dummy rolling logic if required elsewhere */
    public static ItemStack roll() {
        return roll(1);
    }

    private GachaRewards() {
    }
}
