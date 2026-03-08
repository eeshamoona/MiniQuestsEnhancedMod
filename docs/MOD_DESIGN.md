# Eesha's Mini Quests — Mod Design Document

> **Source of truth for all design decisions.**  
> Update this document *first* when changing behaviour, then update code to match.  
> The coding agent should read this before making any changes to the codebase.

---

## Overview

**Eesha's Mini Quests** is a NeoForge mod for Minecraft 1.21.4 that gives players short, randomised delivery quests with instant gacha-style rewards. The core loop is fast and satisfying: receive a quest, collect items, submit them through a vanilla-style trading UI, and immediately claim a randomised reward.

| Property | Value |
|----------|-------|
| Mod ID | `randomquest` |
| Mod Name | Eesha's Mini Quests |
| MC Version | 1.21.4 |
| NeoForge Version | 21.4.156 |
| Java Version | 21 (Eclipse Adoptium) |
| Build System | Gradle 9.2.1 |
| Entry package | `com.example.randomquest` |

---

## Core Gameplay Loop

```
1. /quest start  →  Random quest assigned (1–3 item types)
2. Press G       →  Quest UI opens (Mini Quests screen)
3. Collect items from the world
4. Drag quest item(s) into the INPUT SLOT one at a time
5. Click SUBMIT per item  →  Arrow progress bar fills (furnace style)
6. Final item submitted   →  Reward appears in OUTPUT SLOT
7. Take reward from slot  →  5s countdown begins
8. Countdown hits 0       →  New quest auto-assigned → back to step 2
```

---

## UI Design

The UI is a custom `AbstractContainerScreen` with two panels:

### Left Panel — "The Request"
- Dark background, gold accent header
- **ACTIVE QUEST** label with gold border
- Checklist of all quest requirements, one per row:
  - Item icon (scaled) + item name + `submitted/required` counter
  - Completed items shown with green ✓ and dimmed
- **Progress bar** at the bottom: fills green as items are submitted
- During countdown: shows "Next Quest: Ns" instead of the checklist

### Right Panel — "Mini Quests"
- Light grey (vanilla inventory style)
- **SUBMISSION AREA** label
- `[INPUT SLOT] ——progress arrow——> [OUTPUT SLOT]`
  - Arrow is a horizontal fill bar (green, furnace style), fills `n/total` item types
- **Submit** button — enabled only when input slot has a matching quest item
- **Inventory** — full 3×9 player inventory + hotbar

### Key Panel Dimensions
| Constant | Value | Notes |
|----------|-------|-------|
| `BG_W` | 284px | Total background width |
| `BG_H` | 166px | Total background height |
| `DIVIDER_X` | 108px | Left panel width |
| `INPUT_X/Y` | 133, 36 | Input slot relative to background |
| `OUTPUT_X/Y` | 173, 36 | Output slot relative to background |
| `INV_X/Y` | 115, 90 | Inventory start |
| `HOTBAR_Y` | 148 | Hotbar row Y |

---

## Quest Data Model

Quests are stored as a **pipe-delimited string** in a NeoForge Data Attachment on the player:

```
minecraft:oak_log/1/0|minecraft:feather/3/1
```

Format per segment: `itemKey/required/submitted`

- Parsed/encoded by `QuestData.java`
- Synced to client via `QuestSyncPayload` on every change
- Stored in `ClientQuestCache` client-side

---

## Quest Generation

See [`ITEMS_AND_REWARDS.md`](ITEMS_AND_REWARDS.md) for the full item list and weighting.

| Type | Probability | Qty Range |
|------|-------------|-----------|
| 1 item type | 70% | 1–5 |
| 2 item types | 20% | 1–3 each |
| 3 item types | 10% | 1–2 each |

Items are drawn from a curated 43-item pool of early-game obtainables.

---

## Submission Rules

- **Input slot** only accepts items matching an **incomplete** requirement
- **Max stack** in the slot = exactly the remaining quantity needed (can't over-place)
- **Submit** consumes exactly the needed amount; any excess stays in the slot
- **Progress syncs** to client after every partial submit
- **Reward** triggers only when ALL requirements are complete

---

## Reward System

See [`ITEMS_AND_REWARDS.md`](ITEMS_AND_REWARDS.md) for the full reward table.

Three tiers rolled server-side via `GachaRewards.java`:
- **Common** (~52%): food, basic materials
- **Uncommon** (~35%): iron, gold, bow, bronze tokens
- **Rare / Epic** (~13%): diamonds, emeralds, saddle, silver/gold tokens

---

## Network Architecture

| Payload | Direction | When |
|---------|-----------|------|
| `OpenMenuPayload` | C→S | G key pressed |
| `SubmitQuestPayload` | C→S | Submit button clicked |
| `RequestNextQuestPayload` | C→S | 5s countdown expired |
| `QuestSyncPayload(String)` | S→C | Quest assigned or updated |
| `GachaResultPayload(ItemStack)` | S→C | Quest fully completed |

---

## Key Source Files

| File | Purpose |
|------|---------|
| `RandomQuestMod.java` | Main mod entry, wires all registries |
| `RandomQuestClient.java` | Client entry: keybind (G), countdown tick, screen registration |
| `QuestData.java` | Immutable multi-item quest model (parse, encode, trySubmit) |
| `GachaRewards.java` | Weighted reward pool, `roll()` |
| `QuestCommand.java` | `/quest start` + `assignQuestToPlayer()` |
| `QuestAttachments.java` | NeoForge Data Attachment for current quest string |
| `menu/GachaContainerMenu.java` | AbstractContainerMenu: slot layout, validation |
| `menu/ModMenus.java` | MenuType registration |
| `client/GachaContainerScreen.java` | The rendered UI |
| `client/ClientQuestCache.java` | Client-side quest state + countdown |
| `network/QuestPayloads.java` | All 5 network payload definitions |
| `network/QuestNetworkHandler.java` | Server + client packet handlers |

---

## Known Limitations / Future Work

> Items in this section are **not yet implemented**. Update this list as features are added.

- [ ] Quest quantities as flavour / difficulty levels (e.g., "Easy / Normal / Hard" mode)
- [ ] More quest item types beyond the 43-item early-game pool
- [ ] Enchanted book rewards (requires server-side registry access at roll time)
- [x] Custom Quest Token items as intermediate spin currency — Bronze, Silver, and Gold Tokens
- [ ] Sound effects on submit / reward
- [ ] Quest persistence across server restarts (currently resets on logout)
- [ ] Quest history / stats tracking
- [ ] Multiplayer: per-player quests are fully isolated (already works)

---

## Design Principles

1. **Vanilla-adjacent feel** — UI uses Minecraft's own visual language (slot boxes, grey panels, gold text). No external textures required.
2. **Source of truth flow** — Change this doc → then change the code. Never the other way around without updating docs.
3. **Quick loop** — Quests are intentionally fast (early-game items). Difficulty/complexity comes from the multi-item quests, not from rare drop hunting.
4. **No disk persistence** (intentional) — Quest state intentionally does not survive logout, keeping the mod lightweight and stateless.
