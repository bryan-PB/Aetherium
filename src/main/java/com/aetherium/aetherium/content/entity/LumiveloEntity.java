package com.aetherium.aetherium.content.entity;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

/** A shy, crystal-grazing animal that lures toward raw Aetherium and reveals nearby threats. */
public class LumiveloEntity extends AnimalEntity {
    public LumiveloEntity(EntityType<? extends LumiveloEntity> type, World world) {
        super(type, world);
        this.xpReward = 3;
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, MonsterEntity.class,
                8.0F, 1.25D, 1.45D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.05D,
                Ingredient.of(RegistryHandler.RAW_AETHERIUM.get()), false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomWalkingGoal(this, 0.72D));
        this.goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(5, new LookRandomlyGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        // One bounded local scan every eight seconds, not every game tick.
        if (!this.level.isClientSide && this.tickCount % 160 == 0) {
            List<MonsterEntity> threats = this.level.getEntitiesOfClass(MonsterEntity.class,
                    this.getBoundingBox().inflate(6.0D), threat -> threat.isAlive());
            if (!threats.isEmpty()) {
                for (MonsterEntity threat : threats) {
                    threat.addEffect(new EffectInstance(Effects.GLOWING, 60, 0, true, false));
                }
                this.playSound(SoundEvents.BEACON_ACTIVATE, 0.28F, 1.65F);
            }
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.getItem() == RegistryHandler.RAW_AETHERIUM.get();
    }

    @Override
    public AgeableEntity getBreedOffspring(ServerWorld world, AgeableEntity mate) {
        return RegistryHandler.LUMIVELO.get().create(world);
    }

    @Override
    protected net.minecraft.util.SoundEvent getAmbientSound() {
        return SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getHurtSound(net.minecraft.util.DamageSource source) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.45F;
    }
}
