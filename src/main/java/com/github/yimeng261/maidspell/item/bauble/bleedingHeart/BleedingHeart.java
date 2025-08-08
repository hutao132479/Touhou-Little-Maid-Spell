package com.github.yimeng261.maidspell.item.bauble.bleedingHeart;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 动画法术物品示例
 * 展示如何创建具有多帧贴图的物品
 */
public class BleedingHeart extends Item {
    
    public BleedingHeart() {
        super(new Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC)
        );
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // 发光效果
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("item.maidspell.bleeding_heart.desc1")
            .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.maidspell.bleeding_heart.desc2")
            .withStyle(ChatFormatting.BLUE));
        tooltipComponents.add(Component.translatable("item.maidspell.bleeding_heart.desc3")
            .withStyle(ChatFormatting.YELLOW));
    }
} 