package com.example.randomquest.menu;

import com.example.randomquest.RandomQuestMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {

    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, RandomQuestMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GachaContainerMenu>> GACHA_MENU =
            MENU_TYPES.register("gacha_menu", () ->
                    IMenuTypeExtension.create(GachaContainerMenu::new));

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
