package com.example.randomquest.menu;

import com.example.randomquest.QuestData;
import com.example.randomquest.attachment.QuestAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * GachaContainerMenu — slot layout:
 *   0  Input  – accepts any item matching an INCOMPLETE quest requirement (max = remaining needed)
 *   1  Output – take-only; server populates with the rolled reward
 *   2-28  Player main inventory
 *   29-37 Hotbar
 */
public class GachaContainerMenu extends AbstractContainerMenu {

    public static final int INPUT_X  = 163, INPUT_Y  = 36;
    public static final int OUTPUT_X = 209, OUTPUT_Y = 36;
    public static final int INV_X    = 115, INV_Y    = 90;
    public static final int HOTBAR_Y = 148;

    private final SimpleContainer questContainer = new SimpleContainer(2);
    /** Encoded quest string (for slot validation). Kept as-is at menu-open time for server. */
    private final String questEncoded;

    // ── Server-side constructor ────────────────────────────────────────────
    public GachaContainerMenu(int containerId, Inventory playerInv, Player player) {
        super(ModMenus.GACHA_MENU.get(), containerId);
        this.questEncoded = player.getData(QuestAttachments.CURRENT_QUEST);
        addAllSlots(playerInv);
    }

    // ── Client-side constructor ────────────────────────────────────────────
    public GachaContainerMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        super(ModMenus.GACHA_MENU.get(), containerId);
        this.questEncoded = buf.readUtf();
        addAllSlots(playerInv);
    }

    private void addAllSlots(Inventory playerInv) {
        final String enc = questEncoded;

        // Slot 0 – Input: validates against any incomplete requirement
        addSlot(new Slot(questContainer, 0, INPUT_X, INPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                QuestData data = QuestData.parse(enc);
                return data.remainingFor(stack.getItem()) > 0;
            }
            @Override
            public int getMaxStackSize(ItemStack stack) {
                QuestData data = QuestData.parse(enc);
                int rem = data.remainingFor(stack.getItem());
                return rem > 0 ? rem : 1;
            }
        });

        // Slot 1 – Output: take-only
        addSlot(new Slot(questContainer, 1, OUTPUT_X, OUTPUT_Y) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        // Main inventory (slots 2-28)
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9,
                        INV_X + col * 18, INV_Y + row * 18));

        // Hotbar (slots 29-37)
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, INV_X + col * 18, HOTBAR_Y));
    }

    @Override public boolean stillValid(Player player) { return true; }

    public String getQuestEncoded()      { return questEncoded; }
    public void setReward(ItemStack r)   { getSlot(1).set(r); }
    public ItemStack getInputItem()      { return getSlot(0).getItem(); }
    public void clearInput()             { getSlot(0).set(ItemStack.EMPTY); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return result;
        ItemStack item = slot.getItem();
        result = item.copy();
        if (index == 1) {
            if (!moveItemStackTo(item, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else if (index >= 2) {
            if (!moveItemStackTo(item, 0, 1, false)) {
                if (index < 29) { if (!moveItemStackTo(item, 29, 38, false)) return ItemStack.EMPTY; }
                else            { if (!moveItemStackTo(item, 2, 29, false))  return ItemStack.EMPTY; }
            }
        }
        if (item.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (item.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, item);
        return result;
    }
}
