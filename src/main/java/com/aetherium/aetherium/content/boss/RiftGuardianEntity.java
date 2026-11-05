package com.aetherium.aetherium.content.boss;

import com.aetherium.aetherium.content.entity.RiftStalkerEntity;
import com.aetherium.aetherium.content.projectile.DarkSlashEntity;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.SwimGoal;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

import java.util.List;

/** Two-phase arena boss: shockwave melee in phase I, ranged volleys and brutal slams in phase II. */
public class RiftGuardianEntity extends MonsterEntity {
    private final ServerBossInfo bossInfo = new ServerBossInfo(
            new TranslationTextComponent("entity.aetherium.rift_guardian"),
            BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS);
    private int attackCooldown = 35;
    // The violet sword's moving slash travels ~5.14 blocks from its wielder.
    // Keep the defensive shockwave visibly marked and just beyond that reach.
    private static final double ANTI_COMBO_RADIUS = 5.25D;
    // Prevent close-range Crimson sword pressure from leaving a neutral boss
    // waiting through a long AI cooldown. No damage immunity is introduced.
    private int lastCrimsonHitTick = -1000;
    private int rapidCrimsonHits = 0;
    private int lastPressureDamageTick = -1000;
    private int crimsonCounterCooldown = 0;
    private boolean crimsonCounterPending = false;
    // Alternate fairly between the familiar spacing shockwave and a warned charge.
    // Randomize only the very first choice to avoid a fixed opening every fight.
    private int lastPressureResponse = -1;
    private static final DataParameter<Integer> PHASE = EntityDataManager.defineId(RiftGuardianEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> SLAM_TICKS = EntityDataManager.defineId(RiftGuardianEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> ATTACK_STATE = EntityDataManager.defineId(RiftGuardianEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> MIRAGE_ID =
            EntityDataManager.defineId(RiftGuardianEntity.class, DataSerializers.INT);
    private int phase = 1;
    private int minionCooldown = 0;
    private int mirageCooldown = 165;
    private int attackPattern = 0;
    private double sweepDirectionX;
    private double sweepDirectionZ;
    private boolean sweepHitApplied = false;
    private boolean chargeHitApplied = false;
    private boolean slamDamagePending = false;
    private double pendingSlamRadius;
    private float pendingSlamDamage;
    private double pendingSlamKnockback;

    public RiftGuardianEntity(EntityType<? extends RiftGuardianEntity> type, World world) {
        super(type, world);
        this.xpReward = 55;
        this.setPersistenceRequired();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 1);
        this.entityData.define(SLAM_TICKS, 0);
        this.entityData.define(ATTACK_STATE, 0);
        this.entityData.define(MIRAGE_ID, -1);
    }

    public int getSlamAnimationTicks() {
        return this.entityData.get(SLAM_TICKS);
    }

    public int getAttackAnimationTicks() {
        return this.entityData.get(SLAM_TICKS);
    }

    public int getAttackState() {
        return this.entityData.get(ATTACK_STATE);
    }

    public int getMirageEntityId() {
        return this.entityData.get(MIRAGE_ID);
    }

    public void onMirageDispelled(int fakeId) {
        if (this.entityData.get(MIRAGE_ID) == fakeId) this.entityData.set(MIRAGE_ID, -1);
    }

    /** Old Crimson callback kept compatible; successful direct melee hits also count. */
    public void registerCrimsonHit(PlayerEntity attacker) {
        this.noteMeleePressure(attacker);
    }

    private void noteMeleePressure(PlayerEntity attacker) {
        if (this.level.isClientSide || attacker == null || !attacker.isAlive()
                || this.entityData.get(ATTACK_STATE) != 0
                || this.distanceToSqr(attacker) > ANTI_COMBO_RADIUS * ANTI_COMBO_RADIUS
                || this.lastPressureDamageTick == this.tickCount) return;
        this.lastPressureDamageTick = this.tickCount;
        this.attackCooldown = Math.min(this.attackCooldown, 24);
        this.rapidCrimsonHits = this.tickCount - this.lastCrimsonHitTick <= 36
                ? this.rapidCrimsonHits + 1 : 1;
        this.lastCrimsonHitTick = this.tickCount;
        if (this.rapidCrimsonHits < 3 || this.crimsonCounterCooldown > 0) return;
        this.rapidCrimsonHits = 0;
        this.crimsonCounterCooldown = 120;
        this.crimsonCounterPending = true;
        this.attackCooldown = Math.min(this.attackCooldown, 6);
        if (this.getTarget() == null) this.setTarget(attacker);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 450.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.13D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 0.62D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 16.0F));
        this.goalSelector.addGoal(4, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWater()) {
            Vector3d motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * 0.92D, Math.max(motion.y, 0.24D), motion.z * 0.92D);
            this.hasImpulse = true;
            if (this.tickCount % 20 == 0) {
                BlockPos shore = this.findNearestDryPosition();
                if (shore != null) {
                    double shoreX = shore.getX() + 0.5D - this.getX();
                    double shoreZ = shore.getZ() + 0.5D - this.getZ();
                    double shoreLength = Math.max(0.001D, Math.sqrt(shoreX * shoreX + shoreZ * shoreZ));
                    this.setDeltaMovement(motion.x * 0.55D + shoreX / shoreLength * 0.20D,
                            Math.max(motion.y, 0.24D),
                            motion.z * 0.55D + shoreZ / shoreLength * 0.20D);
                    this.hasImpulse = true;
                }
            }
        }
        if (this.level.isClientSide) return;
        if (this.crimsonCounterCooldown > 0) this.crimsonCounterCooldown--;
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
        if (this.phase >= 2 && this.entityData.get(ATTACK_STATE) == 0 && this.tickCount % 6 == 0) {
            this.sendRageAuraParticles();
        }
        int slamTicks = this.entityData.get(SLAM_TICKS);
        if (slamTicks > 0) {
            int nextSlamTick = slamTicks - 1;
            this.entityData.set(SLAM_TICKS, nextSlamTick);
            int state = this.entityData.get(ATTACK_STATE);
            if (state == 1 || state == 2 || state == 3 || state == 7) {
                // Do not let vanilla melee pathfinding drag the warning area across the arena.
                this.getNavigation().stop();
                // Wind cannon volleys get a stationary, clearly readable
                // warning. Melee slams keep their existing slight inertia.
                this.setDeltaMovement(state == 2
                        ? Vector3d.ZERO : this.getDeltaMovement().scale(state == 7 ? 0.28D : 0.45D));
            }
            if ((state == 1 || state == 3 || state == 4)
                    && nextSlamTick > 11 && nextSlamTick % 4 == 0) {
                this.sendGreatswordWindupTelegraph(state);
            }
            if (state == 2 && nextSlamTick > 16 && nextSlamTick % 3 == 0) {
                this.sendWindCannonWarning();
            }
            // 12-tick warning before the first wind crescent. The follow-up
            // arrives at tick 7, after the player has seen the first attack.
            if (state == 2 && (nextSlamTick == 16 || nextSlamTick == 7)) {
                LivingEntity volleyTarget = this.getTarget();
                if (volleyTarget != null && volleyTarget.isAlive()) {
                    this.launchRiftVolley(volleyTarget, nextSlamTick == 7);
                }
            }
            if (state == 7) {
                if (nextSlamTick > 0 && nextSlamTick % 3 == 0) this.sendFractureWarning(nextSlamTick);
                if (nextSlamTick == 20) this.resolveFracturePulse(false);
                if (nextSlamTick == 8) this.resolveFracturePulse(true);
            }
            if (this.entityData.get(ATTACK_STATE) == 3 && nextSlamTick == 11 && !this.sweepHitApplied) {
                this.resolveGreatswordSweep();
            }
            if (this.entityData.get(ATTACK_STATE) == 1 && nextSlamTick == 11 && this.slamDamagePending) {
                this.resolveGroundSlam();
            }
            if (this.entityData.get(ATTACK_STATE) == 4 && nextSlamTick <= 12) {
                // The dash sound starts exactly when the warning line becomes dangerous.
                if (nextSlamTick == 12)
                    this.playSound(RegistryHandler.GUARDIAN_CHARGE.get(), 0.96F, 1.0F);
                this.performGreatswordChargeStep();
            }
            if (this.entityData.get(ATTACK_STATE) == 6 && nextSlamTick > 0
                    && nextSlamTick % 8 == 0) {
                this.sendPhaseTransitionPulse(nextSlamTick);
            }
            if (slamTicks == 1) {
                if (this.entityData.get(ATTACK_STATE) == 4 && !this.chargeHitApplied) {
                    this.entityData.set(ATTACK_STATE, 5);
                    this.entityData.set(SLAM_TICKS, 50);
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.08D));
                    this.getNavigation().stop();
                    if (this.level instanceof ServerWorld) {
                        ServerWorld server = (ServerWorld) this.level;
                        server.sendParticles(ParticleTypes.END_ROD,
                                this.getX(), this.getY() + 1.15D, this.getZ(),
                                22, 0.55D, 0.7D, 0.55D, 0.035D);
                        server.sendParticles(ParticleTypes.CRIT,
                                this.getX(), this.getY() + 0.8D, this.getZ(),
                                14, 0.45D, 0.45D, 0.45D, 0.04D);
                    }
                    this.playSound(RegistryHandler.GUARDIAN_HURT.get(), 0.78F, 0.85F);
                } else {
                    this.entityData.set(ATTACK_STATE, 0);
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.25D));
                }
            }
        }

        if (this.entityData.get(ATTACK_STATE) == 5) {
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().scale(0.35D));
            this.hasImpulse = true;
        } else if (this.entityData.get(ATTACK_STATE) == 6) {
            this.getNavigation().stop();
            this.setDeltaMovement(this.getDeltaMovement().scale(0.18D));
            this.hasImpulse = true;
            return;
        }

        int nextPhase = this.getHealth() <= this.getMaxHealth() * 0.5F ? 2 : 1;
        // Boss phases only advance: healing must not roll back awakened attacks or stats.
        if (nextPhase > this.phase) {
            this.phase = nextPhase;
            this.entityData.set(PHASE, nextPhase);
            this.bossInfo.setColor(BossInfo.Color.RED);
            this.bossInfo.setName(new TranslationTextComponent("entity.aetherium.rift_guardian.phase_two"));
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D);
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(11.0D);
            this.attackCooldown = 0;
            this.slamDamagePending = false;
            this.sweepHitApplied = false;
            this.entityData.set(ATTACK_STATE, 6);
            this.entityData.set(SLAM_TICKS, 48);
            this.getNavigation().stop();
            this.setDeltaMovement(Vector3d.ZERO);
            if (this.level instanceof ServerWorld) {
                ServerWorld server = (ServerWorld) this.level;
                server.sendParticles(ParticleTypes.EXPLOSION,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 3, 0.45D, 0.45D, 0.45D, 0.0D);
                server.sendParticles(ParticleTypes.CRIT,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 42, 1.4D, 0.9D, 1.4D, 0.16D);
                server.sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 26, 1.0D, 0.6D, 1.0D, 0.035D);
                server.sendParticles(ParticleTypes.END_ROD,
                        this.getX(), this.getY() + 1.2D, this.getZ(), 16, 0.8D, 0.8D, 0.8D, 0.04D);
            }
            this.playSound(RegistryHandler.GUARDIAN_AWAKEN.get(), 1.0F, 0.92F);
            // Keep the awakening animation intact even if a target is in melee range.
            return;
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.minionCooldown > 0) this.minionCooldown--;

        LivingEntity target = this.getTarget();
        if (this.crimsonCounterPending && this.entityData.get(ATTACK_STATE) == 0
                && this.attackCooldown <= 0) {
            this.crimsonCounterPending = false;
            if (target != null && target.isAlive()
                    && this.distanceToSqr(target) <= ANTI_COMBO_RADIUS * ANTI_COMBO_RADIUS) {
                // A separate, clearly telegraphed 24-tick defensive slam.
                // Range matches the violet energy slash, damage remains ordinary.
                // Do not overwrite either phase's vulnerable/stagger states.
                int response = this.lastPressureResponse < 0
                        ? this.random.nextInt(2) : 1 - this.lastPressureResponse;
                this.lastPressureResponse = response;
                if (response == 1) {
                    // Existing charge animation locks direction before rushing.
                    // A miss exposes the Guardian for 50 ticks: avoidable, punishable.
                    this.startGreatswordCharge(target);
                    this.attackCooldown = this.phase == 1 ? 76 : 68;
                } else {
                    this.groundSlam(ANTI_COMBO_RADIUS,
                            this.phase == 1 ? 8.0F : 10.0F,
                            this.phase == 1 ? 1.05D : 1.18D);
                    this.attackCooldown = this.phase == 1 ? 65 : 55;
                }
                this.attackPattern++;
                return;
            }
        }
        // Summons take place between attacks, not during a stagger or a warning.
        if (phase >= 2 && target != null && target.isAlive() && this.minionCooldown <= 0
                && this.entityData.get(ATTACK_STATE) == 0) {
            this.trySpawnMinion();
            this.minionCooldown = 180;
        }
        if (this.mirageCooldown > 0) this.mirageCooldown--;
        if (phase >= 2 && target != null && target.isAlive() && this.mirageCooldown <= 0
                && this.entityData.get(ATTACK_STATE) == 0 && this.distanceToSqr(target) < 19.0D * 19.0D) {
            this.summonMirage();
            this.mirageCooldown = 290;
            this.attackCooldown = Math.max(this.attackCooldown, 22);
        }
        if (target == null || !target.isAlive() || this.attackCooldown > 0
                || this.entityData.get(ATTACK_STATE) != 0
                || this.distanceToSqr(target) > 20.0D * 20.0D) return;

        double distanceSq = this.distanceToSqr(target);
        if (phase >= 2 && distanceSq > 8.0D * 8.0D) {
            // A ranged target sees two staggered volleys, interleaved with a committed rush.
            if (distanceSq <= 15.0D * 15.0D && this.attackPattern % 2 == 0) {
                this.startGreatswordCharge(target);
                this.attackCooldown = 68;
            } else {
                this.fireRiftVolley(target);
                this.attackCooldown = 57;
            }
            this.attackPattern++;
        } else if (phase >= 2 && distanceSq > 4.2D * 4.2D
                && this.attackPattern % 4 == 3) {
            // Bring the projectile pattern into midrange rather than reserving it for kiting.
            this.fireRiftVolley(target);
            this.attackCooldown = 57;
            this.attackPattern++;
        } else if (distanceSq > 3.8D * 3.8D && distanceSq <= 8.0D * 8.0D) {
            if (phase >= 2 && this.attackPattern % 3 != 0) {
                this.startGreatswordCharge(target);
                this.attackCooldown = 68;
            } else {
                this.greatswordSweep(target);
                this.attackCooldown = phase == 1 ? 72 : 58;
            }
            this.attackPattern++;
        } else if (distanceSq <= 3.8D * 3.8D) {
            if (phase >= 2 && this.attackPattern % 3 == 0) {
                this.startFracture();
                this.attackCooldown = 64;
            } else {
                this.groundSlam(phase == 1 ? 3.5D : 4.35D,
                        phase == 1 ? 8.0F : 10.0F,
                        phase == 1 ? 0.78D : 1.15D);
                this.attackCooldown = phase == 1 ? 67 : 56;
            }
            this.attackPattern++;
        }

    }

    /** Starts a visible cast; two offset volleys fire on subsequent animation frames. */
    private void fireRiftVolley(LivingEntity target) {
        this.entityData.set(ATTACK_STATE, 2);
        this.entityData.set(SLAM_TICKS, 28);
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) this.level;
        server.sendParticles(ParticleTypes.CLOUD,
                this.getX(), this.getY() + this.getBbHeight() * 0.72D, this.getZ(),
                16, 0.45D, 0.45D, 0.45D, 0.035D);
        server.sendParticles(ParticleTypes.CRIT,
                this.getX(), this.getY() + this.getBbHeight() * 0.72D, this.getZ(),
                8, 0.30D, 0.26D, 0.30D, 0.022D);
        this.playSound(RegistryHandler.GUARDIAN_WINDUP.get(), 0.74F, 0.94F);
    }

    /** Pressurized air gathers around the cannon mouth before it fires. */
    private void sendWindCannonWarning() {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld)this.level;
        double centerY = this.getY() + this.getBbHeight() * 0.72D;
        for (int i = 0; i < 5; i++) {
            double angle = i * Math.PI * 2.0D / 5.0D + this.tickCount * 0.12D;
            server.sendParticles(ParticleTypes.CLOUD,
                    this.getX() + Math.cos(angle) * 0.65D,
                    centerY + Math.sin(angle) * 0.25D,
                    this.getZ() + Math.sin(angle) * 0.65D,
                    1, 0.045D, 0.045D, 0.045D, 0.008D);
        }
    }

    /** Uses aim-relative offsets so the fan points toward the player at every angle. */
    private void launchRiftVolley(LivingEntity target, boolean secondWave) {
        if (!(this.level instanceof ServerWorld)) return;
        double directionX = target.getX() - this.getX();
        double directionZ = target.getZ() - this.getZ();
        double length = Math.max(0.001D, Math.sqrt(directionX * directionX + directionZ * directionZ));
        double sideX = -directionZ / length;
        double sideZ = directionX / length;
        int halfSpread = secondWave ? 2 : 1;
        for (int i = -halfSpread; i <= halfSpread; i++) {
            double offset = i * (secondWave ? 0.85D : 1.2D);
            AetherBoltEntity bolt = new AetherBoltEntity(this.level, this);
            // A synchronized compressed-air crescent replaces the old
            // hovering rift crystal / fragment sprite completely.
            bolt.setStyle(AetherBoltEntity.WIND_SLICE);
            bolt.setPos(this.getX(), this.getY() + this.getBbHeight() * 0.72D, this.getZ());
            double dx = target.getX() + sideX * offset - bolt.getX();
            double dy = target.getY(0.45D) - bolt.getY();
            double dz = target.getZ() + sideZ * offset - bolt.getZ();
            bolt.shoot(dx, dy, dz, secondWave ? 1.38F : 1.22F, 1.0F);
            this.level.addFreshEntity(bolt);
        }
        ServerWorld server = (ServerWorld) this.level;
        server.sendParticles(ParticleTypes.CLOUD,
                this.getX(), this.getY() + this.getBbHeight() * 0.72D, this.getZ(),
                secondWave ? 18 : 12, 0.35D, 0.32D, 0.35D, 0.045D);
        // One short, distinct release cue: do not restart a long sound on each wave.
        if (!secondWave)
            this.playSound(RegistryHandler.GUARDIAN_SWEEP.get(), 0.65F, 1.22F);
    }

    /** Locks direction and raises the greatsword before a fast, punishable lunge. */
    private void startGreatswordCharge(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        this.sweepDirectionX = dx / length;
        this.sweepDirectionZ = dz / length;
        this.chargeHitApplied = false;
        this.entityData.set(ATTACK_STATE, 4);
        // 0.7 seconds of warning before the phase-II rush.
        this.entityData.set(SLAM_TICKS, this.phase >= 2 ? 26 : 24);
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.2D));
        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) this.level;
            for (int i = 1; i <= 6; i++) {
                double x = this.getX() + this.sweepDirectionX * i * 1.2D;
                double z = this.getZ() + this.sweepDirectionZ * i * 1.2D;
                server.sendParticles(ParticleTypes.CRIT, x, this.getY() + 0.18D, z,
                        3, 0.12D, 0.04D, 0.12D, 0.01D);
            }
            server.sendParticles(ParticleTypes.FLAME,
                    this.getX(), this.getY() + 0.25D, this.getZ(), 8,
                    0.25D, 0.10D, 0.25D, 0.015D);
        }
        this.playSound(RegistryHandler.GUARDIAN_WINDUP.get(), 0.82F, 0.83F);
    }

    /** Carries the Warden forward after the telegraph and resolves at most one player hit. */
    private void performGreatswordChargeStep() {
        if (this.entityData.get(ATTACK_STATE) != 4) return;
        double rushSpeed = this.phase >= 2 ? 0.90D : 0.78D;
        this.setDeltaMovement(this.sweepDirectionX * rushSpeed,
                Math.max(-0.08D, this.getDeltaMovement().y),
                this.sweepDirectionZ * rushSpeed);
        this.hasImpulse = true;

        // Leave a directional rift wake during the charge so its path reads clearly at combat speed.
        if (this.level instanceof ServerWorld && this.tickCount % 2 == 0) {
            ServerWorld server = (ServerWorld) this.level;
            for (int i = 1; i <= 5; i++) {
                double trailX = this.getX() - this.sweepDirectionX * i * 0.72D;
                double trailZ = this.getZ() - this.sweepDirectionZ * i * 0.72D;
                double trailY = this.getY() + 0.18D + i * 0.035D;
                server.sendParticles(ParticleTypes.END_ROD, trailX, trailY, trailZ,
                        1, 0.025D, 0.035D, 0.025D, 0.0D);
                if (i % 2 == 0) {
                    server.sendParticles(ParticleTypes.PORTAL, trailX, trailY, trailZ,
                            2, 0.045D, 0.035D, 0.045D, 0.01D);
                }
            }
        }
        if (this.chargeHitApplied) return;

        AxisAlignedBB hitBox = this.getBoundingBox().inflate(0.52D, 0.24D, 0.52D);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, hitBox, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            if (!this.getBoundingBox().inflate(0.52D, 0.24D, 0.52D).intersects(player.getBoundingBox())) continue;
            if (!player.hurt(DamageSource.mobAttack(this), this.phase >= 2 ? 13.0F : 11.0F))
                continue;
            player.setDeltaMovement(player.getDeltaMovement().add(
                    this.sweepDirectionX * 0.85D, 0.32D, this.sweepDirectionZ * 0.85D));
            player.hurtMarked = true;
            this.chargeHitApplied = true;
            if (this.level instanceof ServerWorld) {
                ServerWorld server = (ServerWorld) this.level;
                server.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY(0.55D), player.getZ(),
                        20, 0.3D, 0.34D, 0.3D, 0.07D);
                server.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        player.getX(), player.getY(0.5D), player.getZ(), 2,
                        0.0D, 0.0D, 0.0D, 0.0D);
            }
            this.playSound(RegistryHandler.GUARDIAN_SWEEP.get(), 0.78F, 0.89F);
            break;
        }
    }

    /** A broad, telegraphed cleave that sweeps the space in front of the Guardian. */
    /** Finds a nearby dry two-block-high landing spot while the boss is submerged. */
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

    /** Repeats the weapon warnings until release, marking the same area each attack can hit. */
    private void sendGreatswordWindupTelegraph(int state) {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) this.level;
        if (state == 1) {
            // The ring matches the radial ground-slam damage radius.
            for (int i = 0; i < 16; i++) {
                double angle = Math.PI * 2.0D * i / 16.0D;
                server.sendParticles(ParticleTypes.PORTAL,
                        this.getX() + Math.cos(angle) * this.pendingSlamRadius,
                        this.getY() + 0.12D,
                        this.getZ() + Math.sin(angle) * this.pendingSlamRadius,
                        1, 0.015D, 0.02D, 0.015D, 0.0D);
            }
        } else if (state == 3) {
            // The cleave reaches 5.8 blocks in a forward cone (dot >= 0.28).
            double direction = Math.atan2(this.sweepDirectionZ, this.sweepDirectionX);
            for (int i = 0; i <= 12; i++) {
                double angle = direction - 1.28D + 2.56D * i / 12.0D;
                double x = Math.cos(angle);
                double z = Math.sin(angle);
                server.sendParticles(ParticleTypes.END_ROD,
                        this.getX() + x * 5.6D, this.getY() + 0.18D,
                        this.getZ() + z * 5.6D,
                        1, 0.02D, 0.02D, 0.02D, 0.0D);
                if (i % 3 == 0) {
                    server.sendParticles(ParticleTypes.PORTAL,
                            this.getX() + x * 2.8D, this.getY() + 0.18D,
                            this.getZ() + z * 2.8D,
                            1, 0.02D, 0.02D, 0.02D, 0.0D);
                }
            }
        } else if (state == 4) {
            // The lunge follows its locked direction; leave a sparse line ahead.
            for (int i = 1; i <= 6; i++) {
                server.sendParticles(ParticleTypes.END_ROD,
                        this.getX() + this.sweepDirectionX * i * 1.2D,
                        this.getY() + 0.18D,
                        this.getZ() + this.sweepDirectionZ * i * 1.2D,
                        1, 0.02D, 0.025D, 0.02D, 0.0D);
            }
        }
    }

    /** Raises the greatsword and locks the facing direction before the cleave lands. */
    private void greatswordSweep(LivingEntity target) {
        this.entityData.set(ATTACK_STATE, 3);
        this.entityData.set(SLAM_TICKS, 24);
        double aimX = target.getX() - this.getX();
        double aimZ = target.getZ() - this.getZ();
        double aimLength = Math.max(0.001D, Math.sqrt(aimX * aimX + aimZ * aimZ));
        this.sweepDirectionX = aimX / aimLength;
        this.sweepDirectionZ = aimZ / aimLength;
        this.sweepHitApplied = false;
        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) this.level;
            server.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.5D, this.getZ(),
                    8, 0.35D, 0.28D, 0.35D, 0.015D);
            server.sendParticles(ParticleTypes.END_ROD,
                    this.getX() + this.sweepDirectionX * 1.8D, this.getY() + 1.5D,
                    this.getZ() + this.sweepDirectionZ * 1.8D, 5,
                    0.12D, 0.10D, 0.12D, 0.01D);
        }
        this.playSound(RegistryHandler.GUARDIAN_WINDUP.get(), 0.78F, 1.05F);
    }

    /** Resolves the sweep at the visible follow-through frame, not at the start of the wind-up. */
    private void resolveGreatswordSweep() {
        this.sweepHitApplied = true;
        AxisAlignedBB area = this.getBoundingBox().inflate(6.0D, 1.8D, 6.0D);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, area, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 5.8D || distance < 0.001D
                    || Math.abs(player.getY() - this.getY()) > this.getBbHeight() + 1.0D) continue;
            double dot = (dx * this.sweepDirectionX + dz * this.sweepDirectionZ) / distance;
            if (dot < 0.28D) continue;
            if (player.hurt(DamageSource.mobAttack(this), this.phase == 1 ? 9.0F : 12.0F)) {
                player.setDeltaMovement(player.getDeltaMovement().add(
                        this.sweepDirectionX * 0.95D, 0.22D, this.sweepDirectionZ * 0.95D));
                player.hurtMarked = true;
                if (this.phase >= 2)
                    player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 32, 0, true, true));
            }
        }

        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) this.level;
            double baseAngle = Math.atan2(this.sweepDirectionZ, this.sweepDirectionX);
            for (int i = 0; i <= 24; i++) {
                double angle = baseAngle - 1.08D + 2.16D * i / 24.0D;
                // Match the actual 5.8-block sweep reach, not a misleading 2.2-block arc.
                double radius = 5.25D + (i % 3) * 0.20D;
                double px = this.getX() + Math.cos(angle) * radius;
                double pz = this.getZ() + Math.sin(angle) * radius;
                server.sendParticles(ParticleTypes.SWEEP_ATTACK, px, this.getY() + 1.05D, pz,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
                server.sendParticles(ParticleTypes.CRIT, px, this.getY() + 1.0D, pz,
                        2, 0.06D, 0.12D, 0.06D, 0.025D);
                server.sendParticles(ParticleTypes.END_ROD, px, this.getY() + 1.0D, pz,
                        1, 0.025D, 0.025D, 0.025D, 0.0D);
            }
            server.sendParticles(ParticleTypes.EXPLOSION, this.getX() + this.sweepDirectionX * 3.6D,
                    this.getY() + 1.0D, this.getZ() + this.sweepDirectionZ * 3.6D,
                    2, 0.25D, 0.25D, 0.25D, 0.0D);
        }
        this.playSound(RegistryHandler.GUARDIAN_SWEEP.get(), 1.0F, 0.94F);
    }

    /** Phase-II pattern: first a close shockwave, then a larger annular fracture. */
    private void startFracture() {
        this.entityData.set(ATTACK_STATE, 7);
        this.entityData.set(SLAM_TICKS, 36);
        this.getNavigation().stop();
        this.sendFractureWarning(36);
        this.playSound(RegistryHandler.GUARDIAN_WINDUP.get(), 0.82F, 0.76F);
    }

    private void sendFractureWarning(int remaining) {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld)this.level;
        double[] rings = {3.5D, 4.1D, 7.0D};
        for (int ring = 0; ring < rings.length; ring++) {
            if (ring == 0 && remaining < 20) continue;
            for (int i = 0; i < 24; i++) {
                double angle = i * Math.PI * 2.0D / 24.0D;
                server.sendParticles(ring == 0 ? ParticleTypes.CRIT : ParticleTypes.PORTAL,
                        this.getX() + Math.cos(angle) * rings[ring], this.getY() + 0.13D,
                        this.getZ() + Math.sin(angle) * rings[ring], 1, 0.02D, 0.03D, 0.02D, 0.0D);
            }
        }
    }

    private void resolveFracturePulse(boolean outer) {
        double radius = outer ? 7.0D : 3.5D;
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class,
                this.getBoundingBox().inflate(radius, 1.2D, radius), PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            double sq = dx * dx + dz * dz;
            if (sq > radius * radius || (outer && sq < 4.1D * 4.1D)
                    || Math.abs(player.getY() - this.getY()) > 1.9D) continue;
            double distance = Math.max(0.001D, Math.sqrt(sq));
            if (player.hurt(DamageSource.mobAttack(this), outer ? 9.0F : 8.0F)) {
                player.setDeltaMovement(player.getDeltaMovement().add(
                        dx / distance * 0.62D, 0.24D, dz / distance * 0.62D));
                player.hurtMarked = true;
            }
        }
        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld)this.level;
            double burstRadius = outer ? 5.5D : 3.1D;
            for (int i = 0; i < 28; i++) {
                double angle = i * Math.PI * 2.0D / 28.0D;
                server.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        this.getX() + Math.cos(angle) * burstRadius,
                        this.getY() + 0.20D, this.getZ() + Math.sin(angle) * burstRadius,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        this.playSound(RegistryHandler.RIFT_GUARDIAN_SLAM.get(), 0.77F, outer ? 1.20F : 0.84F);
    }

    /** Telegraphs the overhead hammer blow; damage is delayed until the impact animation. */
    private void groundSlam(double radius, float damage, double knockback) {
        this.entityData.set(ATTACK_STATE, 1);
        this.entityData.set(SLAM_TICKS, 24);
        this.pendingSlamRadius = radius;
        this.pendingSlamDamage = damage;
        this.pendingSlamKnockback = knockback;
        this.slamDamagePending = true;
        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) this.level;
            for (int i = 0; i < 20; i++) {
                double angle = Math.PI * 2.0D * i / 20.0D;
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;
                server.sendParticles(ParticleTypes.PORTAL, x, this.getY() + 0.12D, z,
                        1, 0.015D, 0.02D, 0.015D, 0.0D);
            }
            server.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.5D, this.getZ(),
                    8, 0.25D, 0.2D, 0.25D, 0.015D);
        }
        this.playSound(RegistryHandler.GUARDIAN_WINDUP.get(), 0.85F, 0.86F);
    }

    /** Resolves the ground shockwave on the same frame the raised weapon comes down. */
    private void resolveGroundSlam() {
        this.slamDamagePending = false;
        double radius = this.pendingSlamRadius;
        float damage = this.pendingSlamDamage;
        double knockback = this.pendingSlamKnockback;
        AxisAlignedBB area = new AxisAlignedBB(
                this.getX() - radius, this.getY() - 0.5D, this.getZ() - radius,
                this.getX() + radius, this.getY() + this.getBbHeight() + 1.0D, this.getZ() + radius);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, area, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            // The floor mark is a ground shockwave: a player clearly above it is safe.
            if (dx * dx + dz * dz > radius * radius
                    || Math.abs(player.getY() - this.getY()) > 1.9D) continue;
            double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            if (player.hurt(DamageSource.mobAttack(this), damage)) {
                player.setDeltaMovement(player.getDeltaMovement().add(
                        dx / length * knockback, 0.32D, dz / length * knockback));
                player.hurtMarked = true;
                if (phase == 2)
                    player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 45, 0, true, true));
            }
        }

        if (this.level instanceof ServerWorld) {
            ServerWorld server = (ServerWorld) this.level;
            for (int i = 0; i < 36; i++) {
                double angle = Math.PI * 2.0D * i / 36.0D;
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;
                server.sendParticles(ParticleTypes.SWEEP_ATTACK, x, this.getY() + 0.15D, z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
                server.sendParticles(ParticleTypes.END_ROD, x, this.getY() + 0.15D, z,
                        1, 0.02D, 0.03D, 0.02D, 0.0D);
            }
            server.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.25D, this.getZ(),
                    phase == 1 ? 24 : 38, radius * 0.38D, 0.10D, radius * 0.38D, 0.05D);
            server.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.25D, this.getZ(),
                    phase == 1 ? 5 : 18, radius * 0.24D, 0.10D, radius * 0.24D, 0.025D);
        }
        this.playSound(RegistryHandler.RIFT_GUARDIAN_SLAM.get(), 0.95F, phase == 1 ? 0.92F : 0.68F);
    }

    private void summonMirage() {
        if (!(this.level instanceof ServerWorld) || this.entityData.get(MIRAGE_ID) >= 0) return;
        BossMirageEntity fake = RegistryHandler.BOSS_MIRAGE.get().create(this.level);
        if (fake == null) return;
        fake.setOwner(this);
        fake.setPos(this.getX() - 3.0D, this.getY(), this.getZ() + 2.1D);
        if (this.level.addFreshEntity(fake)) {
            this.entityData.set(MIRAGE_ID, fake.getId());
            ((ServerWorld)this.level).sendParticles(ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 1.4D, this.getZ(),
                    28, 1.6D, 0.95D, 1.6D, 0.08D);
            this.playSound(RegistryHandler.GUARDIAN_MIRAGE.get(), 0.80F, 1.05F);
        }
    }

    private void dispelMirage() {
        int id = this.entityData.get(MIRAGE_ID);
        if (id < 0) return;
        this.entityData.set(MIRAGE_ID, -1);
        net.minecraft.entity.Entity fake = this.level.getEntity(id);
        if (fake instanceof BossMirageEntity) ((BossMirageEntity) fake).expire();
    }

    /** Invokes up to two stalkers per wave, with a strict four-minion arena cap. */
    private void trySpawnMinion() {
        if (!(this.level instanceof ServerWorld)) return;
        AxisAlignedBB area = this.getBoundingBox().inflate(20.0D);
        List<RiftStalkerEntity> existing = this.level.getEntitiesOfClass(RiftStalkerEntity.class, area,
                RiftStalkerEntity::isAlive);
        int count = Math.min(2, 4 - existing.size());
        if (count <= 0) return;
        LivingEntity target = this.getTarget();
        ServerWorld server = (ServerWorld) this.level;
        for (int i = 0; i < count; i++) {
            double angle = this.tickCount * 0.12D + i * Math.PI;
            int x = (int)Math.floor(this.getX() + Math.cos(angle) * 4.0D);
            int z = (int)Math.floor(this.getZ() + Math.sin(angle) * 4.0D);
            BlockPos spawn = this.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, this.getY(), z));
            if (!this.level.getFluidState(spawn).isEmpty() || !this.level.getBlockState(spawn).isAir()) continue;
            RiftStalkerEntity minion = RegistryHandler.RIFT_STALKER.get().create(this.level);
            if (minion == null) continue;
            minion.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                    this.yRot, 0.0F);
            if (target != null && target.isAlive()) minion.setTarget(target);
            this.level.addFreshEntity(minion);
            server.sendParticles(ParticleTypes.PORTAL, minion.getX(), minion.getY() + 0.7D,
                    minion.getZ(), 20, 0.45D, 0.4D, 0.45D, 0.05D);
        }
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    /** Adds sparse sparks around the awakened armor without flooding the particle budget. */
    private void sendRageAuraParticles() {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) this.level;
        double angle = this.tickCount * 0.17D;
        double radius = 0.72D;
        double height = this.getY() + this.getBbHeight() * 0.72D;
        for (int i = 0; i < 3; i++) {
            double orbit = angle + Math.PI * 2.0D * i / 3.0D;
            double x = this.getX() + Math.cos(orbit) * radius;
            double z = this.getZ() + Math.sin(orbit) * radius;
            server.sendParticles(ParticleTypes.CRIT, x, height + Math.sin(orbit) * 0.18D, z,
                    1, 0.025D, 0.035D, 0.025D, 0.0D);
            if (i == 0) {
                server.sendParticles(ParticleTypes.PORTAL, x, height, z, 1,
                        0.02D, 0.025D, 0.02D, 0.01D);
            }
        }
    }

    /** Emits a modest expanding ring during the Warden's phase-two awakening. */
    private void sendPhaseTransitionPulse(int remainingTicks) {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) this.level;
        double progress = 1.0D - remainingTicks / 48.0D;
        double radius = 1.0D + progress * 3.5D;
        for (int i = 0; i < 18; i++) {
            double angle = Math.PI * 2.0D * i / 18.0D;
            server.sendParticles(ParticleTypes.END_ROD,
                    this.getX() + Math.cos(angle) * radius, this.getY() + 0.12D,
                    this.getZ() + Math.sin(angle) * radius, 1,
                    0.015D, 0.035D, 0.015D, 0.0D);
        }
        server.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0D, this.getZ(),
                4, 0.28D, 0.38D, 0.28D, 0.025D);
    }

    /** A failed greatsword charge leaves the Warden open to counterattacks. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.entityData.get(ATTACK_STATE) == 5) amount *= 1.45F;
        boolean damaged = super.hurt(source, amount);
        if (damaged && !this.level.isClientSide) {
            this.dispelMirage();
            if (source.getEntity() instanceof PlayerEntity
                    && (source.getDirectEntity() == source.getEntity()
                        || source.getDirectEntity() instanceof DarkSlashEntity))
                this.noteMeleePressure((PlayerEntity) source.getEntity());
        }
        return damaged;
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
        this.dispelMirage();
        super.die(source);
        this.bossInfo.removeAllPlayers();
    }

    @Override
    public boolean doHurtTarget(net.minecraft.entity.Entity target) {
        // All Guardian damage is applied by its timed slam, sword sweep, charge or bolts.
        // Disable untelegraphed vanilla MeleeAttackGoal hits; that goal still handles chase.
        return false;
    }

    @Override
    protected net.minecraft.util.SoundEvent getAmbientSound() {
        return this.entityData.get(ATTACK_STATE) == 0 && this.getTarget() == null
                ? RegistryHandler.GUARDIAN_AMBIENT.get() : null;
    }

    @Override
    protected net.minecraft.util.SoundEvent getHurtSound(DamageSource source) {
        return RegistryHandler.GUARDIAN_HURT.get();
    }

    @Override
    protected net.minecraft.util.SoundEvent getDeathSound() {
        return RegistryHandler.GUARDIAN_DEATH.get();
    }
}
