package com.example.randomquest.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.example.randomquest.client.TokenScreenHelper;

public class TokenItem extends Item {
    
    public TokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            TokenScreenHelper.openScreen(stack.getHoverName().getString());
        } else {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        
        return InteractionResult.CONSUME;
    }
}
