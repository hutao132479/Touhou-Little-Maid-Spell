package com.github.yimeng261.maidspell.item.bauble.springRing;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SpringRing extends Item {
    public SpringRing() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
        );
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("item.maidspell.spring_rings.desc1")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.maidspell.spring_ring.desc2")
                .withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.translatable("item.maidspell.spring_ring.desc3")
                .withStyle(ChatFormatting.YELLOW));
    }
}
