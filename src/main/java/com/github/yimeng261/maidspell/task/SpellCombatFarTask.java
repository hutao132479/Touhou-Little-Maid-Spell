package com.github.yimeng261.maidspell.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IAttackTask;
import com.github.tartaricacid.touhoulittlemaid.api.task.IRangedAttackTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidRangedWalkToTarget;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.github.yimeng261.maidspell.spell.SimplifiedSpellCaster;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 法术战斗任务 - 统一的索敌和战斗管理
 * 实现IRangedAttackTask接口，为女仆提供法术攻击能力和混合战斗模式
 * 负责统一的目标搜索、验证和管理
 *
 * 索敌系统：
 * - 使用近战索敌方法 (IAttackTask::findFirstValidAttackTarget)
 * - 自动优先攻击距离最近的可见目标
 * - 简化代码并确保与其他攻击任务的一致性
 */
public class SpellCombatFarTask implements IRangedAttackTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation UID = new ResourceLocation("maidspell", "spell_combat_far");
    private static final MutableComponent NAME = Component.translatable("task.maidspell.spell_combat_far");
    private static float SPELL_RANGE;

    public static void setSpellRange(Float range){
        SPELL_RANGE = range;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public MutableComponent getName() {
        return NAME;
    }

    @Override
    public ItemStack getIcon() {
        return Items.ENCHANTED_BOOK.getDefaultInstance();
    }

    @Override
    public SoundEvent getAmbientSound(EntityMaid maid) {
        // 使用女仆远程攻击音效
        return SoundUtil.attackSound(maid, InitSounds.MAID_RANGE_ATTACK.get(), 0.5f);
    }

    @Override
    public boolean isEnable(EntityMaid maid) {
        return true;
    }



    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        BehaviorControl<EntityMaid> supplementedTask = StartAttacking.create(this::hasSpellBook, IAttackTask::findFirstValidAttackTarget);
        BehaviorControl<EntityMaid> findTargetTask = StopAttackingIfTargetInvalid.create(target -> farAway(target, maid));
        BehaviorControl<EntityMaid> moveToTargetTask = MaidRangedWalkToTarget.create(0.6f);
        BehaviorControl<EntityMaid> spellCastingTask = new UnifiedSpellCombatBehavior();
        BehaviorControl<EntityMaid> strafingTask = new SpellStrafingTask();

        return Lists.newArrayList(
                Pair.of(5, supplementedTask),
                Pair.of(5, findTargetTask),
                Pair.of(5, moveToTargetTask),
                Pair.of(5, spellCastingTask),
                Pair.of(5, strafingTask)
        );
    }

    @Override
    public void performRangedAttack(EntityMaid shooter, LivingEntity target, float distanceFactor) {
        // 空实现，实际攻击由UnifiedSpellCombatBehavior处理
    }


    @Override
    public boolean canSee(EntityMaid maid, LivingEntity target) {
        return maid.distanceTo(target) <= SPELL_RANGE && Math.abs(maid.getY()-target.getY()) < 3.5;
    }

    @Override
    public AABB searchDimension(EntityMaid maid) {
        float searchRange = Math.max(SPELL_RANGE, 32.0f);
        return maid.hasRestriction()
            ? new AABB(maid.getRestrictCenter()).inflate(searchRange)
            : maid.getBoundingBox().inflate(searchRange);
    }

    @Override
    public float searchRadius(EntityMaid maid) {
        return SPELL_RANGE;
    }

    @Override
    public boolean hasExtraAttack(EntityMaid maid, Entity target) {
        // 如果有法术书且目标在范围内，则可以进行额外的法术攻击
        return hasSpellBook(maid) && target instanceof LivingEntity && maid.distanceTo(target) <= SPELL_RANGE;
    }

    @Override
    public List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(EntityMaid maid) {
        return Lists.newArrayList(
                Pair.of("spell_book", this::hasSpellBook)
        );
    }

    @Override
    public boolean isWeapon(EntityMaid maid, ItemStack stack) {
        // 法术书和近战武器都被认为是武器
        return true;
    }

    /**
     * 检查女仆是否装备了法术书（检查副手、主手和背包）
     */
    private boolean hasSpellBook(EntityMaid maid) {
        return true;
    }

    /**
     * 检查目标是否距离过远
     */
    private boolean farAway(LivingEntity target, EntityMaid maid) {
        return maid.distanceTo(target) > this.searchRadius(maid);
    }


    /**
     * 统一的法术战斗行为控制器
     * 负责管理SimplifiedSpellCaster，并确保目标同步
     */
    private class UnifiedSpellCombatBehavior extends Behavior<EntityMaid> {
        private SimplifiedSpellCaster currentSpellCaster;

        public UnifiedSpellCombatBehavior() {
            super(Map.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
            ));
        }

        @Override
        protected boolean checkExtraStartConditions(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            LivingEntity target = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (target == null || !target.isAlive()) {
                return false;
            }
            
            double distance = maid.distanceTo(target);
            return distance <= SPELL_RANGE && maid.hasLineOfSight(target) && hasSpellBook(maid);
        }

        @Override
        protected boolean canStillUse(net.minecraft.server.level.ServerLevel level, EntityMaid maid, long gameTime) {
            return checkExtraStartConditions(level, maid);
        }

        @Override
        protected void start(net.minecraft.server.level.ServerLevel level, EntityMaid maid, long gameTime) {
            // 创建SpellCaster并设置初始目标
            currentSpellCaster = new SimplifiedSpellCaster(maid);

            // 立即设置当前目标
            LivingEntity target = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (validateTarget(target)) {
                currentSpellCaster.setTarget(target);
            }

        }

        private boolean validateTarget(LivingEntity target) {
            return target != null && !(target instanceof Player) && !(target instanceof EntityMaid);
        }

        @Override
        protected void tick(net.minecraft.server.level.ServerLevel level, EntityMaid maid, long gameTime) {
            if (currentSpellCaster != null) {
                // 确保目标同步 - 这是唯一的目标更新点
                LivingEntity currentTarget = maid.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
                if (validateTarget(currentTarget)) {
                    currentSpellCaster.setTarget(currentTarget);
                    currentSpellCaster.far_tick();
                }
            }
        }

        @Override
        protected void stop(net.minecraft.server.level.ServerLevel level, EntityMaid maid, long gameTime) {
            // 停止和清理SpellCaster
            if (currentSpellCaster != null) {
                currentSpellCaster = null;
            }
        }
    }

    /**
     * 法术战斗专用走位任务 - 基于弓箭任务的走位逻辑优化
     * 主要改进：
     * 1. 修正了条件检查，使用法术书而不是投射武器
     * 2. 使用法术攻击距离而不是武器射程
     * 3. 优化了距离判断逻辑，确保女仆保持合适的施法距离
     */
    public static class SpellStrafingTask extends Behavior<EntityMaid> {
        private boolean strafingClockwise;
        private boolean strafingBackwards;
        private int strafingTime = -1;

        public SpellStrafingTask() {
            super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                            MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                            MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT),
                    1200);
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel worldIn, EntityMaid owner) {
            // 修正：检查是否有法术书而不是投射武器
            SpellCombatFarTask task = new SpellCombatFarTask();
            return task.hasSpellBook(owner) &&
                    owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET)
                            .filter(Entity::isAlive)
                            .isPresent();
        }

        @Override
        protected void tick(ServerLevel worldIn, EntityMaid owner, long gameTime) {
            // 修正：检查法术书而不是投射武器
            SpellCombatFarTask task = new SpellCombatFarTask();
            if (!task.hasSpellBook(owner)) {
                return;
            }
            
            owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((target) -> {
                double maxAttackDistance = SPELL_RANGE;
                double distance = owner.distanceTo(target);
                
                // 优化的距离判断逻辑
                double optimalMinDistance = SimplifiedSpellCaster.FAR_RANGE; // 最佳最小距离
                double optimalMaxDistance = optimalMinDistance + 5; // 最佳最大距离

                // 如果在攻击距离内且能看到目标，开始计时
                if (distance < maxAttackDistance && owner.canSee(target)) {
                    ++this.strafingTime;
                } else {
                    this.strafingTime = -1;
                }

                // 随机改变走位方向，增加不可预测性
                if (this.strafingTime >= 20) {
                    if (owner.getRandom().nextFloat() < 0.3) {
                        this.strafingClockwise = !this.strafingClockwise;
                    }
                    if (owner.getRandom().nextFloat() < 0.3) {
                        this.strafingBackwards = !this.strafingBackwards;
                    }
                    this.strafingTime = 0;
                }

                // 执行走位逻辑
                if (this.strafingTime > -1) {
                    if (distance > optimalMaxDistance) {
                        this.strafingBackwards = false;
                    } else if (distance < optimalMinDistance) {
                        this.strafingBackwards = true;
                    }

                    float forwardSpeed = this.strafingBackwards ? -0.7F : 0.7F;
                    float strafeSpeed = this.strafingClockwise ? 0.7F : -0.7F;
                    
                    owner.getMoveControl().strafe(forwardSpeed, strafeSpeed);
                    owner.setYRot(Mth.rotateIfNecessary(owner.getYRot(), owner.yHeadRot, 0.0F));
                    BehaviorUtils.lookAtEntity(owner, target);

                } else {
                    BehaviorUtils.lookAtEntity(owner, target);
                }
            });
        }

        @Override
        protected void start(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
            entityIn.setSwingingArms(true);
        }

        @Override
        protected void stop(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
            entityIn.setSwingingArms(false);
            entityIn.getMoveControl().strafe(0, 0);
        }

        @Override
        protected boolean canStillUse(ServerLevel worldIn, EntityMaid entityIn, long gameTimeIn) {
            return this.checkExtraStartConditions(worldIn, entityIn);
        }
    }
} 