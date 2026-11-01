package com.aetherium.aetherium.content.boss;

import com.aetherium.aetherium.content.entity.RiftStalkerEntity;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.world.BossInfo;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

import java.util.List;

/** Two-phase arena boss: telegraphed shockwaves, then heavier slams and one optional minion. */
public class RiftGuardianEntity extends MonsterEntity {
    private final ServerBossInfo bossInfo = new ServerBossInfo(
            new TranslationTextComponent("entity.aetherium.rift_guardian"),
            BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS);
    private int attackCooldown = 55;
    private static final DataParameter<Integer> PHASE = EntityDataManager.defineId(RiftGuardianEntity.class, DataSerializers.INT);
    private int phase = 1;
    private int minionCooldown = 0;

    public RiftGuardianEntity(EntityType<? extends RiftGuardianEntity> type, World world) {
        super(type, world);
        this.xpReward = 55;
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 1);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 260.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.05D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 0.62D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 16.0F));
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

        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
        int nextPhase = this.getHealth() <= this.getMaxHealth() * 0.5F ? 2 : 1;
        if (nextPhase != this.phase) {
            this.phase = nextPhase;
            this.entityData.set(PHASE, nextPhase);
            this.bossInfo.setColor(BossInfo.Color.RED);
            this.bossInfo.setName(new TranslationTextComponent("entity.aetherium.rift_guardian.phase_two"));
            if (this.level instanceof ServerWorld) {
                ((ServerWorld) this.level).sendParticles(ParticleTypes.EXPLOSION,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                ((ServerWorld) this.level).sendParticles(ParticleTypes.CRIT,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 18, 1.0D, 0.5D, 1.0D, 0.08D);
            }
            this.playSound(SoundEvents.RAVAGER_ROAR, 0.9F, 0.7F);
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.minionCooldown > 0) this.minionCooldown--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.attackCooldown <= 0
                && this.distanceToSqr(target) <= 18.0D * 18.0D) {
            this.groundSlam(phase == 1 ? 4.4D : 5.7D,
                    phase == 1 ? 8.0F : 11.0F,
                    phase == 1 ? 0.8D : 1.15D);
            this.attackCooldown = phase == 1 ? 105 : 82;
            if (phase == 2 && this.minionCooldown <= 0 && this.random.nextInt(3) == 0) {
                this.trySpawnMinion();
                this.minionCooldown = 240;
            }
        }
    }

    private void groundSlam(double radius, float damage, double knockback) {
        AxisAlignedBB area = this.getBoundingBox().inflate(radius, 2.0D, radius);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, area, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            Vector3d push = player.position().subtract(this.position());
            double length = Math.max(0.001D, push.length());
            player.hurt(DamageSource.mobAttack(this), damage);
            player.setDeltaMovement(player.getDeltaMovement().add(
                    push.x / length * knockback, 0.32D, push.z / length * knockback));
            player.hurtMarked = true;
            if (phase == 2) player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 45, 0, true, true));
        }

        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) this.level;
            server.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.15D, this.getZ(),
                    phase == 1 ? 16 : 24, radius * 0.42D, 0.08D, radius * 0.42D, 0.035D);
            server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.4D, this.getZ(),
                    phase == 1 ? 5 : 9, radius * 0.25D, 0.18D, radius * 0.25D, 0.015D);
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE, 0.75F, phase == 1 ? 0.8F : 0.62F);
    }

    private void trySpawnMinion() {
        if (!(this.level instanceof ServerWorld)) return;
        AxisAlignedBB area = this.getBoundingBox().inflate(14.0D);
        List<RiftStalkerEntity> existing = this.level.getEntitiesOfClass(RiftStalkerEntity.class, area,
                RiftStalkerEntity::isAlive);
        if (existing.size() >= 1) return;

        RiftStalkerEntity minion = RegistryHandler.RIFT_STALKER.get().create(this.level);
        if (minion != null) {
            minion.moveTo(this.getX() + 2.0D, this.getY(), this.getZ() + 1.0D,
                    this.yRot, 0.0F);
            this.level.addFreshEntity(minion);
        }
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    @Override
    public void startSeenByPlayer(ServerPlayerEntity player) {
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayerEntity player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.bossInfo.removeAllPlayers();
    }

    @Override
    protected net.minecraft.util.SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }
}
