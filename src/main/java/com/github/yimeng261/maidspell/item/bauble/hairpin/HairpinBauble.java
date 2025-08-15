package com.github.yimeng261.maidspell.item.bauble.hairpin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.yimeng261.maidspell.Global;
import com.github.yimeng261.maidspell.api.IExtendBauble;
import com.github.yimeng261.maidspell.item.MaidSpellItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 发簪 Hairpin 饰品扩展行为
 */
public class HairpinBauble implements IExtendBauble {
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onRemove(EntityMaid maid) {
        // 默认无特殊移除行为
    }

    static {
        Global.bauble_hurtProcessors_pre.put(MaidSpellItems.itemDesc(MaidSpellItems.HAIRPIN),(event,maid)->{
            LivingEntity owner = maid.getOwner();
            DamageSource source = event.getSource();

            if(owner==null){
                return null;
            }

            // 重入保护：如果当前处于回流阶段，不生成新票据，直接让事件继续（由上层控制）
            if (Boolean.TRUE.equals(Global.IN_REDIRECT.get())) {
                return null;
            }

            // 生成一次性票据（TTL：当前 tick + 2，容忍 1~2 tick 抖动）
            long now = owner.level().getGameTime();
            Global.DamageKey key = Global.DamageKey.build(owner, source);
            long expire = now + 2;

            Deque<Global.RedirectTicket> queue = Global.hairpinTickets.computeIfAbsent(owner.getUUID(), k -> new ArrayDeque<>());
            queue.addLast(new Global.RedirectTicket(maid.getUUID(), key, expire));
            Global.hairpinTickets.put(owner.getUUID(), queue);

            //LOGGER.debug("[hairpin] enqueue redirect ticket: owner={}, maid={}, keyTick={} ttl={}", owner.getUUID(), maid.getUUID(), key.tick, expire - now);

            // 将本次伤害转发给玩家，并取消女仆受伤
            owner.hurt(source, event.getAmount());
            event.setCanceled(true);

            return null;
        });
    }
}
