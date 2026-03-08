package com.example.miniquestsenhancedmod.client;

import net.minecraft.client.Minecraft;

public class TokenScreenHelper {
    public static void openScreen(String tokenName) {
        Minecraft.getInstance().setScreen(new TokenScreen(tokenName));
    }
}
