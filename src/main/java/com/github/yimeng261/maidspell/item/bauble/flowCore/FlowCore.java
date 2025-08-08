package com.github.yimeng261.maidspell.item.bauble.flowCore;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class FlowCore extends Item {
    public FlowCore() {
        super(new Properties()
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

        tooltipComponents.add(Component.translatable("item.maidspell.flow_core.desc1")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.maidspell.flow_core.desc2")
                .withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.translatable("item.maidspell.flow_core.desc3")
                .withStyle(ChatFormatting.YELLOW));
    }
}
