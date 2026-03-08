package com.example.miniquestsenhancedmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TokenScreen extends Screen {

    private final String tokenName;

    public TokenScreen(String tokenName) {
        super(Component.literal("Token Screen"));
        this.tokenName = tokenName;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("Close"), btn -> this.onClose())
                .pos(this.width / 2 - 50, this.height / 2 + 30)
                .size(100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        guiGraphics.drawCenteredString(this.font, tokenName + " found!", this.width / 2, this.height / 2 - 20, 0xFFD700);
        guiGraphics.drawCenteredString(this.font, "There will be a game that shows up here in the future.", this.width / 2, this.height / 2, 0xFFFFFF);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
