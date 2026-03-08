package com.example.randomquest.client;

import com.example.randomquest.QuestData;
import com.example.randomquest.menu.GachaContainerMenu;
import com.example.randomquest.network.QuestPayloads;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

/**
 * Gacha Quest ContainerScreen — two-panel layout:
 *   Left (dark)  : "The Request" — ACTIVE QUEST checklist + progress bar
 *   Right (grey) : "Mini Quests" — input slot → furnace arrow → output slot + inventory
 */
public class GachaContainerScreen extends AbstractContainerScreen<GachaContainerMenu> {

    private static final int BG_W      = 284;
    private static final int BG_H      = 166;
    private static final int DIVIDER_X = 108;

    // Colors
    private static final int C_DARK   = 0xFF2D2D2D;
    private static final int C_LIGHT  = 0xFFC6C6C6;
    private static final int C_BORDER = 0xFF555555;
    private static final int C_SLOT   = 0xFF8B8B8B;
    private static final int C_GOLD   = 0xFFFFD700;
    private static final int C_WHITE  = 0xFFFFFFFF;
    private static final int C_GREY   = 0xFFAAAAAA;
    private static final int C_GREEN  = 0xFF55FF55;
    private static final int C_DIM    = 0xFF666666;
    private static final int C_COUNT  = 0xDD111111;

    private Button submitButton;
    private int sparkleTicks = 0;

    public GachaContainerScreen(GachaContainerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth  = BG_W;
        imageHeight = BG_H;
        inventoryLabelY = 9999;
        titleLabelY     = 9999;
    }

    @Override
    protected void init() {
        super.init();
        // Center Submit button under the slot group
        // Right panel center x = DIVIDER_X + (BG_W - DIVIDER_X)/2 = 196
        int bx = leftPos + 196 - 36;  // button half-width 36 (72÷2)
        int by = topPos  + GachaContainerMenu.INPUT_Y + 22;
        submitButton = Button.builder(Component.literal("Submit"), btn -> onSubmitClicked())
                .pos(bx, by).size(72, 16).build();
        addRenderableWidget(submitButton);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g, mx, my, pt);
        super.render(g, mx, my, pt);
        renderTooltip(g, mx, my);
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int lx = leftPos, ty = topPos;

        // ── Left panel ────────────────────────────────────────────────────
        g.fill(lx, ty, lx + DIVIDER_X, ty + BG_H, C_DARK);
        g.fill(lx, ty, lx + DIVIDER_X, ty + 1,         C_BORDER);
        g.fill(lx, ty + BG_H - 1, lx + DIVIDER_X, ty + BG_H, C_BORDER);
        g.fill(lx, ty, lx + 1, ty + BG_H,               C_BORDER);
        g.fill(lx + DIVIDER_X - 1, ty, lx + DIVIDER_X, ty + BG_H, C_BORDER);

        // ── Right panel ───────────────────────────────────────────────────
        g.fill(lx + DIVIDER_X, ty, lx + BG_W, ty + BG_H, C_LIGHT);
        g.fill(lx + DIVIDER_X, ty, lx + BG_W, ty + 1,    C_BORDER);
        g.fill(lx + DIVIDER_X, ty + BG_H - 1, lx + BG_W, ty + BG_H, C_BORDER);
        g.fill(lx + BG_W - 1, ty, lx + BG_W, ty + BG_H,  C_BORDER);

        // ── Submission slots ──────────────────────────────────────────────
        drawSlotBox(g, lx + GachaContainerMenu.INPUT_X,  ty + GachaContainerMenu.INPUT_Y);
        drawSlotBox(g, lx + GachaContainerMenu.OUTPUT_X, ty + GachaContainerMenu.OUTPUT_Y);

        // ── Furnace-style progress arrow (between slots) ─────────────────
        QuestData quest = ClientQuestCache.getCurrentQuestData();
        float progress = quest.isEmpty() ? 0f : (float) quest.completedCount() / quest.totalCount();
        int arrowX = leftPos + GachaContainerMenu.INPUT_X + 20;  // 2px gap from input slot
        int arrowY = topPos  + GachaContainerMenu.INPUT_Y;       // top of slot area
        drawProgressArrow(g, arrowX, arrowY, progress);

        // ── Inventory slots ───────────────────────────────────────────────
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                drawSlotBox(g, lx + GachaContainerMenu.INV_X + col * 18,
                               ty + GachaContainerMenu.INV_Y + row * 18);
        for (int col = 0; col < 9; col++)
            drawSlotBox(g, lx + GachaContainerMenu.INV_X + col * 18,
                           ty + GachaContainerMenu.HOTBAR_Y);

        // ── Labels ────────────────────────────────────────────────────────
        // "SUBMISSION AREA" centered in the right panel
        int subW = font.width("SUBMISSION AREA");
        int panelCx = DIVIDER_X + (BG_W - DIVIDER_X) / 2;
        g.drawString(font, "SUBMISSION AREA", lx + panelCx - subW / 2, ty + 20, 0xFF444444, false);
        g.fill(lx + DIVIDER_X + 5, ty + 80, lx + BG_W - 5, ty + 81, C_BORDER);
        g.drawString(font, "Inventory", lx + DIVIDER_X + 6, ty + 82, 0xFF444444, false);

