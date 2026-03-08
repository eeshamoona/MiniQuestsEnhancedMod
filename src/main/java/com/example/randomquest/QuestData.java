package com.example.randomquest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable data model for a multi-item quest.
 *
 * Wire format (pipe-delimited, stored in the String Data Attachment and synced):
 *   "minecraft:oak_log/1/0|minecraft:feather/3/1"
 *   ^item key        ^req ^submitted
 *
 * Use {@link #parse} / {@link #encode} to convert to/from String.
 */
public record QuestData(List<QuestItem> items) {

    public record QuestItem(String itemKey, int required, int submitted) {
        public boolean complete()    { return submitted >= required; }
        public int remaining()       { return Math.max(0, required - submitted); }
        public String encode()       { return itemKey + "/" + required + "/" + submitted; }

        public static QuestItem parse(String segment) {
            String[] parts = segment.split("/");
            if (parts.length != 3) throw new IllegalArgumentException("Bad segment: " + segment);
            return new QuestItem(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
    }

    // ── Factory ──────────────────────────────────────────────────────────
    public static QuestData empty() {
        return new QuestData(List.of());
    }

    public static QuestData parse(String encoded) {
        if (encoded == null || encoded.isBlank()) return empty();
        List<QuestItem> items = new ArrayList<>();
        for (String seg : encoded.split("\\|")) {
            if (!seg.isBlank()) items.add(QuestItem.parse(seg.trim()));
        }
        return new QuestData(List.copyOf(items));
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(items.get(i).encode());
        }
        return sb.toString();
    }

    // ── State queries ─────────────────────────────────────────────────────
    public boolean isEmpty()       { return items.isEmpty(); }
    public boolean isComplete()    { return !isEmpty() && items.stream().allMatch(QuestItem::complete); }
    public int completedCount()    { return (int) items.stream().filter(QuestItem::complete).count(); }
    public int totalCount()        { return items.size(); }

    /**
     * Try to submit `count` of `item` against any incomplete requirement.
     * Returns a {@link SubmitResult} with the updated QuestData and how many
     * items were consumed, or {@code null} if the item doesn't match anything.
     */
    public SubmitResult trySubmit(Item item, int count) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        List<QuestItem> updated = new ArrayList<>(items);
        for (int i = 0; i < updated.size(); i++) {
            QuestItem req = updated.get(i);
            if (req.complete()) continue;
            if (!ResourceLocation.parse(req.itemKey()).equals(itemId)) continue;

            int consume = Math.min(count, req.remaining());
            updated.set(i, new QuestItem(req.itemKey(), req.required(), req.submitted() + consume));
            return new SubmitResult(new QuestData(List.copyOf(updated)), consume);
        }
        return null; // no match
    }

    public record SubmitResult(QuestData updated, int consumed) {}

    /**
     * Find the remaining count the input slot should accept for a given item
     * (0 if it doesn't match any incomplete requirement, or the item is already done).
     */
    public int remainingFor(Item item) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        for (QuestItem req : items) {
            if (req.complete()) continue;
            ResourceLocation reqId = ResourceLocation.parse(req.itemKey());
            if (reqId.equals(itemId)) return req.remaining();
        }
        return 0;
    }
}
