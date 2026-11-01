package com.aetherium.aetherium.content.entity;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
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
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/** Ambusher that telegraphs a quick rift pounce and attacks only inside its visible claw reach. */
public class RiftStalkerEntity extends MonsterEntity {
    private static final DataParameter<Integer> POUNCE_TICKS =
            EntityDataManager.defineId(RiftStalkerEntity.class, DataSerializers.INT);
    private int pounceCooldown = 60;

    public RiftStalkerEntity(EntityType<? extends RiftStalkerEntity> type, World world) {
        super(type, world);
        this.xpReward = 5;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(POUNCE_TICKS, 0);
    }

    public int getPounceAnimationTicks() {
        return this.entityData.get(POUNCE_TICKS);
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
        if (this.level.isClientSide) return;

        int animation = this.entityData.get(POUNCE_TICKS);
        if (animation > 0) this.entityData.set(POUNCE_TICKS, animation - 1);
        if (this.pounceCooldown > 0) --this.pounceCooldown;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.pounceCooldown > 0) return;

        double distance = this.distanceToSqr(target);
        if (distance >= 16.0D && distance <= 100.0D && this.onGround) {
            Vector3d direction = target.position().subtract(this.position()).normalize();
            this.entityData.set(POUNCE_TICKS, 12);
            this.setDeltaMovement(this.getDeltaMovement().add(direction.x * 0.38D,
                    0.27D, direction.z * 0.38D));
            this.hasImpulse = true;
            if (this.level instanceof ServerWorld) {
                ((ServerWorld)this.level).sendParticles(ParticleTypes.PORTAL,
                        this.getX(), this.getY() + 0.5D, this.getZ(), 12, 0.25D, 0.35D, 0.25D, 0.08D);
                ((ServerWorld)this.level).sendParticles(ParticleTypes.CRIT,
                        this.getX(), this.getY() + 0.7D, this.getZ(), 6, 0.2D, 0.3D, 0.2D, 0.04D);
            }
            this.playSound(RegistryHandler.RIFT_STALKER_POUNCE.get(), 0.72F,
                    0.88F + this.random.nextFloat() * 0.18F);
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
        if (target == null) return false;
        AxisAlignedBB attackBox = this.getBoundingBox().inflate(0.24D, 0.18D, 0.24D);
        if (!attackBox.intersects(target.getBoundingBox())) return false;
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity && this.random.nextFloat() < 0.22F) {
            ((LivingEntity) target).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 40, 0));
        }
        return hit;
    }
}
