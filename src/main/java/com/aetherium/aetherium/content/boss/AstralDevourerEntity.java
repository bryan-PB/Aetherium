package com.aetherium.aetherium.content.boss;

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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

/** Three-phase endgame boss with homing-style bolts, gravity pulls and a bounded nova attack. */
public class AstralDevourerEntity extends MonsterEntity {
    private final ServerBossInfo bossInfo = new ServerBossInfo(
            new TranslationTextComponent("entity.aetherium.astral_devourer"),
            BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS);
    private static final DataParameter<Integer> PHASE = EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.INT);
    private int phase = 1;
    private int attackCooldown = 70;

    public AstralDevourerEntity(EntityType<? extends AstralDevourerEntity> type, World world) {
        super(type, world);
        this.xpReward = 100;
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 1);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 420.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.95D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 0.45D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 20.0F));
        this.goalSelector.addGoal(4, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) return;

        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
        int nextPhase = this.getHealth() <= this.getMaxHealth() * 0.33F ? 3
                : this.getHealth() <= this.getMaxHealth() * 0.66F ? 2 : 1;
        if (nextPhase != this.phase) {
            this.phase = nextPhase;
            this.entityData.set(PHASE, nextPhase);
            if (phase == 2) {
                this.bossInfo.setColor(BossInfo.Color.PURPLE);
                this.bossInfo.setName(new TranslationTextComponent("entity.aetherium.astral_devourer.phase_two"));
            } else {
                this.bossInfo.setColor(BossInfo.Color.RED);
                this.bossInfo.setName(new TranslationTextComponent("entity.aetherium.astral_devourer.phase_three"));
            }
            if (this.level instanceof ServerWorld) {
                ((ServerWorld) this.level).sendParticles(ParticleTypes.PORTAL,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 26, 1.2D, 0.8D, 1.2D, 0.2D);
            }
            this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 0.95F, phase == 3 ? 0.72F : 0.9F);
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > 26.0D * 26.0D
                || this.attackCooldown > 0) return;

        if (phase == 1) {
            this.shootBolt(target, 0.0F, 1.0F);
            this.attackCooldown = 88;
        } else if (phase == 2) {
            this.gravityWell();
            this.shootBolt(target, 0.35F, 0.92F);
            this.attackCooldown = 98;
        } else {
            this.cosmicNova();
            this.shootBolt(target, 0.85F, 0.95F);
            this.attackCooldown = 82;
        }
    }

    private void shootBolt(LivingEntity target, float inaccuracy, float velocity) {
        if (!(this.level instanceof ServerWorld)) return;
        AetherBoltEntity bolt = new AetherBoltEntity(this.level, this);
        bolt.setPos(this.getX(), this.getY() + this.getEyeHeight() * 0.72D, this.getZ());
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.45D) - bolt.getY();
        double dz = target.getZ() - this.getZ();
        bolt.shoot(dx, dy, dz, velocity, inaccuracy);
        this.level.addFreshEntity(bolt);
        if (phase == 3 && this.random.nextInt(3) == 0) {
            AetherBoltEntity second = new AetherBoltEntity(this.level, this);
            second.setPos(bolt.getX(), bolt.getY(), bolt.getZ());
            second.shoot(dx, dy + 0.10D, dz, velocity, 2.0F);
            this.level.addFreshEntity(second);
        }
    }

    private void gravityWell() {
        AxisAlignedBB area = this.getBoundingBox().inflate(8.0D, 4.0D, 8.0D);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, area, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            Vector3d pull = this.position().subtract(player.position());
            double length = Math.max(0.001D, pull.length());
            player.setDeltaMovement(player.getDeltaMovement().add(
                    pull.x / length * 0.52D, 0.12D, pull.z / length * 0.52D));
            player.hurtMarked = true;
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 45, 0, true, true));
        }
        if (this.level instanceof ServerWorld) {
            ((ServerWorld) this.level).sendParticles(ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 0.8D, this.getZ(), 20, 2.2D, 0.7D, 2.2D, 0.12D);
        }
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 0.85F, 0.55F);
    }

    private void cosmicNova() {
        double radius = 6.5D;
        AxisAlignedBB area = this.getBoundingBox().inflate(radius, 3.0D, radius);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, area, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            Vector3d away = player.position().subtract(this.position());
            double length = Math.max(0.001D, away.length());
            player.hurt(DamageSource.mobAttack(this), 10.0F);
            player.setDeltaMovement(player.getDeltaMovement().add(
                    away.x / length * 1.15D, 0.48D, away.z / length * 1.15D));
            player.hurtMarked = true;
            player.addEffect(new EffectInstance(Effects.BLINDNESS, 25, 0, true, true));
        }
        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) this.level;
            server.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.7D, this.getZ(),
                    42, 2.8D, 1.0D, 2.8D, 0.22D);
            server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5D, this.getZ(),
                    12, 1.8D, 0.4D, 1.8D, 0.04D);
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 0.48F);
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
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }
}
