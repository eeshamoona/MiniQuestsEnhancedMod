package com.example.miniquestsenhancedmod;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weighted gacha reward pool. Rolled server-side on quest submission.
 *
 * Tiers:
 *   Common   (~60%) – basic consumables / materials
 *   Uncommon (~30%) – mid-tier crafting items
 *   Rare     (~10%) – diamonds, emeralds, saddle
 */
public final class GachaRewards {

    public record RewardEntry(ItemStack stack, int weight) {}

    private static final List<RewardEntry> POOL = List.of(
            // ── Common ──────────────────────────────────────────────
            new RewardEntry(new ItemStack(Items.BREAD, 8),          15),
            new RewardEntry(new ItemStack(Items.TORCH, 16),         12),
            new RewardEntry(new ItemStack(Items.ARROW, 16),         12),
            new RewardEntry(new ItemStack(Items.COOKED_CHICKEN, 4), 11),
            new RewardEntry(new ItemStack(Items.OAK_PLANKS, 32),    10),
            // ── Uncommon ────────────────────────────────────────────
            new RewardEntry(new ItemStack(Items.IRON_INGOT, 4),     12),
            new RewardEntry(new ItemStack(Items.GOLD_INGOT, 2),      9),
            new RewardEntry(new ItemStack(Items.BOW),                9),
            new RewardEntry(new ItemStack(com.example.miniquestsenhancedmod.item.ModItems.BRONZE_TOKEN.get(), 1), 15),
            // ── Rare / Epic ──────────────────────────────────────────
            new RewardEntry(new ItemStack(Items.DIAMOND, 2),         5),
            new RewardEntry(new ItemStack(Items.EMERALD, 3),          3),
            new RewardEntry(new ItemStack(Items.SADDLE),              2),
            new RewardEntry(new ItemStack(com.example.miniquestsenhancedmod.item.ModItems.SILVER_TOKEN.get(), 1), 8),
            new RewardEntry(new ItemStack(com.example.miniquestsenhancedmod.item.ModItems.GOLD_TOKEN.get(), 1), 3)
    );

    private static final int TOTAL_WEIGHT =
            POOL.stream().mapToInt(RewardEntry::weight).sum();

    /** All reward stacks, used client-side for the spin animation scroll. */
    public static final List<ItemStack> ALL_STACKS =
            POOL.stream().map(e -> e.stack().copy()).toList();

    /** Thread-safe: roll a random reward. Returns a fresh copy. */
    public static ItemStack roll() {
        int roll = ThreadLocalRandom.current().nextInt(TOTAL_WEIGHT);
        int cumulative = 0;
        for (RewardEntry entry : POOL) {
            cumulative += entry.weight();
            if (roll < cumulative) return entry.stack().copy();
        }
        return new ItemStack(Items.BREAD, 8); // unreachable fallback
    }

    private GachaRewards() {}
}
