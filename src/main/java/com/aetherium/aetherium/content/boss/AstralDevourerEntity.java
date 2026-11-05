package com.aetherium.aetherium.content.boss;

import com.aetherium.aetherium.registry.RegistryHandler;
import com.aetherium.aetherium.content.entity.RiftStalkerEntity;
import net.minecraft.world.gen.Heightmap;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/** Winged Executioner: armored aerial hunter with blade feathers, deceptive copies and a chest-eye ray. */
public class AstralDevourerEntity extends MonsterEntity {
    private final ServerBossInfo bossInfo = new ServerBossInfo(
            new TranslationTextComponent("entity.aetherium.astral_devourer"),
            BossInfo.Color.BLUE, BossInfo.Overlay.PROGRESS);
    private static final DataParameter<Integer> PHASE = EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> ATTACK_TICKS = EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> ATTACK_STATE = EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.INT);
    // A locked endpoint is synchronized so every client can draw the full beam.
    private static final DataParameter<Integer> MIRAGE_ID =
            EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.INT);
    private static final DataParameter<Float> BEAM_X = EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> BEAM_Y = EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> BEAM_Z = EntityDataManager.defineId(AstralDevourerEntity.class, DataSerializers.FLOAT);
    private int phase = 1;
    private int attackCooldown = 45;
    // React to repeated, correctly spaced Crimson sword strikes by retreating
    // before the next telegraphed attack, never by gaining invulnerability.
    private int lastCrimsonHitTick = -1000;
    private int rapidCrimsonHits = 0;
    private int lastPressureDamageTick = -1000;
    private boolean pincerPending = false;
    private int crimsonRetreatCooldown = 0;
    private int crimsonRetreatTicks = 0;
    private int attackCycle = 0;
    private int minionCooldown = 200;
    private int mirageCooldown = 155;
    private final List<Integer> activeMirages = new ArrayList<>();
    private float queuedVolleyVelocity = 1.16F;
    private float queuedVolleyInaccuracy = 0.0F;
    private int beamChargeTicks = 0;
    private int beamTicks = 0;
    // Short-lived per-attack tracking prevents rapid repeated damage and permits
    // players to enter the beam at any point during its actual firing window.
    private final Map<UUID, Integer> beamLastHitTick = new HashMap<>();
    private final Map<UUID, Integer> beamHitCount = new HashMap<>();
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
        this.entityData.define(MIRAGE_ID, -1);
        this.entityData.define(BEAM_X, 0.0F);
        this.entityData.define(BEAM_Y, 0.0F);
        this.entityData.define(BEAM_Z, 0.0F);
    }

    public int getAttackAnimationTicks() {
        return this.entityData.get(ATTACK_TICKS);
    }

    public int getAttackState() {
        return this.entityData.get(ATTACK_STATE);
    }

    /** Entity ID of the attackable copy, or -1 when the illusion has expired. */
    public int getMirageEntityId() {
        return this.entityData.get(MIRAGE_ID);
    }

    /** Each decoy disappears on its own; the other copies keep deceiving. */
    public void onMirageDispelled(int fakeId) {
        this.activeMirages.remove(Integer.valueOf(fakeId));
        this.entityData.set(MIRAGE_ID,
                this.activeMirages.isEmpty() ? -1 : this.activeMirages.get(0));
    }

    /** Compatibility hook retained for the Crimson sword's successful-hit callback. */
    public void registerCrimsonHit(PlayerEntity attacker) {
        this.noteMeleePressure(attacker);
    }

    /** Repeated hits produce a real retreat without cancelling beam/dash or vulnerability. */
    private void noteMeleePressure(PlayerEntity attacker) {
        if (this.level.isClientSide || attacker == null || !attacker.isAlive()
                || this.entityData.get(ATTACK_STATE) != 0 || this.isInWater()
                || this.distanceToSqr(attacker) > 7.0D * 7.0D
                || this.lastPressureDamageTick == this.tickCount) return;
        this.lastPressureDamageTick = this.tickCount;
        this.attackCooldown = Math.min(this.attackCooldown, 26);
        this.rapidCrimsonHits = this.tickCount - this.lastCrimsonHitTick <= 36
                ? this.rapidCrimsonHits + 1 : 1;
        this.lastCrimsonHitTick = this.tickCount;
        if (this.rapidCrimsonHits < 3 || this.crimsonRetreatCooldown > 0) return;
        this.rapidCrimsonHits = 0;
        this.crimsonRetreatCooldown = 120;
        this.crimsonRetreatTicks = 20;
        this.attackCooldown = Math.min(this.attackCooldown, 12);
        if (this.getTarget() == null) this.setTarget(attacker);
        if (this.level instanceof ServerWorld)
            ((ServerWorld)this.level).sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY(0.6D), this.getZ(),
                    12, 0.34D, 0.24D, 0.34D, 0.02D);
    }

    /** Fixed world-space beam endpoint, synchronized to tracking clients. */
    public Vector3d getBeamRenderTarget() {
        return new Vector3d(this.entityData.get(BEAM_X),
                this.entityData.get(BEAM_Y), this.entityData.get(BEAM_Z));
    }

    /**
     * Laser muzzle on the exposed chest eye, not the middle of a floating head.
     * The server collision line and client render both use this exact function.
     */
    public Vector3d getBeamOrigin() {
        Vector3d center = this.position().add(0.0D, this.getBbHeight() * 0.58D, 0.0D);
        Vector3d axis = this.getBeamRenderTarget().subtract(center);
        return axis.lengthSqr() < 0.0001D
                ? center : center.add(axis.normalize().scale(0.38D));
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 650.0D)
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
        if (this.crimsonRetreatCooldown > 0) this.crimsonRetreatCooldown--;
        if (this.crimsonRetreatTicks > 0) this.crimsonRetreatTicks--;
        if (this.phase >= 2 && this.entityData.get(ATTACK_STATE) == 0 && this.tickCount % 6 == 0) {
            this.sendPhaseAuraParticles();
        }

        LivingEntity target = this.getTarget();
        boolean phaseTransitionActive = this.entityData.get(ATTACK_STATE) == 9;
        boolean dashActive = phaseTransitionActive ? false : this.tickDashPattern();
        if (!phaseTransitionActive && !dashActive) {
            int activeState = this.entityData.get(ATTACK_STATE);
            if (this.beamChargeTicks > 0 || this.beamTicks > 0
                    || activeState == 4 || activeState == 5 || activeState == 8) {
                // The warning line and damage line must share a stationary origin.
                this.getNavigation().stop();
                this.setDeltaMovement(Vector3d.ZERO);
                this.hasImpulse = true;
            } else {
                this.updateHoverMovement(target);
                // Feather volleys keep moving slowly; only the laser and ring strikes lock position.
                if (activeState == 1)
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.58D));
            }
        }
        if (this.isInWater() && !phaseTransitionActive) {
            Vector3d motion = this.getDeltaMovement();
            this.setDeltaMovement(motion.x * 0.92D, Math.max(motion.y, 0.20D), motion.z * 0.92D);
            this.hasImpulse = true;
        }

        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
        int nextPhase = this.getHealth() <= this.getMaxHealth() * 0.33F ? 3
                : this.getHealth() <= this.getMaxHealth() * 0.66F ? 2 : 1;
        // Boss phases only advance: healing must not roll back awakened attacks or stats.
        if (nextPhase > this.phase) {
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
            // The awakening itself is a safe breathing window for repositioning.
            this.attackCooldown = 30;
            this.dashChargeTicks = 0;
            this.dashTicks = 0;
            this.dashRecoveryTicks = 0;
            this.dashesRemaining = 0;
            this.dashTarget = null;
            this.beamChargeTicks = 0;
            this.beamTicks = 0;
            this.beamLastHitTick.clear();
            this.beamHitCount.clear();
            this.gravityDamagePending = false;
            this.novaDamagePending = false;
            this.pincerPending = false;
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
            this.playSound(RegistryHandler.ASTRAL_AWAKEN.get(), 0.95F, phase == 3 ? 0.84F : 1.0F);
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

        if (this.mirageCooldown > 0) this.mirageCooldown--;
        if (this.phase >= 2 && this.mirageCooldown <= 0
                && this.entityData.get(ATTACK_STATE) == 0
                && target != null && target.isAlive() && this.distanceToSqr(target) < 22.0D * 22.0D) {
            this.summonMirage();
            this.mirageCooldown = this.phase >= 3 ? 250 : 320;
            this.attackCooldown = Math.max(this.attackCooldown, 24);
        }
        if (this.minionCooldown > 0) this.minionCooldown--;
        if (this.phase >= 2 && this.minionCooldown <= 0 && target != null && target.isAlive()
                && this.entityData.get(ATTACK_STATE) == 0
                && this.distanceToSqr(target) < 22.0D * 22.0D) {
            this.summonAstralServants(target);
            this.minionCooldown = this.phase >= 3 ? 190 : 250;
        }
        if (this.attackCooldown > 0) this.attackCooldown--;
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > 28.0D * 28.0D
                || this.attackCooldown > 0 || this.entityData.get(ATTACK_STATE) != 0) return;

        double distanceSq = this.distanceToSqr(target);
        this.attackCycle++;
        // Reserve independent attack slots. Previously phase-III dashes consumed the
        // same even turns as the laser, effectively preventing the laser from firing.
        int pattern = this.phase >= 3 ? this.attackCycle % 4 : this.attackCycle % 3;
        if (this.phase >= 2 && this.attackCycle % 4 == 3
                && distanceSq > 4.0D * 4.0D && distanceSq < 20.0D * 20.0D) {
            this.startFeatherPincer();
            this.attackCooldown = this.phase >= 3 ? 56 : 64;
            return;
        }
        boolean dashTurn = this.phase >= 3 ? (pattern == 0 || pattern == 2) : pattern == 0;
        if (dashTurn && distanceSq > 3.6D * 3.6D && distanceSq < 22.0D * 22.0D) {
            this.startDashPattern(target);
            this.attackCooldown = phase >= 3 ? 45 : (phase >= 2 ? 58 : 80);
            return;
        }
        if (phase >= 2 && pattern == 1 && distanceSq > 5.6D * 5.6D) {
            this.prepareAstralRay(target);
            this.attackCooldown = phase >= 3 ? 62 : 76;
            return;
        }
        if (phase == 1) {
            // The opening phase teaches dodging with quick shots and a readable gravity pull.
            if (distanceSq > 6.5D * 6.5D) {
                this.shootBolt(target, 0.0F, 1.16F);
                this.attackCooldown = 52;
            } else {
                this.gravityWell();
                this.attackCooldown = 70;
            }
        } else if (phase == 2) {
            // Phase II follows an initial spread with a later, tighter fan.
            if (distanceSq > 9.0D * 9.0D) {
                this.shootBolt(target, 0.25F, 1.18F);
                this.attackCooldown = 51;
            } else if (this.attackCycle % 2 == 0) {
                this.gravityWell();
                this.attackCooldown = 72;
            } else {
                this.shootBolt(target, 0.45F, 1.16F);
                this.attackCooldown = 54;
            }
        } else {
            // Phase III pressures with faster projectiles and close-range novas.
            if (distanceSq > 7.5D * 7.5D) {
                this.shootBolt(target, 0.35F, 1.30F);
                this.attackCooldown = 45;
            } else if (this.attackCycle % 2 == 0) {
                this.cosmicNova();
                this.attackCooldown = 66;
            } else {
                this.shootBolt(target, 0.45F, 1.24F);
                this.attackCooldown = 49;
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
            // Float above the target instead of hovering almost motionless at its feet.
            double hoverY = target.getY() + (this.phase >= 2 ? 3.3D : 2.8D);
            desiredY = Math.max(this.isInWater() ? 0.20D : -0.14D,
                    Math.min(this.isInWater() ? 0.30D : 0.14D, (hoverY - this.getY()) * 0.075D));

            double preferredDistance = this.phase == 1 ? 9.0D : (this.phase == 2 ? 7.0D : 5.8D);
            double side = this.attackCycle % 4 < 2 ? 1.0D : -1.0D;
            double orbitSpeed = this.phase == 1 ? 0.10D : (this.phase == 2 ? 0.17D : 0.22D);
            double approach = horizontalDistance > preferredDistance + 1.2D
                    ? (this.phase >= 2 ? 0.20D : 0.15D)
                    : horizontalDistance < preferredDistance - 1.5D ? -0.12D : 0.0D;
            desiredX = dx / horizontalDistance * approach - dz / horizontalDistance * orbitSpeed * side;
            desiredZ = dz / horizontalDistance * approach + dx / horizontalDistance * orbitSpeed * side;
            if (this.crimsonRetreatTicks > 0) {
                // Physically fly away from close-range sword pressure. Smooth
                // acceleration and normal collision mean this is NOT a teleport
                // and walls still stop the boss. It remains fully damageable.
                desiredX = -dx / horizontalDistance * 0.36D
                        - dz / horizontalDistance * 0.055D * side;
                desiredZ = -dz / horizontalDistance * 0.36D
                        + dx / horizontalDistance * 0.055D * side;
                desiredY = Math.max(desiredY, 0.18D);
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

    /**
     * Raise the wings, unsheathe three iron feathers and HOLD POSITION for 12 ticks.
     * This is a readably telegraphed razor-feather volley, not crystals.
     */
    private void shootBolt(LivingEntity target, float inaccuracy, float velocity) {
        if (!(this.level instanceof ServerWorld)) return;
        this.pincerPending = false;
        this.queuedVolleyVelocity = velocity;
        this.queuedVolleyInaccuracy = inaccuracy;
        this.entityData.set(ATTACK_STATE, 1);
        this.entityData.set(ATTACK_TICKS, 23);
        this.getNavigation().stop();
        this.setDeltaMovement(Vector3d.ZERO);
        this.hasImpulse = true;
    }

    /**
     * Wing-blade pincer: two widely spaced feathers first, then three converging
     * feathers. Each release has its own visible warning and can be dodged.
     */
    private void startFeatherPincer() {
        this.pincerPending = true;
        this.entityData.set(ATTACK_STATE, 1);
        this.entityData.set(ATTACK_TICKS, 32);
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 0.58F, 1.24F);
    }

    private void sendPincerWarning() {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld)this.level;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        double x = target.getX() - this.getX();
        double z = target.getZ() - this.getZ();
        double length = Math.max(0.001D, Math.sqrt(x * x + z * z));
        double sideX = -z / length, sideZ = x / length;
        for (int side = -1; side <= 1; side += 2) {
            server.sendParticles(ParticleTypes.SMOKE,
                    this.getX() + sideX * side * 2.9D,
                    this.getY() + this.getBbHeight() * 0.55D,
                    this.getZ() + sideZ * side * 2.9D,
                    3, 0.12D, 0.2D, 0.12D, 0.01D);
            server.sendParticles(ParticleTypes.CRIT,
                    target.getX() + sideX * side * 2.0D, target.getY(0.2D),
                    target.getZ() + sideZ * side * 2.0D,
                    2, 0.12D, 0.05D, 0.12D, 0.015D);
        }
    }

    private void launchFeatherPincer(LivingEntity target, boolean followUp) {
        if (!(this.level instanceof ServerWorld)) return;
        double x = target.getX() - this.getX();
        double z = target.getZ() - this.getZ();
        double length = Math.max(0.001D, Math.sqrt(x * x + z * z));
        double sideX = -z / length, sideZ = x / length;
        int count = followUp ? 3 : 2;
        for (int i = 0; i < count; i++) {
            double fraction = i - (count - 1) / 2.0D;
            double muzzleOffset = fraction * (followUp ? 2.7D : 5.6D);
            double targetOffset = fraction * (followUp ? 0.9D : 3.8D);
            AetherBoltEntity bolt = new AetherBoltEntity(this.level, this);
            bolt.setStyle(AetherBoltEntity.FEATHER_BLADE);
            bolt.setPos(this.getX() + sideX * muzzleOffset,
                    this.getY() + this.getBbHeight() * 0.58D,
                    this.getZ() + sideZ * muzzleOffset);
            bolt.shoot(target.getX() + sideX * targetOffset - bolt.getX(),
                    target.getY(0.45D) - bolt.getY(),
                    target.getZ() + sideZ * targetOffset - bolt.getZ(),
                    followUp ? 1.36F : 1.22F, 0.0F);
            this.level.addFreshEntity(bolt);
        }
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 0.5F,
                followUp ? 1.48F : 1.20F);
    }

    /** Smoke and metallic sparks gather where the three feather blades appear. */
    private void sendVolleyWarning() {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld world = (ServerWorld)this.level;
        double cy = this.getY() + this.getBbHeight() * 0.58D;
        for (int i = 0; i < 3; i++) {
            double angle = this.tickCount * 0.12D + i * Math.PI * 2.0D / 3.0D;
            world.sendParticles(i == 1 ? ParticleTypes.SMOKE : ParticleTypes.CRIT,
                    this.getX() + Math.cos(angle) * 0.54D,
                    cy + Math.sin(angle) * 0.18D,
                    this.getZ() + Math.sin(angle) * 0.54D,
                    1, 0.02D, 0.02D, 0.02D, 0.005D);
        }
    }

    /** Fans spread perpendicular to the actual aim vector, not along world X/Z axes. */
    private void launchAstralFan(LivingEntity target, int count, float velocity,
                                float inaccuracy, boolean followUp) {
        if (!(this.level instanceof ServerWorld)) return;
        double aimX = target.getX() - this.getX();
        double aimZ = target.getZ() - this.getZ();
        double length = Math.max(0.001D, Math.sqrt(aimX * aimX + aimZ * aimZ));
        double sideX = -aimZ / length;
        double sideZ = aimX / length;
        for (int i = 0; i < count; i++) {
            double spread = (i - (count - 1) / 2.0D) * (followUp ? 0.7D : 1.05D);
            AetherBoltEntity bolt = new AetherBoltEntity(this.level, this);
            // The projectile now has a dedicated black iron FEATHER mesh.
            // Spread the muzzle across the chest/wing roots, not one crystal point.
            bolt.setStyle(AetherBoltEntity.FEATHER_BLADE);
            double muzzleOffset = (i - (count - 1) * 0.5D) * 0.22D;
            bolt.setPos(this.getX() + sideX * muzzleOffset,
                    this.getY() + this.getBbHeight() * 0.58D,
                    this.getZ() + sideZ * muzzleOffset);
            double dx = target.getX() + sideX * spread - bolt.getX();
            double dy = target.getY(0.45D) - bolt.getY();
            double dz = target.getZ() + sideZ * spread - bolt.getZ();
            bolt.shoot(dx, dy, dz, velocity, inaccuracy);
            this.level.addFreshEntity(bolt);
        }
        ServerWorld server = (ServerWorld) this.level;
        server.sendParticles(ParticleTypes.SMOKE,
                this.getX(), this.getY() + this.getBbHeight() * 0.58D, this.getZ(),
                followUp ? 13 : 10, 0.42D, 0.26D, 0.42D, 0.025D);
        server.sendParticles(ParticleTypes.CRIT,
                this.getX(), this.getY() + this.getBbHeight() * 0.58D, this.getZ(),
                8, 0.30D, 0.22D, 0.30D, 0.025D);
        if (followUp) this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 0.7F, 1.32F);
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
        this.lockDashFacing();
        this.getNavigation().stop();
        // Keep the triple dash threatening but give players 0.6s to read each direction.
        this.dashChargeTicks = this.phase >= 3 ? 12 : 15;
        this.dashHitRegistered = false;
        this.entityData.set(ATTACK_STATE, 6);
        this.entityData.set(ATTACK_TICKS, this.dashChargeTicks);
        this.setDeltaMovement(Vector3d.ZERO);
        this.hasImpulse = true;
        this.playSound(RegistryHandler.ASTRAL_DEVOURER_PULSE.get(), 0.72F, 1.12F);
    }

    /**
     * One locked aim direction for the entire telegraph and rush. Previously
     * the model kept turning to face the moving player while velocity was
     * fixed to the old aim, so the dash looked sideways/backwards.
     */
    private void lockDashFacing() {
        double horizontal = Math.sqrt(this.dashDirX * this.dashDirX
                + this.dashDirZ * this.dashDirZ);
        float yaw = (float)(Math.atan2(this.dashDirZ, this.dashDirX)
                * 180.0D / Math.PI - 90.0D);
        this.yRot = yaw;
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        this.xRot = (float)(-Math.atan2(this.dashDirY, horizontal)
                * 180.0D / Math.PI);
    }

    /** Server-side dash state machine. Movement and damage remain authoritative. */
    private boolean tickDashPattern() {
        if (this.dashChargeTicks > 0) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vector3d.ZERO);
            this.lockDashFacing();
            this.dashChargeTicks--;
            this.entityData.set(ATTACK_TICKS, this.dashChargeTicks);
            if (this.level instanceof ServerWorld && this.dashChargeTicks % 2 == 0) {
                ServerWorld server = (ServerWorld) this.level;
                double y = this.getY() + this.getBbHeight() * 0.55D;
                // A thin, stationary aiming corridor plus compressed wing
                // smoke makes the danger direction recognizable from any angle.
                server.sendParticles(ParticleTypes.SMOKE, this.getX(), y, this.getZ(),
                        5, 0.30D, 0.26D, 0.30D, 0.012D);
                for (int step = 1; step <= 9; step += 2) {
                    double d = step * 1.0D;
                    server.sendParticles(ParticleTypes.CRIT,
                            this.getX() + this.dashDirX * d,
                            y + this.dashDirY * d,
                            this.getZ() + this.dashDirZ * d,
                            1, 0.05D, 0.05D, 0.05D, 0.005D);
                }
            }
            if (this.dashChargeTicks == 0) this.beginDashBurst();
            return true;
        }

        if (this.dashTicks > 0) {
            this.lockDashFacing();
            double speed = this.phase >= 3 ? 1.08D : 0.94D;
            this.setDeltaMovement(this.dashDirX * speed, this.dashDirY * speed,
                    this.dashDirZ * speed);
            this.hasImpulse = true;
            if (this.level instanceof ServerWorld) {
                ServerWorld server = (ServerWorld) this.level;
                server.sendParticles(ParticleTypes.SMOKE,
                        this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                        6, 0.35D, 0.34D, 0.35D, 0.022D);
                server.sendParticles(ParticleTypes.CRIT,
                        this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                        3, 0.25D, 0.20D, 0.25D, 0.015D);
            }
            if (!this.dashHitRegistered) this.checkDashImpact();
            this.dashTicks--;
            this.entityData.set(ATTACK_TICKS, this.dashTicks);
            if (this.dashTicks == 0) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.32D));
                this.hasImpulse = true;
                if (this.dashesRemaining > 0) {
                    this.dashRecoveryTicks = 6;
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
            this.entityData.set(ATTACK_TICKS, 42);
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
            this.playSound(RegistryHandler.ASTRAL_HURT.get(), 0.72F, 0.9F);
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
        double speed = this.phase >= 3 ? 1.08D : 0.94D;
        this.setDeltaMovement(this.dashDirX * speed, this.dashDirY * speed,
                this.dashDirZ * speed);
        this.hasImpulse = true;
        this.playSound(RegistryHandler.ASTRAL_DASH.get(), 1.0F, this.phase >= 3 ? 1.06F : 0.91F);
    }

    private void checkDashImpact() {
        // Swept hitbox includes where the boss will travel this tick. The old
        // hit test checked only its previous position and could miss players
        // completely when the burst was moving roughly one block per tick.
        AxisAlignedBB hitBox = this.getBoundingBox()
                .expandTowards(this.getDeltaMovement()).inflate(0.38D, 0.26D, 0.38D);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(
                PlayerEntity.class, hitBox, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            if (!hitBox.intersects(player.getBoundingBox())) continue;
            Vector3d origin = this.position().add(0.0D, this.getBbHeight() * 0.52D, 0.0D);
            Vector3d victim = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
            if (this.level.clip(new RayTraceContext(origin, victim,
                    RayTraceContext.BlockMode.COLLIDER,
                    RayTraceContext.FluidMode.NONE, this)).getType() == RayTraceResult.Type.BLOCK) {
                continue; // Do not dash-hit a player THROUGH a wall.
            }
            if (!player.hurt(DamageSource.mobAttack(this), this.phase >= 3 ? 12.0F : 10.0F))
                continue;
            player.setDeltaMovement(player.getDeltaMovement().add(
                    this.dashDirX * 0.62D, 0.26D + this.dashDirY * 0.22D, this.dashDirZ * 0.62D));
            player.hurtMarked = true;
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 20, 0, true, true));
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

    /**
     * Locks a visible line for more than a second. Solid blocks stop the laser,
     * so taking cover is as reliable as stepping away from its centerline.
     */
    private void prepareAstralRay(LivingEntity target) {
        Vector3d center = this.position().add(0.0D, this.getBbHeight() * 0.58D, 0.0D);
        Vector3d aim = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        Vector3d toward = aim.subtract(center);
        if (toward.lengthSqr() < 0.001D) return;
        Vector3d origin = center.add(toward.normalize().scale(0.38D));
        if (toward.lengthSqr() > 32.0D * 32.0D) {
            aim = origin.add(toward.normalize().scale(32.0D));
        }
        BlockRayTraceResult blocked = this.level.clip(new RayTraceContext(
                origin, aim, RayTraceContext.BlockMode.COLLIDER,
                RayTraceContext.FluidMode.NONE, this));
        if (blocked.getType() == RayTraceResult.Type.BLOCK) aim = blocked.getLocation();
        this.beamEndX = aim.x;
        this.beamEndY = aim.y;
        this.beamEndZ = aim.z;
        this.entityData.set(BEAM_X, (float)this.beamEndX);
        this.entityData.set(BEAM_Y, (float)this.beamEndY);
        this.entityData.set(BEAM_Z, (float)this.beamEndZ);
        // 26 ticks (1.3 seconds) of visible warning before 20 ticks of dangerous beam.
        this.beamLastHitTick.clear();
        this.beamHitCount.clear();
        this.beamChargeTicks = 26;
        this.entityData.set(ATTACK_STATE, 2);
        this.entityData.set(ATTACK_TICKS, this.beamChargeTicks);
        this.setDeltaMovement(Vector3d.ZERO);
        this.hasImpulse = true;
        this.playSound(RegistryHandler.ASTRAL_LASER_CHARGE.get(), 1.05F, 1.0F);
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
            // Contact is checked throughout the firing window, with an 11-tick
            // cooldown and a strict cap of two damage instances per player.
            this.damageAlongAstralRay();
            if (this.beamTicks % 2 == 0) this.sendAstralRayParticles(1.0D, true);
            this.beamTicks--;
            this.entityData.set(ATTACK_TICKS, this.beamTicks);
            if (this.beamTicks == 0) {
                this.entityData.set(ATTACK_STATE, 0);
                this.beamLastHitTick.clear();
                this.beamHitCount.clear();
            }
            return true;
        }
        return false;
    }

    private void beginAstralRay() {
        this.beamTicks = 20;
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
        this.playSound(RegistryHandler.ASTRAL_LASER_FIRE.get(), 1.1F, this.phase >= 3 ? 1.05F : 0.94F);
    }

    /** Sparse impact sparks complement the continuous client-rendered beam without flooding the network. */
    private void sendAstralRayParticles(double reach, boolean firing) {
        if (!(this.level instanceof ServerWorld)) return;
        ServerWorld server = (ServerWorld) this.level;
        Vector3d muzzle = this.getBeamOrigin();
        double sx = muzzle.x;
        double sy = muzzle.y;
        double sz = muzzle.z;
        double dx = this.beamEndX - sx;
        double dy = this.beamEndY - sy;
        double dz = this.beamEndZ - sz;
        for (int i = 0; i <= 12; i++) {
            double progress = i / 12.0D;
            if (progress > reach) continue;
            double px = sx + dx * progress;
            double py = sy + dy * progress;
            double pz = sz + dz * progress;
            if (firing) {
                server.sendParticles(ParticleTypes.END_ROD,
                        px, py, pz, 1, 0.10D, 0.10D, 0.10D, 0.015D);
                if (i % 3 == 0) server.sendParticles(ParticleTypes.DRAGON_BREATH,
                        px, py, pz, 1, 0.12D, 0.12D, 0.12D, 0.01D);
            } else if (i % 2 == 0) {
                server.sendParticles(ParticleTypes.PORTAL,
                        px, py, pz, 1, 0.06D, 0.06D, 0.06D, 0.0D);
            }
        }
    }

    /**
     * Aimed, fixed beam: narrow centerline, up to two well-spaced hits per player.
     * The visible halo is cosmetic and does not extend the damaging hitbox.
     */
    private void damageAlongAstralRay() {
        Vector3d muzzle = this.getBeamOrigin();
        double sx = muzzle.x;
        double sy = muzzle.y;
        double sz = muzzle.z;
        double dx = this.beamEndX - sx;
        double dy = this.beamEndY - sy;
        double dz = this.beamEndZ - sz;
        double lengthSq = dx * dx + dy * dy + dz * dz;
        if (lengthSq < 0.01D) return;
        final double hitRadius = 0.78D;
        AxisAlignedBB area = new AxisAlignedBB(
                Math.min(sx, beamEndX), Math.min(sy, beamEndY), Math.min(sz, beamEndZ),
                Math.max(sx, beamEndX), Math.max(sy, beamEndY), Math.max(sz, beamEndZ))
                .inflate(hitRadius + 0.55D);
        List<PlayerEntity> players = this.level.getEntitiesOfClass(PlayerEntity.class, area, PlayerEntity::isAlive);
        for (PlayerEntity player : players) {
            double px = player.getX() - sx;
            double py = player.getY(0.55D) - sy;
            double pz = player.getZ() - sz;
            double t = (px * dx + py * dy + pz * dz) / lengthSq;
            if (t < 0.0D || t > 1.0D) continue;
            double ex = player.getX() - (sx + dx * t);
            double ey = player.getY(0.55D) - (sy + dy * t);
            double ez = player.getZ() - (sz + dz * t);
            if (ex * ex + ey * ey + ez * ez > hitRadius * hitRadius) continue;
            UUID id = player.getUUID();
            if (this.beamHitCount.getOrDefault(id, 0) >= 2
                    || this.tickCount - this.beamLastHitTick.getOrDefault(id, -1000) < 11)
                continue;
            // Two successful hits total 12 / 14 health before other reductions,
            // even when the player remains inside the beam for its full duration.
            if (player.hurt(DamageSource.indirectMagic(this, this),
                    this.phase >= 3 ? 7.0F : 6.0F)) {
                this.beamLastHitTick.put(id, this.tickCount);
                this.beamHitCount.put(id, this.beamHitCount.getOrDefault(id, 0) + 1);
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 22, 0, true, true));
                if (this.level instanceof ServerWorld) {
                    ((ServerWorld)this.level).sendParticles(ParticleTypes.CRIT,
                            player.getX(), player.getY(0.55D), player.getZ(),
                            18, 0.28D, 0.30D, 0.28D, 0.06D);
                    ((ServerWorld)this.level).sendParticles(ParticleTypes.DRAGON_BREATH,
                            player.getX(), player.getY(0.55D), player.getZ(),
                            12, 0.25D, 0.30D, 0.25D, 0.02D);
                }
            }
        }
    }

    private void tickAttackAnimation() {
        int ticks = this.entityData.get(ATTACK_TICKS);
        if (ticks > 0) {
            int nextTicks = ticks - 1;
            this.entityData.set(ATTACK_TICKS, nextTicks);
            int state = this.entityData.get(ATTACK_STATE);
            if (state == 1 && this.pincerPending) {
                if (nextTicks > 8 && nextTicks % 3 == 0) this.sendPincerWarning();
                if (nextTicks == 20 || nextTicks == 8) {
                    LivingEntity target = this.getTarget();
                    if (target != null && target.isAlive())
                        this.launchFeatherPincer(target, nextTicks == 8);
                }
            } else if (state == 1) {
                if (nextTicks > 11 && nextTicks % 2 == 0) {
                    this.sendVolleyWarning();
                }
                if (nextTicks == 11 || (nextTicks == 4 && this.phase >= 2)) {
                    LivingEntity volleyTarget = this.getTarget();
                    if (volleyTarget != null && volleyTarget.isAlive()) {
                        boolean followUp = nextTicks == 4;
                        this.launchAstralFan(volleyTarget,
                                followUp ? (this.phase == 2 ? 3 : 5)
                                        : (this.phase == 1 ? 3 : (this.phase == 2 ? 4 : 5)),
                                followUp ? (this.phase == 2 ? 1.28F : 1.38F)
                                        : this.queuedVolleyVelocity,
                                followUp ? 0.25F : this.queuedVolleyInaccuracy,
                                followUp);
                    }
                }
            }
            if (state == 4 || state == 5) this.sendAreaAttackTelegraph(state, nextTicks);
            if (state == 4 && nextTicks == 1 && this.gravityDamagePending) {
                this.resolveGravityWell();
            } else if (state == 5 && nextTicks == 1 && this.novaDamagePending) {
                this.resolveCosmicNova();
            }
            if (nextTicks == 0) {
                this.entityData.set(ATTACK_STATE, 0);
                this.pincerPending = false;
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
        LivingEntity target = this.getTarget();
        double markerY = target != null && target.isAlive()
                ? target.getY() + 0.12D : this.getY() - 2.4D;
        if (remainingTicks % 2 != 0) return;
        for (int i = 0; i < 20; i++) {
            double angle = Math.PI * 2.0D * i / 20.0D + this.tickCount * 0.025D;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            server.sendParticles(state == 4 ? ParticleTypes.PORTAL : ParticleTypes.DRAGON_BREATH,
                    x, markerY, z, 1, 0.015D, 0.025D, 0.015D, 0.0D);
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
        this.playSound(RegistryHandler.ASTRAL_NOVA.get(), 0.80F, 0.92F);
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
            if (player.hurt(DamageSource.mobAttack(this), 11.0F)) {
                player.setDeltaMovement(player.getDeltaMovement().add(
                        dx / length * 0.88D, 0.38D, dz / length * 0.88D));
                player.hurtMarked = true;
                player.addEffect(new EffectInstance(Effects.BLINDNESS, 16, 0, true, true));
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 28, 0, true, true));
            }
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
        this.playSound(RegistryHandler.ASTRAL_NOVA.get(), 1.06F, 0.73F);
    }

    /**
     * The boss's three mirror images are real tracking entities, not a second
     * fully opaque render drawn only while the boss itself is visible.
     *
     * Their own renderer makes them increasingly opaque as health drops. The
     * mirror positions are updated relative to the PLAYER, Brain of Cthulhu
     * style, and fake hits do not punish the player.
     */
    private void summonMirage() {
        if (!(this.level instanceof ServerWorld) || !this.activeMirages.isEmpty()) return;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;
        ServerWorld server = (ServerWorld)this.level;
        double dx = this.getX() - target.getX();
        double dz = this.getZ() - target.getZ();
        double radius = Math.max(4.3D, Math.min(10.0D, Math.sqrt(dx * dx + dz * dz)));
        double angle = Math.atan2(dz, dx);
        for (int i = 0; i < 3; i++) {
            BossMirageEntity fake = RegistryHandler.BOSS_MIRAGE.get().create(this.level);
            if (fake == null) continue;
            fake.setOwner(this);
            fake.setCopySlot(i);
            double theta = angle + (i + 1) * (Math.PI * 0.5D);
            fake.setPos(target.getX() + Math.cos(theta) * radius,
                    this.getY(), target.getZ() + Math.sin(theta) * radius);
            fake.yRot = this.yRot;
            if (this.level.addFreshEntity(fake)) this.activeMirages.add(fake.getId());
        }
        this.entityData.set(MIRAGE_ID,
                this.activeMirages.isEmpty() ? -1 : this.activeMirages.get(0));
        if (!this.activeMirages.isEmpty()) {
            server.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 1.2D, this.getZ(),
                    18, 0.65D, 0.50D, 0.65D, 0.035D);
            this.playSound(RegistryHandler.ASTRAL_MIRAGE.get(), 0.66F, 1.04F);
        }
    }

    private void dispelMirage() {
        if (this.activeMirages.isEmpty()) return;
        List<Integer> copies = new ArrayList<>(this.activeMirages);
        this.activeMirages.clear();
        this.entityData.set(MIRAGE_ID, -1);
        for (int id : copies) {
            net.minecraft.entity.Entity fake = this.level.getEntity(id);
            if (fake instanceof BossMirageEntity) ((BossMirageEntity)fake).expire();
        }
    }

    /** Phase-bound reinforcements on the arena floor, capped so they never snowball forever. */
    private void summonAstralServants(LivingEntity target) {
        if (!(this.level instanceof ServerWorld)) return;
        AxisAlignedBB area = this.getBoundingBox().inflate(22.0D);
        List<RiftStalkerEntity> existing = this.level.getEntitiesOfClass(
                RiftStalkerEntity.class, area, RiftStalkerEntity::isAlive);
        int remaining = (this.phase >= 3 ? 5 : 3) - existing.size();
        if (remaining <= 0) return;
        int count = Math.min(remaining, this.phase >= 3 ? 2 : 1);
        ServerWorld server = (ServerWorld) this.level;
        for (int i = 0; i < count; i++) {
            double angle = this.tickCount * 0.32D + i * Math.PI;
            int x = (int)Math.floor(target.getX() + Math.cos(angle) * 5.0D);
            int z = (int)Math.floor(target.getZ() + Math.sin(angle) * 5.0D);
            BlockPos spawn = this.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, target.getY(), z));
            if (!this.level.getFluidState(spawn).isEmpty() || !this.level.getBlockState(spawn).isAir()) continue;
            RiftStalkerEntity servant = RegistryHandler.RIFT_STALKER.get().create(this.level);
            if (servant == null) continue;
            servant.moveTo(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                    this.yRot, 0.0F);
            servant.setTarget(target);
            this.level.addFreshEntity(servant);
            server.sendParticles(ParticleTypes.PORTAL,
                    servant.getX(), servant.getY() + 0.7D, servant.getZ(),
                    24, 0.45D, 0.5D, 0.45D, 0.06D);
        }
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    /** A fully dodged dash combo briefly exposes the Devourer's core. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.entityData.get(ATTACK_STATE) == 8) amount *= 1.35F;
        boolean damaged = super.hurt(source, amount);
        if (damaged && !this.level.isClientSide && source.getEntity() instanceof PlayerEntity
                && source.getDirectEntity() == source.getEntity())
            this.noteMeleePressure((PlayerEntity)source.getEntity());
        // Receiving damage should NOT instantly erase all three illusions.
        // The real boss is revealed by its health bar and hit response.
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
        if (target == null || !this.getBoundingBox().inflate(0.48D, 0.32D, 0.48D).intersects(target.getBoundingBox())) {
            return false;
        }
        return super.doHurtTarget(target);
    }

    @Override
    protected net.minecraft.util.SoundEvent getAmbientSound() {
        // Keep the slow ambient layer out of charged attacks and close-range battles.
        return this.entityData.get(ATTACK_STATE) == 0 && this.getTarget() == null
                ? RegistryHandler.ASTRAL_AMBIENT.get() : null;
    }

    @Override
    protected net.minecraft.util.SoundEvent getHurtSound(DamageSource source) {
        return RegistryHandler.ASTRAL_HURT.get();
    }

    @Override
    protected net.minecraft.util.SoundEvent getDeathSound() {
        return RegistryHandler.ASTRAL_DEATH.get();
    }
}
