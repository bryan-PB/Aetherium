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
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
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
import net.minecraft.util.math.BlockPos;
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
    private static final DataParameter<Integer> ATTACK_STATE = EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.INT);
    private int phase = 1;
    private int attackCooldown = 70;
    private int attackCycle = 0;
    private int beamChargeTicks = 0;
    private int beamTicks = 0;
    private double beamEndX;
    private double beamEndY;
    private double beamEndZ;
    private int dashChargeTicks = 0;
    private int dashTicks = 0;
    private int dashRecoveryTicks = 0;
    private int dashesRemaining = 0;
    private boolean dashHitRegistered = false;
    private boolean dashChainHitRegistered = false;
    private double dashDirX;
    private double dashDirY;
    private double dashDirZ;
    private LivingEntity dashTarget;
    private BlockPos waterExitTarget;
    private int waterSearchCooldown = 0;
    private boolean gravityDamagePending = false;
    private boolean novaDamagePending = false;

    public AstralDevourerEntity(EntityType<? extends AstralDevourerEntity> type, World world) {
        super(type, world);
        this.xpReward = 100;
        this.setPersistenceRequired();
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 1);
        this.entityData.define(ATTACK_TICKS, 0);
        this.entityData.define(ATTACK_STATE, 0);
    }

    public int getAttackAnimationTicks() {
        return this.entityData.get(ATTACK_TICKS);
    }

    public int getAttackState() {
        return this.entityData.get(ATTACK_STATE);
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
        // Flight and combat spacing are controlled in tick(); ground navigation would pull it underground.
        this.goalSelector.addGoal(1, new LookAtGoal(this, PlayerEntity.class, 28.0F));
        this.goalSelector.addGoal(2, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (this.level.isClientSide) return;
        if (this.phase >= 2 && this.entityData.get(ATTACK_STATE) == 0 && this.tickCount % 6 == 0) {
            this.sendPhaseAuraParticles();
        }

        LivingEntity target = this.getTarget();
        boolean phaseTransitionActive = this.entityData.get(ATTACK_STATE) == 9;
        boolean dashActive = phaseTransitionActive ? false : this.tickDashPattern();
        if (!phaseTransitionActive && !dashActive) this.updateHoverMovement(target);
        if (this.isInWater() && !phaseTransitionActive) {
            Vector3d motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * 0.92D, Math.max(motion.y, 0.20D), motion.z * 0.92D);
            this.hasImpulse = true;
        }

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
            this.attackCooldown = 0;
            this.dashChargeTicks = 0;
            this.dashTicks = 0;
            this.dashRecoveryTicks = 0;
            this.dashesRemaining = 0;
            this.dashTarget = null;
            this.beamChargeTicks = 0;
            this.beamTicks = 0;
            this.gravityDamagePending = false;
            this.novaDamagePending = false;
            this.entityData.set(ATTACK_STATE, 9);
            this.entityData.set(ATTACK_TICKS, 54);
            this.setDeltaMovement(Vector3d.ZERO);
            this.hasImpulse = true;
            if (this.level instanceof ServerWorld) {
                ServerWorld server = (ServerWorld) this.level;
                server.sendParticles(ParticleTypes.PORTAL,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 36, 1.35D, 0.9D, 1.35D, 0.18D);
                server.sendParticles(ParticleTypes.END_ROD,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 28, 1.0D, 0.9D, 1.0D, 0.09D);
                if (phase == 3) server.sendParticles(ParticleTypes.CRIT,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 32, 1.5D, 1.0D, 1.5D, 0.16D);
            }
            this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 1.0F, phase == 3 ? 0.62F : 0.82F);
        }

        if (this.entityData.get(ATTACK_STATE) == 9) {
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().scale(0.18D));
            this.hasImpulse = true;
            int remaining = this.entityData.get(ATTACK_TICKS);
            if (remaining > 0 && remaining % 9 == 0) this.sendPhaseTransitionPulse(remaining);
            this.tickAttackAnimation();
            return;
        }

        if (dashActive) return;
        if (this.tickAstralBeam()) return;
        this.tickAttackAnimation();

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > 28.0D * 28.0D
                || this.attackCooldown > 0) return;

        double distanceSq = this.distanceToSqr(target);
        this.attackCycle++;
        if (distanceSq > 4.5D * 4.5D && distanceSq < 18.0D * 18.0D && this.attackCycle % 3 == 0) {
            this.startDashPattern(target);
            this.attackCooldown = phase >= 3 ? 118 : 132;
            return;
        }
        if (phase >= 2 && distanceSq > 8.0D * 8.0D && this.attackCycle % 2 == 0) {
            this.prepareAstralRay(target);
            this.attackCooldown = 136;
            return;
        }
        if (phase == 1) {
            // The first phase teaches players to dodge a precise lance, then punishes close pursuit.
            if (distanceSq > 6.5D * 6.5D) {
                this.shootBolt(target, 0.0F, 1.05F);
                this.attackCooldown = 78;
            } else {
                this.gravityWell();
                this.attackCooldown = 102;
            }
        } else if (phase == 2) {
            // The awakened phase mixes double volleys with a pull-in when players get near.
            if (distanceSq > 9.0D * 9.0D) {
                this.shootBolt(target, 0.35F, 1.0F);
                this.attackCooldown = 72;
            } else if (this.attackCycle % 2 == 0) {
                this.gravityWell();
                this.attackCooldown = 102;
            } else {
                this.shootBolt(target, 0.75F, 0.94F);
                this.attackCooldown = 82;
            }
        } else {
            // Final phase closes distance quickly and explodes if the player stays underneath it.
            if (distanceSq > 7.5D * 7.5D) {
                this.shootBolt(target, 0.9F, 1.08F);
                this.attackCooldown = 66;
            } else if (this.attackCycle % 2 == 0) {
                this.cosmicNova();
                this.attackCooldown = 94;
            } else {
                this.shootBolt(target, 0.85F, 0.98F);
                this.attackCooldown = 72;
            }
        }
    }

    /** Keeps the Devourer airborne and circling its target instead of pathfinding on the ground. */
    private void updateHoverMovement(LivingEntity target) {
        this.getNavigation().stop();
        Vector3d velocity = this.getDeltaMovement();
        double desiredX = 0.0D;
        double desiredZ = 0.0D;
        double desiredY = this.isInWater() ? 0.24D : Math.sin(this.tickCount * 0.075D) * 0.012D;

        if (target != null && target.isAlive()) {
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double horizontalDistance = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            double hoverY = target.getY() + 0.45D;
            desiredY = Math.max(this.isInWater() ? 0.20D : -0.14D,
                    Math.min(this.isInWater() ? 0.30D : 0.14D, (hoverY - this.getY()) * 0.075D));

            double preferredDistance = this.phase == 1 ? 10.5D : (this.phase == 2 ? 8.5D : 7.0D);
            if (horizontalDistance > preferredDistance + 1.0D) {
                desiredX = dx / horizontalDistance * 0.105D;
                desiredZ = dz / horizontalDistance * 0.105D;
            } else if (horizontalDistance < preferredDistance - 1.0D) {
                desiredX = -dx / horizontalDistance * 0.075D;
                desiredZ = -dz / horizontalDistance * 0.075D;
            } else {
                // A slow orbit makes the ranged phases harder to kite without teleporting.
                desiredX = -dz / horizontalDistance * 0.045D;
                desiredZ = dx / horizontalDistance * 0.045D;
            }
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        if (this.isInWater()) {
            if (this.waterSearchCooldown > 0) this.waterSearchCooldown--;
            if (this.waterSearchCooldown <= 0) {
                this.waterExitTarget = this.findNearestDryPosition();
                this.waterSearchCooldown = 20;
            }
            if (this.waterExitTarget != null) {
                double shoreX = this.waterExitTarget.getX() + 0.5D - this.getX();
                double shoreZ = this.waterExitTarget.getZ() + 0.5D - this.getZ();
                double shoreLength = Math.max(0.001D, Math.sqrt(shoreX * shoreX + shoreZ * shoreZ));
                desiredX = shoreX / shoreLength * 0.15D;
                desiredZ = shoreZ / shoreLength * 0.15D;
                desiredY = Math.max(desiredY, 0.22D);
            }
        } else {
            this.waterExitTarget = null;
            this.waterSearchCooldown = 0;
        }

        this.setDeltaMovement(
                velocity.x * 0.70D + desiredX * 0.30D,
                velocity.y * 0.66D + desiredY * 0.34D,
                velocity.z * 0.70D + desiredZ * 0.30D);
    }

    /** Finds a nearby dry two-block-high landing position when the flying boss is submerged. */
    private BlockPos findNearestDryPosition() {
        BlockPos origin = this.blockPosition();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        for (int dx = -12; dx <= 12; dx += 2) {
            for (int dz = -12; dz <= 12; dz += 2) {
                for (int dy = 8; dy >= -12; dy--) {
                    BlockPos ground = origin.offset(dx, dy, dz);
                    if (!this.level.getBlockState(ground).getMaterial().isSolid()
                            || !this.level.getFluidState(ground).isEmpty()) continue;
                    BlockPos feet = ground.offset(0, 1, 0);
                    BlockPos head = ground.offset(0, 2, 0);
                    if (!this.level.getBlockState(feet).isAir() || !this.level.getFluidState(feet).isEmpty()
                            || !this.level.getBlockState(head).isAir() || !this.level.getFluidState(head).isEmpty()) continue;
                    double score = dx * dx + dz * dz + dy * dy * 0.35D;
                    if (score < bestScore) {
                        bestScore = score;
                        best = feet;
                    }
                }
            }
        }
        return best;
    }

    /** Fires an animated volley of bright astral projectiles. */
    private void shootBolt(LivingEntity target, float inaccuracy, float velocity) {
        if (!(this.level instanceof ServerWorld)) return;
        this.entityData.set(ATTACK_STATE, 1);
        this.entityData.set(ATTACK_TICKS, 16);
        int count = phase == 1 ? 3 : (phase == 2 ? 4 : 5);
        for (int i = 0; i < count; i++) {
            AetherBoltEntity bolt = new AetherBoltEntity(this.level, this);
            bolt.setPos(this.getX(), this.getY() + this.getEyeHeight() * 0.72D, this.getZ());
            double offset = (i - (count - 1) / 2.0D) * (phase == 1 ? 0.85D : 0.95D);
            double dx = target.getX() + offset - this.getX();
            double dy = target.getY(0.45D) - bolt.getY();
            double dz = target.getZ() - this.getZ() - offset * 0.45D;
            bolt.shoot(dx, dy, dz, velocity, inaccuracy);
            this.level.addFreshEntity(bolt);
        }
        ServerWorld server = (ServerWorld) this.level;
        server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + this.getBbHeight() * 0.58D,
                this.getZ(), 18, 0.35D, 0.3D, 0.35D, 0.02D);
        server.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + this.getBbHeight() * 0.58D,
                this.getZ(), 12, 0.25D, 0.25D, 0.25D, 0.03D);
    }



    /** Starts two or three Eye-like charges with a readable pause between each rush. */
    private void startDashPattern(LivingEntity target) {
        this.dashTarget = target;
        this.dashesRemaining = this.phase >= 3 ? 3 : 2;
        this.dashRecoveryTicks = 0;
        this.dashChainHitRegistered = false;
        this.beginDashCharge(target);
    }

    private void beginDashCharge(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            this.dashTarget = null;
            this.entityData.set(ATTACK_STATE, 0);
            return;
        }
        Vector3d direction = target.position().add(0.0D, target.getBbHeight() * 0.45D, 0.0D)
                .subtract(this.position().add(0.0D, this.getBbHeight() * 0.55D, 0.0D)).normalize();
        this.dashDirX = direction.x;
        this.dashDirY = direction.y;
        this.dashDirZ = direction.z;
        this.dashChargeTicks = this.phase >= 3 ? 10 : 14;
        this.dashHitRegistered = false;
        this.entityData.set(ATTACK_STATE, 6);
        this.entityData.set(ATTACK_TICKS, this.dashChargeTicks);
        this.setDeltaMovement(Vector3d.ZERO);
        this.hasImpulse = true;
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 0.9F, 1.25F);
    }

    /** Server-side dash state machine. Motion and damage are authoritative; effects are sent to tracking clients. */
    private boolean tickDashPattern() {
        if (this.dashChargeTicks > 0) {
            this.dashChargeTicks--;
            this.entityData.set(ATTACK_TICKS, this.dashChargeTicks);
            if (this.level instanceof ServerWorld && this.dashChargeTicks % 2 == 0) {
                ServerWorld server = (ServerWorld) this.level;
                double y = this.getY() + this.getBbHeight() * 0.55D;
                server.sendParticles(ParticleTypes.DRAGON_BREATH, this.getX(), y, this.getZ(),
                        5, 0.28D, 0.24D, 0.28D, 0.015D);
                server.sendParticles(ParticleTypes.END_ROD,
                        this.getX() + this.dashDirX * 0.8D, y + this.dashDirY * 0.8D,
                        this.getZ() + this.dashDirZ * 0.8D, 3, 0.08D, 0.08D, 0.08D, 0.01D);
            }
            if (this.dashChargeTicks == 0) this.beginDashBurst();
            return true;
        }

        if (this.dashTicks > 0) {
            double speed = this.phase >= 3 ? 1.02D : 0.86D;
            this.setDeltaMovement(this.dashDirX * speed, this.dashDirY * speed,
                    this.dashDirZ * speed);
            this.hasImpulse = true;
            if (this.level instanceof ServerWorld) {
                ServerWorld server = (ServerWorld) this.level;
                server.sendParticles(ParticleTypes.DRAGON_BREATH,
                        this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                        4, 0.18D, 0.20D, 0.18D, 0.01D);
                server.sendParticles(ParticleTypes.END_ROD,
                        this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                        2, 0.10D, 0.12D, 0.10D, 0.015D);
            }
            if (!this.dashHitRegistered) this.checkDashImpact();
            this.dashTicks--;
            this.entityData.set(ATTACK_TICKS, this.dashTicks);
            if (this.dashTicks == 0) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.32D));
                this.hasImpulse = true;
                if (this.dashesRemaining > 0) {
                    this.dashRecoveryTicks = 8;
                    this.entityData.set(ATTACK_STATE, 6);
                    this.entityData.set(ATTACK_TICKS, this.dashRecoveryTicks);
                } else {
                    this.finishDashPattern();
                }
            }
            return true;
        }

        if (this.dashRecoveryTicks > 0) {
            this.dashRecoveryTicks--;
            this.entityData.set(ATTACK_TICKS, this.dashRecoveryTicks);
            if (this.dashRecoveryTicks == 0) {
                if (this.dashTarget != null && this.dashTarget.isAlive()) {
                    this.beginDashCharge(this.dashTarget);
                } else {
                    this.dashesRemaining = 0;
                    this.finishDashPattern();
                }
            }
            return true;
        }
        return false;
    }

    /** Ends the dash combo; a clean miss creates a brief counterattack window. */
    private void finishDashPattern() {
        this.dashChargeTicks = 0;
        this.dashTicks = 0;
        this.dashRecoveryTicks = 0;
        this.dashTarget = null;
        if (!this.dashChainHitRegistered) {
            this.entityData.set(ATTACK_STATE, 8);
            this.entityData.set(ATTACK_TICKS, 50);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.18D));
            this.hasImpulse = true;
            if (this.level instanceof ServerWorld) {
                ServerWorld server = (ServerWorld) this.level;
                server.sendParticles(ParticleTypes.END_ROD,
                        this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                        26, 0.65D, 0.5D, 0.65D, 0.035D);
                server.sendParticles(ParticleTypes.PORTAL,
                        this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                        16, 0.45D, 0.4D, 0.45D, 0.04D);
            }
            this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 0.95F, 0.86F);
        } else {
            this.entityData.set(ATTACK_STATE, 0);
            this.entityData.set(ATTACK_TICKS, 0);
        }
    }

    private void beginDashBurst() {
        this.dashTicks = this.phase >= 3 ? 10 : 9;
        this.dashesRemaining = Math.max(0, this.dashesRemaining - 1);
        this.entityData.set(ATTACK_STATE, 7);
        this.entityData.set(ATTACK_TICKS, this.dashTicks);
        double speed = this.phase >= 3 ? 1.02D : 0.86D;
        this.setDeltaMovement(this.dashDirX * speed, this.dashDirY * speed,
                this.dashDirZ * speed);
        this.hasImpulse = true;
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 1.0F, this.phase >= 3 ? 0.48F : 0.62F);
    }

    private void checkDashImpact() {
        AxisAlignedBB hitBox = this.getBoundingBox().inflate(0.42D, 0.30D, 0.42D);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, hitBox, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            if (!this.getBoundingBox().inflate(0.42D, 0.30D, 0.42D).intersects(player.getBoundingBox())) continue;
            player.hurt(DamageSource.mobAttack(this), this.phase >= 3 ? 14.0F : 10.0F);
            player.setDeltaMovement(player.getDeltaMovement().add(
                    this.dashDirX * 0.72D, 0.28D + this.dashDirY * 0.25D, this.dashDirZ * 0.72D));
            player.hurtMarked = true;
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 30, 0, true, true));
            this.dashHitRegistered = true;
            this.dashChainHitRegistered = true;
            if (this.level instanceof ServerWorld) {
                ((ServerWorld) this.level).sendParticles(ParticleTypes.CRIT,
                        player.getX(), player.getY(0.55D), player.getZ(), 16,
                        0.28D, 0.32D, 0.28D, 0.06D);
            }
            break;
        }
    }

    /** Starts a clearly telegraphed line attack; the locked endpoint lets players dodge before release. */
    private void prepareAstralRay(LivingEntity target) {
        this.beamEndX = target.getX();
        this.beamEndY = target.getY() + target.getBbHeight() * 0.55D;
        this.beamEndZ = target.getZ();
        this.beamChargeTicks = 22;
        this.entityData.set(ATTACK_STATE, 2);
        this.entityData.set(ATTACK_TICKS, this.beamChargeTicks);
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 0.9F, 1.18F);
    }

    /** Advances the charge and firing windows on the server and streams particles to every tracking client. */
    private boolean tickAstralBeam() {
        if (this.beamChargeTicks > 0) {
            this.beamChargeTicks--;
            this.entityData.set(ATTACK_TICKS, this.beamChargeTicks);
            if (this.beamChargeTicks % 3 == 0) {
                this.sendAstralRayParticles(1.0D, false);
                if (this.level instanceof ServerWorld) {
                    ServerWorld server = (ServerWorld) this.level;
                    server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + this.getBbHeight() * 0.58D,
                            this.getZ(), 6, 0.22D, 0.22D, 0.22D, 0.01D);
                }
            }
            if (this.beamChargeTicks == 0) this.beginAstralRay();
            return true;
        }
        if (this.beamTicks > 0) {
            this.sendAstralRayParticles(1.0D, true);
            this.beamTicks--;
            this.entityData.set(ATTACK_TICKS, this.beamTicks);
            if (this.beamTicks == 0) this.entityData.set(ATTACK_STATE, 0);
            return true;
        }
        return false;
    }

    private void beginAstralRay() {
        this.beamTicks = 10;
        this.entityData.set(ATTACK_STATE, 3);
        this.entityData.set(ATTACK_TICKS, this.beamTicks);
        this.sendAstralRayParticles(1.0D, true);
        this.damageAlongAstralRay();
        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) this.level;
            server.sendParticles(ParticleTypes.END_ROD, this.beamEndX, this.beamEndY, this.beamEndZ,
                    24, 0.4D, 0.4D, 0.4D, 0.06D);
            server.sendParticles(ParticleTypes.PORTAL, this.beamEndX, this.beamEndY, this.beamEndZ,
                    18, 0.35D, 0.35D, 0.35D, 0.08D);
        }
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 1.0F, 0.56F);
    }

    /** Draws a bright, slightly wavering particle beam instead of relying on an invisible line entity. */
    private void sendAstralRayParticles(double reach, boolean firing) {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) this.level;
        double sx = this.getX();
        double sy = this.getY() + this.getBbHeight() * 0.58D;
        double sz = this.getZ();
        double dx = this.beamEndX - sx;
        double dy = this.beamEndY - sy;
        double dz = this.beamEndZ - sz;
        double sideLength = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double sideX = -dz / sideLength;
        double sideZ = dx / sideLength;
        for (int i = 0; i <= 28; i++) {
            double progress = i / 28.0D;
            if (progress > reach) continue;
            double wave = Math.sin(this.tickCount * 0.72D + i * 0.9D) * (firing ? 0.11D : 0.035D);
            double px = sx + dx * progress + sideX * wave;
            double py = sy + dy * progress + Math.cos(this.tickCount * 0.5D + i) * wave;
            double pz = sz + dz * progress + sideZ * wave;
            // A layered ribbon is easier to see than a single strand of tiny end-rod particles.
            server.sendParticles(firing ? ParticleTypes.END_ROD : ParticleTypes.DRAGON_BREATH,
                    px, py, pz, firing ? 3 : 3, 0.045D, 0.045D, 0.045D, 0.0D);
            server.sendParticles(firing ? ParticleTypes.CRIT : ParticleTypes.PORTAL,
                    px, py, pz, firing ? 2 : 2, 0.035D, 0.035D, 0.035D, 0.0D);
            if (firing) {
                // A warm, unmistakable core separates the ray from the boss model and background.
                server.sendParticles(ParticleTypes.FLAME, px, py, pz,
                        1, 0.012D, 0.012D, 0.012D, 0.0D);
                if (i % 2 == 0) {
                    server.sendParticles(ParticleTypes.DRAGON_BREATH, px + sideX * 0.18D, py,
                            pz + sideZ * 0.18D, 2, 0.035D, 0.035D, 0.035D, 0.0D);
                }
            }
        }
    }

    /** Hits players intersecting the beam once, at release; the remaining beam time is visual. */
    private void damageAlongAstralRay() {
        double sx = this.getX();
        double sy = this.getY() + this.getBbHeight() * 0.58D;
        double sz = this.getZ();
        double dx = this.beamEndX - sx;
        double dy = this.beamEndY - sy;
        double dz = this.beamEndZ - sz;
        double lengthSq = dx * dx + dy * dy + dz * dz;
        if (lengthSq < 0.01D) return;
        AxisAlignedBB area = new AxisAlignedBB(
                Math.min(sx, beamEndX), Math.min(sy, beamEndY), Math.min(sz, beamEndZ),
                Math.max(sx, beamEndX), Math.max(sy, beamEndY), Math.max(sz, beamEndZ)).inflate(1.35D);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, area, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            double px = player.getX() - sx;
            double playerCenterY = player.getY(0.5D);
            double py = playerCenterY - sy;
            double pz = player.getZ() - sz;
            double t = (px * dx + py * dy + pz * dz) / lengthSq;
            if (t < 0.0D || t > 1.0D) continue;
            double nearX = sx + dx * t;
            double nearY = sy + dy * t;
            double nearZ = sz + dz * t;
            double ex = player.getX() - nearX;
            double ey = playerCenterY - nearY;
            double ez = player.getZ() - nearZ;
            if (ex * ex + ey * ey + ez * ez <= 1.35D * 1.35D) {
                player.hurt(DamageSource.indirectMagic(this, this), 12.0F);
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 50, 0, true, true));
            }
        }
    }

    private void tickAttackAnimation() {
        int ticks = this.entityData.get(ATTACK_TICKS);
        if (ticks > 0) {
            int nextTicks = ticks - 1;
            this.entityData.set(ATTACK_TICKS, nextTicks);
            int state = this.entityData.get(ATTACK_STATE);
            if (state == 4 || state == 5) this.sendAreaAttackTelegraph(state, nextTicks);
            if (state == 4 && nextTicks == 1 && this.gravityDamagePending) {
                this.resolveGravityWell();
            } else if (state == 5 && nextTicks == 1 && this.novaDamagePending) {
                this.resolveCosmicNova();
            }
            if (nextTicks == 0) {
                this.entityData.set(ATTACK_STATE, 0);
                this.gravityDamagePending = false;
                this.novaDamagePending = false;
            }
        }
    }

    /** Keeps the awakened silhouette readable with a low-cost orbit of client-visible particles. */
    private void sendPhaseAuraParticles() {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) this.level;
        double angle = this.tickCount * 0.11D;
        double radius = this.phase >= 3 ? 1.45D : 1.15D;
        double height = this.getY() + this.getBbHeight() * 0.58D;
        for (int i = 0; i < 4; i++) {
            double orbit = angle + Math.PI * 0.5D * i;
            double x = this.getX() + Math.cos(orbit) * radius;
            double z = this.getZ() + Math.sin(orbit) * radius;
            double y = height + Math.sin(orbit * 1.7D) * 0.32D;
            server.sendParticles(ParticleTypes.END_ROD, x, y, z, 1,
                    0.015D, 0.025D, 0.015D, 0.0D);
            if (this.phase >= 3 && i % 2 == 0) {
                server.sendParticles(ParticleTypes.PORTAL, x, y, z, 1,
                        0.025D, 0.025D, 0.025D, 0.01D);
            }
        }
    }

    private void sendPhaseTransitionPulse(int remainingTicks) {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) this.level;
        double progress = 1.0D - remainingTicks / 54.0D;
        double radius = 0.8D + progress * 3.8D;
        for (int i = 0; i < 20; i++) {
            double angle = Math.PI * 2.0D * i / 20.0D + this.tickCount * 0.035D;
            server.sendParticles(ParticleTypes.PORTAL,
                    this.getX() + Math.cos(angle) * radius,
                    this.getY() + this.getBbHeight() * 0.52D + Math.sin(angle * 2.0D) * 0.2D,
                    this.getZ() + Math.sin(angle) * radius, 1,
                    0.02D, 0.02D, 0.02D, 0.0D);
        }
        server.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                5, 0.32D, 0.3D, 0.32D, 0.02D);
    }

    /** Maintains an expanding warning ring so the player can read the radius before the attack lands. */
    private void sendAreaAttackTelegraph(int state, int remainingTicks) {
        if (!(this.level instanceof ServerWorld) || remainingTicks <= 0) return;
        ServerWorld server = (ServerWorld) this.level;
        double maxRadius = state == 4 ? 6.0D : 5.4D;
        double radius = maxRadius * (1.0D - remainingTicks / 24.0D);
        if (remainingTicks % 2 != 0) return;
        for (int i = 0; i < 20; i++) {
            double angle = Math.PI * 2.0D * i / 20.0D + this.tickCount * 0.025D;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            server.sendParticles(state == 4 ? ParticleTypes.PORTAL : ParticleTypes.DRAGON_BREATH,
                    x, this.getY() + 0.18D, z, 1, 0.015D, 0.025D, 0.015D, 0.0D);
        }
        server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + this.getBbHeight() * 0.56D,
                this.getZ(), state == 4 ? 3 : 5, 0.32D, 0.22D, 0.32D, 0.01D);
    }

    /** Starts a 24-tick gravity warning; the pull only happens on the release frame. */
    private void gravityWell() {
        this.gravityDamagePending = true;
        this.novaDamagePending = false;
        this.entityData.set(ATTACK_STATE, 4);
        this.entityData.set(ATTACK_TICKS, 24);
        this.sendAreaAttackTelegraph(4, 24);
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 0.9F, 0.92F);
    }

    /** Applies the pull and slow after the warning ring has reached its full radius. */
    private void resolveGravityWell() {
        this.gravityDamagePending = false;
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
            ServerWorld server = (ServerWorld) this.level;
            for (int i = 0; i < 36; i++) {
                double angle = Math.PI * 2.0D * i / 36.0D;
                server.sendParticles(ParticleTypes.PORTAL,
                        this.getX() + Math.cos(angle) * radius, this.getY() + 0.15D,
                        this.getZ() + Math.sin(angle) * radius, 2, 0.02D, 0.05D, 0.02D, 0.0D);
            }
            server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.8D, this.getZ(),
                    28, 1.8D, 0.7D, 1.8D, 0.12D);
            server.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.7D, this.getZ(),
                    2, 0.35D, 0.22D, 0.35D, 0.0D);
        }
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 0.95F, 0.72F);
    }

    /** Starts a ring warning before the final phase nova; damage is delayed to the slam frame. */
    private void cosmicNova() {
        this.gravityDamagePending = false;
        this.novaDamagePending = true;
        this.entityData.set(ATTACK_STATE, 5);
        this.entityData.set(ATTACK_TICKS, 24);
        this.sendAreaAttackTelegraph(5, 24);
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 1.0F, 0.7F);
    }

    private void resolveCosmicNova() {
        this.novaDamagePending = false;
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
                        this.getZ() + Math.sin(angle) * radius, 2, 0.025D, 0.04D, 0.025D, 0.0D);
            }
            server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5D, this.getZ(),
                    42, 2.0D, 0.9D, 2.0D, 0.08D);
            server.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.5D, this.getZ(),
                    28, 1.4D, 0.8D, 1.4D, 0.12D);
            server.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5D, this.getZ(),
                    3, 0.35D, 0.35D, 0.35D, 0.0D);
        }
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 1.0F, 0.48F);
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    /** A fully dodged dash combo briefly exposes the Devourer's core. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.entityData.get(ATTACK_STATE) == 8) amount *= 1.35F;
        return super.hurt(source, amount);
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
