package com.example.randomquest.item;

import com.example.randomquest.RandomQuestMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Central registry for all custom items in the mod.
 *
 * Pattern: NeoForge DeferredRegister
 *   1. Create a DeferredRegister for Registries.ITEM under our mod ID.
 *   2. For each item, call ITEMS.register(id, factory) — this returns a
 *      DeferredHolder, which is a lazy reference that resolves AFTER
 *      registration completes. NEVER call .get() at class-load time.
 *   3. Call ModItems.register(modEventBus) from the mod constructor so
 *      NeoForge knows to fire our registration events.
 * Tokens:
 *   - GOLD_TOKEN    (epic tier — Rarity.EPIC)
 *   - SILVER_TOKEN  (rare tier — Rarity.RARE)
 *   - BRONZE_TOKEN  (uncommon tier — Rarity.UNCOMMON)
 */
public class ModItems {

    // In NeoForge 1.21+, we must use DeferredRegister.Items (not DeferredRegister<Item>)
    // so that it automatically injects the Registry Key into the item properties BEFORE the item is constructed.
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(RandomQuestMod.MODID);

    // ── Tokens ─────────────────────────────────────────────────────────────────
    // Registry ID: "randomquest:gold_token"
    // In-game name comes from lang key: "item.randomquest.gold_token"
    public static final DeferredItem<Item> GOLD_TOKEN =
            ITEMS.registerItem("gold_token", props -> new TokenItem(props
                    .stacksTo(64)
                    .rarity(net.minecraft.world.item.Rarity.EPIC)));

    public static final DeferredItem<Item> SILVER_TOKEN =
            ITEMS.registerItem("silver_token", props -> new TokenItem(props
                    .stacksTo(64)
                    .rarity(net.minecraft.world.item.Rarity.RARE)));

    public static final DeferredItem<Item> BRONZE_TOKEN =
            ITEMS.registerItem("bronze_token", props -> new TokenItem(props
                    .stacksTo(64)
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    // ── Wiring ─────────────────────────────────────────────────────────────────
    // Called once from RandomQuestMod constructor.
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
