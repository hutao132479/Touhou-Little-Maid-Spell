package com.github.yimeng261.maidspell;

import com.github.yimeng261.maidspell.task.SpellCombatTask;
import com.github.yimeng261.maidspell.event.MaidSpellEventHandler;
import com.github.yimeng261.maidspell.item.MaidSpellItems;
import com.github.yimeng261.maidspell.item.MaidSpellCreativeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MaidSpellMod.MOD_ID)
public class MaidSpellMod {
    public static final String MOD_ID = "touhou_little_maid_spell";
    public static final String MOD_NAME = "Touhou Little Maid: Spell";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final SpellCombatTask SPELL_COMBAT_TASK = new SpellCombatTask();
    

    public MaidSpellMod(IEventBus modEventBus) {
        
        var forgeBus = NeoForge.EVENT_BUS;
        
        modEventBus.addListener(this::setup);
        
        // 手动注册事件处理器，确保事件能被正确监听
        forgeBus.register(MaidSpellEventHandler.class);

        // 注册物品（只在铁魔法模组存在时）
        MaidSpellItems.register(modEventBus);
        
        // 注册创造模式物品栏（始终注册）
        MaidSpellCreativeTab.register(modEventBus);
        
        // Register the config through ModContainer
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (checkDependencies()) {
                LOGGER.info("Dependencies verified - initialization complete");
            }
        });
    }
    
    private boolean checkDependencies() {
        boolean hasTouhouMaid = checkModLoaded("touhou_little_maid");
        if (!hasTouhouMaid) {
            return false;
        }
        return true;
    }
    
    private boolean checkModLoaded(String modId) {
        return net.neoforged.fml.ModList.get().isLoaded(modId);
    }
    

}