        // ── Left panel content ────────────────────────────────────────────
        renderLeftPanel(g, lx, ty, quest);

        // ── Submit button state ───────────────────────────────────────────
        updateSubmitButton(quest);

        // ── Sparkles ──────────────────────────────────────────────────────
        if (sparkleTicks > 0) { renderSparkles(g, lx, ty); sparkleTicks--; }

        // ── Countdown overlay ─────────────────────────────────────────────
        if (ClientQuestCache.isCountingDown()) renderCountdown(g, lx, ty);
    }

    // ── Progress arrow (proper Minecraft-style arrowhead) ─────────────────
    /**
     * Draws a right-pointing arrow between the two submission slots.
     * The arrow fills solid green from left-to-right as quest progress increases.
     * Bounding box: 24px wide × 16px tall.
     */
    private void drawProgressArrow(GuiGraphics g, int ax, int ay, float progress) {
        // ax/ay = top-left of arrow bounding box (already computed in renderBg caller)
        int dark  = 0xFF555555;
        int green = 0xFF4ADA4A;

        // Draw background (dark) then overlay green up to fillW columns
        arrowShape(g, ax, ay, dark, 24);
        if (progress > 0f) {
            arrowShape(g, ax, ay, green, Math.round(progress * 24));
        }
    }

    /**
     * Draws the arrow shape clipped to [ax, ax+maxW).
     * Shape: shaft rows 5-10, then widening arrowhead cols 12-24.
     */
    private void arrowShape(GuiGraphics g, int x, int y, int c, int maxW) {
        // Shaft (rows 5-10, cols 0-12)
        g.fill(x, y + 5, Math.min(x + 12, x + maxW), y + 11, c);

        // Wing A — top & bottom (rows 2-4 and 11-13, cols 12-15)
        if (maxW > 12) {
            int ex = Math.min(x + maxW, x + 15);
            g.fill(x + 12, y + 2, ex, y + 5,  c);
            g.fill(x + 12, y + 11, ex, y + 14, c);
        }
        // Wing B — outer top & bottom (rows 0-1 and 14-15, cols 14-17)
        if (maxW > 14) {
            int ex = Math.min(x + maxW, x + 17);
            g.fill(x + 14, y,      ex, y + 2,  c);
            g.fill(x + 14, y + 14, ex, y + 16, c);
        }
        // Tip — triangle drawn column-by-column for smooth point (cols 14-24)
        if (maxW > 14) {
            for (int col = 14; col < Math.min(24, maxW); col++) {
                // Half-height decreases linearly from 8→1 as col goes 14→24
                int halfH = 8 - (int) Math.ceil((col - 14) * 8.0 / 10.0);
                if (halfH < 1) halfH = 1;
                g.fill(x + col, y + 8 - halfH, x + col + 1, y + 8 + halfH, c);
            }
        }
    }


    // ── Left panel ────────────────────────────────────────────────────────
    private void renderLeftPanel(GuiGraphics g, int lx, int ty, QuestData quest) {
        g.drawString(font, "The Request", lx + 8, ty + 7, C_GOLD, true);
        g.fill(lx + 5, ty + 17, lx + DIVIDER_X - 5, ty + 18, 0xFF555555);

        if (ClientQuestCache.isCountingDown()) {
            int remaining = Math.max(1, (ClientQuestCache.getCountdownTicks() + 19) / 20);
            String line1 = "Next Quest:";
            String line2 = remaining + "s";
            g.drawString(font, line1, lx + (DIVIDER_X - font.width(line1)) / 2, ty + 52, C_GREY, false);
            g.drawString(font, line2, lx + (DIVIDER_X - font.width(line2)) / 2, ty + 64, C_GOLD, true);
            return;
        }

        if (quest.isEmpty()) {
            String msg = "No quest.";
            String hint = "/quest start";
            g.drawString(font, msg,  lx + (DIVIDER_X - font.width(msg)) / 2,  ty + 52, C_GREY, false);
            g.drawString(font, hint, lx + (DIVIDER_X - font.width(hint)) / 2, ty + 64, C_GOLD, false);
            return;
        }

        // "ACTIVE QUEST:" header
        g.fill(lx + 5, ty + 22, lx + DIVIDER_X - 5, ty + 33, 0xFF5C4A1E);
        g.fill(lx + 5, ty + 22, lx + DIVIDER_X - 5, ty + 23, C_GOLD);
        g.fill(lx + 5, ty + 32, lx + DIVIDER_X - 5, ty + 33, C_GOLD);
        g.drawString(font, "ACTIVE QUEST:", lx + 9, ty + 24, C_GOLD, false);

        // Checklist — one row per requirement
        int rowY = ty + 37;
        for (QuestData.QuestItem req : quest.items()) {
            boolean done = req.complete();
            int textColor = done ? C_DIM : C_WHITE;

            // Item icon (8×8 via scale trick — just use renderItem at 16×16, clip with scissor... or shift)
            Optional<Item> itemOpt = BuiltInRegistries.ITEM
                    .getOptional(ResourceLocation.parse(req.itemKey()));
            if (itemOpt.isPresent()) {
                // Render 16×16 icon scaled – use pose matrix to draw at half size at offset
                g.pose().pushPose();
                g.pose().scale(0.75f, 0.75f, 1f);
                int iconX = (int)((lx + 8) / 0.75f);
                int iconY = (int)(rowY / 0.75f);
                g.renderItem(new ItemStack(itemOpt.get()), iconX, iconY);
                g.pose().popPose();
            }

            // Item name + count
            String label = itemOpt.map(i -> i.getDefaultInstance().getHoverName().getString())
                    .orElse(req.itemKey());
            // Truncate if too long for the panel
            String countStr = done ? "✓" : req.submitted() + "/" + req.required();
            String shortLabel = truncate(label, DIVIDER_X - 36);
            g.drawString(font, shortLabel, lx + 22, rowY + 2, textColor, false);
            g.drawString(font, countStr,   lx + 22, rowY + 11, done ? C_GREEN : C_GREY, false);

            rowY += 22;
        }

        // Progress bar at bottom
        int barY = ty + BG_H - 22;
        int total = quest.totalCount(), done = quest.completedCount();
        String progLabel = "Progress: " + done + " / " + total;
        g.drawString(font, progLabel, lx + 5, barY - 10, C_GOLD, false);

        int barW = DIVIDER_X - 12;
        int filled = total > 0 ? (int)((float) done / total * barW) : 0;
        g.fill(lx + 5, barY, lx + 5 + barW, barY + 6, 0xFF333333);
        if (filled > 0) g.fill(lx + 5, barY, lx + 5 + filled, barY + 6, C_GREEN);
        g.fill(lx + 5, barY, lx + 5 + barW, barY + 1,      0xFF222222);
        g.fill(lx + 5, barY + 5, lx + 5 + barW, barY + 6,  C_DIM);
    }

    private String truncate(String s, int maxWidth) {
        if (font.width(s) <= maxWidth) return s;
        while (s.length() > 1 && font.width(s + "…") > maxWidth) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    // ── Sparkles ──────────────────────────────────────────────────────────
    private void renderSparkles(GuiGraphics g, int lx, int ty) {
        int ox = lx + GachaContainerMenu.OUTPUT_X;
        int oy = ty + GachaContainerMenu.OUTPUT_Y;
        java.util.Random rng = new java.util.Random(sparkleTicks * 1337L);
        for (int i = 0; i < 10; i++) {
            int sx = ox + rng.nextInt(36) - 10;
            int sy = oy + rng.nextInt(28) - 6;
            int alpha = Math.min(255, sparkleTicks * 8) << 24;
            g.fill(sx, sy, sx + 2, sy + 2, alpha | 0x00FF88);
        }
    }

    // ── Countdown overlay ─────────────────────────────────────────────────
    private void renderCountdown(GuiGraphics g, int lx, int ty) {
        int remaining = Math.max(1, (ClientQuestCache.getCountdownTicks() + 19) / 20);
        String text = "Next quest in: " + remaining;
        int tw = font.width(text);
        int cx = lx + DIVIDER_X + (BG_W - DIVIDER_X) / 2;
        int cy = ty + 56;
        g.fill(cx - tw/2 - 4, cy - 3, cx + tw/2 + 4, cy + font.lineHeight + 3, C_COUNT);
        g.drawString(font, text, cx - tw/2, cy, C_GOLD, true);
    }

    // ── Slot box ──────────────────────────────────────────────────────────
    private void drawSlotBox(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y + 17, C_SLOT);
        g.fill(x - 1, y - 1, x + 17, y,       0xFF373737);
        g.fill(x - 1, y - 1, x,      y + 17,  0xFF373737);
        g.fill(x - 1, y + 17, x + 17, y + 18, 0xFFFFFFFF);
        g.fill(x + 17, y - 1, x + 18, y + 18, 0xFFFFFFFF);
    }

    private void updateSubmitButton(QuestData quest) {
        if (submitButton == null) return;
        submitButton.active = !quest.isEmpty()
                && !menu.getSlot(0).getItem().isEmpty()
                && !ClientQuestCache.isCountingDown();
    }

    private void onSubmitClicked() {
        PacketDistributor.sendToServer(new QuestPayloads.SubmitQuestPayload());
        if (submitButton != null) submitButton.active = false;
    }

    public void onRewardReceived(ItemStack reward) { sparkleTicks = 40; }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        String title = "Mini Quests";
        int tw = font.width(title);
        int panelMid = DIVIDER_X + (BG_W - DIVIDER_X) / 2;
        g.drawString(font, title, panelMid - tw / 2, 7, C_WHITE, true);
    }

    @Override public boolean isPauseScreen() { return false; }
}
