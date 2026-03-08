# Quest Items & Rewards Reference

> **Source of truth** — update this file first when adding/removing items or rewards, then update the matching code in `QuestCommand.java` and `GachaRewards.java`.

---

## Quest Input Items

All possible quest items are drawn from `EASY_ITEMS` in [`QuestCommand.java`](src/main/java/com/example/randomquest/command/QuestCommand.java). Every item here is obtainable in standard survival within the first few minutes of gameplay.

| Category | Items |
|----------|-------|
| **Wood & Basics** | Oak Log, Oak Planks, Oak Sapling, Stick |
| **Stone & Mining** | Cobblestone, Stone, Gravel, Flint |
| **Nature** | Dirt, Sand, Grass Block, Oak Leaves, Birch Leaves, Fern, Dandelion, Poppy |
| **Food** | Apple, Bread, Cooked Beef, Cooked Chicken, Cooked Porkchop, Carrot, Potato |
| **Tools** | Wooden Pickaxe, Stone Pickaxe, Wooden Sword, Wooden Axe, Wooden Shovel |
| **Animal Drops** | Leather, Feather, Egg, Bone, String |
| **Crafting Components** | Coal, Iron Ingot, Iron Nugget, Torch, Crafting Table, Furnace, Chest |
| **Fish** | Cod, Salmon, Tropical Fish |

**Total pool: 43 items**

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

Rewards are rolled server-side via [`GachaRewards.java`](src/main/java/com/example/randomquest/GachaRewards.java) using a weighted pool. The reward appears in the **output slot** of the UI after all quest requirements are fulfilled.

### Reward Pool

| Tier | Effective Chance | Reward | Quantity | Weight |
|------|-----------------|--------|----------|--------|
| **Common** | ~52.2% combined | Bread | ×8 | 15 |
| Common | | Torches | ×16 | 12 |
| Common | | Arrows | ×16 | 12 |
| Common | | Cooked Chicken | ×4 | 11 |
| Common | | Oak Planks | ×32 | 10 |
| **Uncommon** | ~34.8% combined | Iron Ingot | ×4 | 12 |
| Uncommon | | Bronze Token | ×1 | 10 |
| Uncommon | | Gold Ingot | ×2 | 9 |
| Uncommon | | Bow | ×1 | 9 |
| **Rare / Epic** | ~13.0% combined | Diamond | ×2 | 5 |
| Rare / Epic | | Silver Token | ×1 | 4 |
| Rare / Epic | | Emerald | ×3 | 3 |
| Rare / Epic | | Saddle | ×1 | 2 |
| Rare / Epic | | Gold Token | ×1 | 1 |

**Total weight: 115**

> Effective chance per reward = `weight / 100 × 100%`

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
