# Quest Items & Rewards Reference

> **Source of truth** — update this file first when adding/removing items or rewards, then update the matching code in `QuestCommand.java` and `GachaRewards.java`.

---

## Quest Input Items

All possible quest items are drawn from `EASY_ITEMS` in [`QuestCommand.java`](src/main/java/com/example/miniquestsenhancedmod/command/QuestCommand.java). Every item here is obtainable in standard survival within the first few minutes of gameplay.

| Category | Items |
|----------|-------|
| **Wood & Basics** | Oak Log, Spruce Log, Birch Log, Jungle Log, Acacia Log, Dark Oak Log, Cherry Log, Stick, Oak Sapling |
| **Stone & Mining** | Cobblestone, Stone, Smooth Stone, Andesite, Diorite, Granite, Tuff, Deepslate, Gravel, Flint, Raw Iron, Raw Copper |
| **Nature** | Dirt, Coarse Dirt, Rooted Dirt, Mud, Grass Block, Sand, Red Sand, Clay Ball, Wheat Seeds, Pumpkin Seeds, Melon Seeds, Beetroot Seeds, Wheat, Sugar Cane, Bamboo, Kelp, Cactus, Sweet Berries, Glow Berries, Moss Block, Pointed Dripstone, White Wool, Glass |
| **Crafting Components** | Coal, Charcoal, Iron Nugget, Gold Nugget, Lead, Lantern, Scaffolding |
| **Mob Drops** | Ink Sac, Leather, Feather, Egg, Bone, String, Spider Eye, Rotten Flesh |
| **Food** | Apple, Bread, Cooked Beef, Cooked Porkchop, Cooked Chicken, Cooked Mutton, Milk Bucket |

**Total pool: 66 items**

### Quest Type Weighting

| Quest Type | Probability | Item Types | Qty per Type |
|------------|-------------|------------|--------------|
| Single item | **70%** | 1 | 1–5 |
| Two items | **20%** | 2 distinct | 1–3 each |
| Three items | **10%** | 3 distinct | 1–2 each |

> **To add items**: Add to the `EASY_ITEMS` list in `QuestCommand.java`.  
> **To change quantities**: Update `maxQty` per tier in `assignQuestToPlayer()`.  
> **To change type weights**: Update the `roll < 7`, `roll < 9` thresholds in `assignQuestToPlayer()`.

---

## Rewards

Rewards are rolled server-side via [`GachaRewards.java`](src/main/java/com/example/miniquestsenhancedmod/GachaRewards.java) using a weighted pool. The reward appears in the **output slot** of the UI after all quest requirements are fulfilled.

Weighting formula based on the **2-Question Framework**:
`Weight = Math.max(1, 10 / (Effort + Skip))`

### Difficulty Scaling (The "Roll" Logic)

The reward pool is now dynamically selected based on the number of item types requested in the quest:

| Quest Difficulty | Input Types | Reward Probability |
|------------------|-------------|--------------------|
| **Difficulty 1** | 1 Type | 20% Uncommon, 80% Common |
| **Difficulty 2** | 2 Types | 50% Uncommon, 50% Common |
| **Difficulty 3** | 3 Types | 20% Rare/Epic, 80% Uncommon |

### Reward Tiers

| Tier | Reward | Quantity | Effort | Skip | Weight |
|------|--------|----------|--------|------|--------|
| **Common** | Experience Bottle | ×16 | 3 | 2 | 2 |
| Common | Iron Block | ×1 | 3 | 3 | 1 |
| Common | Firework Rocket | ×32 | 2 | 2 | 2 |
| Common | Bronze Token | ×1 | 1 | 1 | 5 |
| **Uncommon** | Golden Apple | ×2 | 3 | 3 | 1 |
| Uncommon | Ender Pearl | ×2 | 3 | 4 | 1 |
| Uncommon | Blaze Rod | ×4 | 4 | 4 | 1 |
| Uncommon | Emerald Block | ×2 | 4 | 3 | 1 |
| Uncommon | Silver Token | ×1 | 3 | 2 | 2 |
| **Rare / Epic** | Diamond Block | ×1 | 5 | 5 | 1 |
| Rare / Epic | Netherite Ingot | ×1 | 5 | 5 | 1 |
| Rare / Epic | Totem of Undying | ×1 | 5 | 5 | 1 |
| Rare / Epic | Enchanted Golden Apple | ×1 | 5 | 5 | 1 |
| Rare / Epic | Shulker Box | ×1 | 5 | 5 | 1 |
| Rare / Epic | Gold Token | ×1 | 5 | 4 | 1 |

> Effective chance within a tier = `Weight / Tier Total Weight x 100%`

### Adding / Changing Rewards

Edit `POOL` in `GachaRewards.java`:
```java
new RewardEntry(new ItemStack(Items.YOUR_ITEM, quantity), weight)
```
Then adjust the `TOTAL_WEIGHT` constant is computed automatically via stream sum — no manual update needed.

---

## Notes for the Coding Agent

- Quest items (`EASY_ITEMS`) and rewards (`POOL`) are **intentionally separate lists** — quest inputs and reward outputs should rarely overlap to keep the loop feeling fresh.
- Enchanted items as rewards require registry access at runtime; defer until server data-driven reward tables are implemented.
- The quest data wire format is: `namespace:item/required/submitted|namespace:item/required/submitted` — parsed by `QuestData.parse()`.
