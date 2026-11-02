package com.aetherium.aetherium.content.boss;

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

/** Three-phase endgame boss that changes from ranged bolts to gravity control and close-range novas. */
public class AstralDevourerEntity extends MonsterEntity {
    private final ServerBossInfo bossInfo = new ServerBossInfo(
            new TranslationTextComponent("entity.aetherium.astral_devourer"),
            BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS);
    private static final DataParameter<Integer> PHASE = EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> ATTACK_TICKS = EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.INT);
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
        this.entityData.define(ATTACK_TICKS, 0);
    }

    public int getAttackAnimationTicks() {
        return this.entityData.get(ATTACK_TICKS);
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
        int animation = this.entityData.get(ATTACK_TICKS);
        if (animation > 0) this.entityData.set(ATTACK_TICKS, animation - 1);

        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
        int nextPhase = this.getHealth() <= this.getMaxHealth() * 0.33F ? 3
                : this.getHealth() <= this.getMaxHealth() * 0.66F ? 2 : 1;
        if (nextPhase != this.phase) {
            this.phase = nextPhase;
            this.entityData.set(PHASE, nextPhase);
            if (phase == 2) {
                this.bossInfo.setColor(BossInfo.Color.PURPLE);
                this.bossInfo.setName(new TranslationTextComponent("entity.aetherium.astral_devourer.phase_two"));
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.28D);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(10.0D);
            } else {
                this.bossInfo.setColor(BossInfo.Color.RED);
                this.bossInfo.setName(new TranslationTextComponent("entity.aetherium.astral_devourer.phase_three"));
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.34D);
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(12.0D);
            }
            this.attackCooldown = 24;
            if (this.level instanceof ServerWorld) {
                ServerWorld server = (ServerWorld) this.level;
                server.sendParticles(ParticleTypes.PORTAL,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 44, 1.45D, 1.0D, 1.45D, 0.25D);
                server.sendParticles(ParticleTypes.END_ROD,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 24, 1.0D, 0.9D, 1.0D, 0.09D);
                if (phase == 3) server.sendParticles(ParticleTypes.CRIT,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 36, 1.5D, 1.0D, 1.5D, 0.2D);
            }
            this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 1.0F, phase == 3 ? 0.62F : 0.82F);
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > 28.0D * 28.0D
                || this.attackCooldown > 0) return;

        double distanceSq = this.distanceToSqr(target);
        if (phase == 1) {
            this.shootBolt(target, 0.0F, 1.0F);
            this.attackCooldown = 82;
        } else if (phase == 2) {
            if (distanceSq > 10.0D * 10.0D) {
                this.shootBolt(target, 0.45F, 0.96F);
                this.attackCooldown = 76;
            } else {
                this.gravityWell();
                this.shootBolt(target, 0.8F, 0.9F);
                this.attackCooldown = 96;
            }
        } else {
            if (distanceSq > 8.5D * 8.5D) {
                this.shootBolt(target, 1.1F, 1.0F);
                this.attackCooldown = 72;
            } else {
                this.cosmicNova();
                this.shootBolt(target, 0.9F, 0.95F);
                this.attackCooldown = 84;
            }
        }
    }

    /** Phase I fires one precise bolt; later phases fan out two or three bolts. */
    private void shootBolt(LivingEntity target, float inaccuracy, float velocity) {
        if (!(this.level instanceof ServerWorld)) return;
        this.entityData.set(ATTACK_TICKS, 16);
        int count = phase == 1 ? 1 : (phase == 2 ? 2 : 3);
        for (int i = 0; i < count; i++) {
            AetherBoltEntity bolt = new AetherBoltEntity(this.level, this);
            bolt.setPos(this.getX(), this.getY() + this.getEyeHeight() * 0.72D, this.getZ());
            double offset = (i - (count - 1) / 2.0D) * 0.9D;
            double dx = target.getX() + offset - this.getX();
            double dy = target.getY(0.45D) - bolt.getY();
            double dz = target.getZ() - this.getZ() - offset * 0.45D;
            bolt.shoot(dx, dy, dz, velocity, inaccuracy);
            this.level.addFreshEntity(bolt);
        }
    }

    private void gravityWell() {
        this.entityData.set(ATTACK_TICKS, 16);
        double radius = 6.0D;
        AxisAlignedBB area = new AxisAlignedBB(this.getX() - radius, this.getY() - 3.0D,
                this.getZ() - radius, this.getX() + radius,
                this.getY() + this.getBbHeight() + 2.0D, this.getZ() + radius);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, area, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            double dx = this.getX() - player.getX();
            double dz = this.getZ() - player.getZ();
            if (dx * dx + dz * dz > radius * radius || Math.abs(player.getY() - this.getY()) > 5.0D) continue;
            double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            player.setDeltaMovement(player.getDeltaMovement().add(dx / length * 0.52D,
                    0.12D, dz / length * 0.52D));
            player.hurtMarked = true;
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 45, 0, true, true));
        }
        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld)this.level;
            for (int i = 0; i < 36; i++) {
                double angle = Math.PI * 2.0D * i / 36.0D;
                server.sendParticles(ParticleTypes.PORTAL,
                        this.getX() + Math.cos(angle) * radius, this.getY() + 0.15D,
                        this.getZ() + Math.sin(angle) * radius, 1, 0.02D, 0.05D, 0.02D, 0.0D);
            }
            server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.8D, this.getZ(),
                    24, 1.8D, 0.7D, 1.8D, 0.12D);
        }
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 0.9F, 0.72F);
    }

    private void cosmicNova() {
        this.entityData.set(ATTACK_TICKS, 18);
        double radius = 5.4D;
        AxisAlignedBB area = new AxisAlignedBB(this.getX() - radius, this.getY() - 2.5D,
                this.getZ() - radius, this.getX() + radius,
                this.getY() + this.getBbHeight() + 1.5D, this.getZ() + radius);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, area, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            if (dx * dx + dz * dz > radius * radius || Math.abs(player.getY() - this.getY()) > 4.0D) continue;
            double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            player.hurt(DamageSource.mobAttack(this), 11.0F);
            player.setDeltaMovement(player.getDeltaMovement().add(
                    dx / length * 1.25D, 0.52D, dz / length * 1.25D));
            player.hurtMarked = true;
            player.addEffect(new EffectInstance(Effects.BLINDNESS, 30, 0, true, true));
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 45, 1, true, true));
        }
        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) this.level;
            for (int i = 0; i < 48; i++) {
                double angle = Math.PI * 2.0D * i / 48.0D;
                server.sendParticles(ParticleTypes.PORTAL,
                        this.getX() + Math.cos(angle) * radius, this.getY() + 0.16D,
                        this.getZ() + Math.sin(angle) * radius, 1, 0.025D, 0.04D, 0.025D, 0.0D);
            }
            server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5D, this.getZ(),
                    34, 2.0D, 0.9D, 2.0D, 0.08D);
            server.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.5D, this.getZ(),
                    20, 1.4D, 0.8D, 1.4D, 0.12D);
        }
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 1.0F, 0.48F);
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
    public boolean doHurtTarget(net.minecraft.entity.Entity target) {
        if (target == null || !this.getBoundingBox().inflate(0.48D, 0.32D, 0.48D).intersects(target.getBoundingBox())) {
            return false;
        }
        return super.doHurtTarget(target);
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
