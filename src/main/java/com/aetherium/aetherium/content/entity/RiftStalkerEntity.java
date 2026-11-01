package com.aetherium.aetherium.content.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/** A low-frequency ambusher that uses a telegraphed short pounce instead of costly path tricks. */
public class RiftStalkerEntity extends MonsterEntity {
    private int pounceCooldown = 60;

    public RiftStalkerEntity(net.minecraft.entity.EntityType<? extends RiftStalkerEntity> type, World world) {
        super(type, world);
        this.xpReward = 5;
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 26.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ARMOR, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.18D, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 0.78D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 10.0F));
        this.goalSelector.addGoal(4, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            return;
        }
        if (this.pounceCooldown > 0) {
            --this.pounceCooldown;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.pounceCooldown > 0) {
            return;
        }

        double distance = this.distanceToSqr(target);
        // Trigger only at medium range; avoids repeatedly applying velocity at point-blank range.
        if (distance >= 16.0D && distance <= 100.0D && this.onGround) {
            Vector3d direction = target.position().subtract(this.position()).normalize();
            this.setDeltaMovement(this.getDeltaMovement().add(direction.x * 0.38D,
                    0.22D, direction.z * 0.38D));
            this.hasImpulse = true;
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.65F, 0.78F + this.random.nextFloat() * 0.18F);
            this.pounceCooldown = 100;
        }
    }

    @Override
    protected net.minecraft.util.SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getHurtSound(net.minecraft.util.DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity && this.random.nextFloat() < 0.22F) {
            ((LivingEntity) target).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 0));
        }
        return hit;
    }
}
