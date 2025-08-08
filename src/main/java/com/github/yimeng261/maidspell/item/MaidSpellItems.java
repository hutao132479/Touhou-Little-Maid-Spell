package com.github.yimeng261.maidspell.item;

import com.github.yimeng261.maidspell.item.bauble.bleedingHeart.BleedingHeart;
import com.github.yimeng261.maidspell.item.bauble.flowCore.FlowCore;
import com.github.yimeng261.maidspell.item.bauble.quickChantRing.QuickChantRing;
import com.github.yimeng261.maidspell.item.bauble.spellCore.SpellEnhancementCore;
import com.github.yimeng261.maidspell.item.bauble.springRing.SpringRing;
import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import static com.github.yimeng261.maidspell.MaidSpellMod.MOD_ID;

/**
 * 女仆法术饰品物品注册
 * 只有在铁魔法模组加载时才会注册这些物品
 */
public class MaidSpellItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredItem<Item> SPELL_ENHANCEMENT_CORE = ITEMS.register("spell_enhancement_core", SpellEnhancementCore::new);
    public static final DeferredItem<Item> BLEEDING_HEART = ITEMS.register("bleeding_heart", BleedingHeart::new);
    public static final DeferredItem<Item> FLOW_CORE = ITEMS.register("flow_core", FlowCore::new);
    public static final DeferredItem<Item> QUICK_CHANT_RING = ITEMS.register("quick_chant_ring", QuickChantRing::new);
    public static final DeferredItem<Item> SPRING_RING = ITEMS.register("spring_ring", SpringRing::new);

    /**
     * 注册物品
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static String itemDesc(DeferredItem<Item> item) {
        return Util.makeDescriptionId("item", item.getId());
    }

} 