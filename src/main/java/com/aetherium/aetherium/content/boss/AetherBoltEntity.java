package com.aetherium.aetherium.content.boss;

import com.aetherium.aetherium.registry.RegistryHandler;
import com.aetherium.aetherium.content.entity.RiftStalkerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Boss projectile with actual combat-specific silhouettes.
 *
 * Style 1 = Executer black iron cutting feather (red spine).
 * Style 2 = Guardian compressed-air crescent (pale white/slate).
 *
 * Entity and registry IDs remain aetherium:aether_bolt for compatibility, but
 * no crystal or fragment item is drawn: AetherBoltRenderer uses mesh geometry.
 */
public class AetherBoltEntity extends ProjectileItemEntity {
    public static final int FEATHER_BLADE = 1;
    public static final int WIND_SLICE = 2;
    private static final DataParameter<Integer> STYLE =
            EntityDataManager.defineId(AetherBoltEntity.class, DataSerializers.INT);

    public AetherBoltEntity(EntityType<? extends AetherBoltEntity> type, World world) {
        super(type, world);
    }

    public AetherBoltEntity(World world, LivingEntity shooter) {
        super(RegistryHandler.AETHER_BOLT.get(), shooter, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STYLE, FEATHER_BLADE);
    }

    public void setStyle(int style) {
        this.entityData.set(STYLE, style == WIND_SLICE ? WIND_SLICE : FEATHER_BLADE);
    }

    public int getStyle() {
        return this.entityData.get(STYLE);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected Item getDefaultItem() {
        // ProjectileItemEntity still needs a legacy default item internally;
        // the actual renderer draws mesh feathers/wind, never this sprite.
        return RegistryHandler.ASTRAL_FRAGMENT.get();
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    protected boolean canHitEntity(Entity candidate) {
        if (this.getOwner() instanceof AstralDevourerEntity
                || this.getOwner() instanceof RiftGuardianEntity) {
            if (candidate instanceof RiftStalkerEntity) return false;
        }
        if (candidate instanceof BossMirageEntity
                && ((BossMirageEntity)candidate).getBoss() == this.getOwner()) {
            return false;
        }
        return super.canHitEntity(candidate);
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult result) {
        super.onHitEntity(result);
        if (!this.level.isClientSide && result.getEntity() instanceof BossMirageEntity
                && this.getOwner() instanceof PlayerEntity) {
            ((BossMirageEntity)result.getEntity()).hurt(
                    DamageSource.indirectMagic(this, (LivingEntity)this.getOwner()), 1.0F);
        }
        if (!this.level.isClientSide && result.getEntity() instanceof LivingEntity
                && result.getEntity() != this.getOwner()) {
            LivingEntity victim = (LivingEntity)result.getEntity();
            LivingEntity owner = this.getOwner() instanceof LivingEntity
                    ? (LivingEntity)this.getOwner() : null;
            DamageSource source = owner == null ? DamageSource.MAGIC
                    : DamageSource.indirectMagic(this, owner);
            if (victim.hurt(source, owner instanceof PlayerEntity ? 4.0F : 8.0F)) {
                victim.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 50, 0, true, true));
                // The victim's regular hurt audio is sufficient. Avoid the
                // previous electronic sword-hit sound layered over each bolt.
                if (this.level instanceof ServerWorld) {
                    ServerWorld server = (ServerWorld)this.level;
                    server.sendParticles(this.getStyle() == WIND_SLICE
                                    ? ParticleTypes.CLOUD : ParticleTypes.CRIT,
                            victim.getX(), victim.getY(0.55D), victim.getZ(),
                            12, 0.24D, 0.35D, 0.24D, 0.042D);
                }
            }
        }
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte)3);
            this.remove();
        }
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult result) {
        super.onHitBlock(result);
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte)3);
            this.remove();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            if (this.getStyle() == WIND_SLICE) {
                this.level.addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(),
                        -this.getDeltaMovement().x * 0.05D,
                        -this.getDeltaMovement().y * 0.05D,
                        -this.getDeltaMovement().z * 0.05D);
                if (this.tickCount % 3 == 0) {
                    this.level.addParticle(ParticleTypes.CRIT,
                            this.getX(), this.getY(), this.getZ(),
                            0.0D, 0.01D, 0.0D);
                }
            } else {
                this.level.addParticle(ParticleTypes.SMOKE,
                        this.getX(), this.getY(), this.getZ(),
                        -this.getDeltaMovement().x * 0.03D,
                        -this.getDeltaMovement().y * 0.03D,
                        -this.getDeltaMovement().z * 0.03D);
                if (this.tickCount % 2 == 0) {
                    this.level.addParticle(ParticleTypes.CRIT,
                            this.getX(), this.getY(), this.getZ(), 0.0D, 0.01D, 0.0D);
                }
            }
        } else if (this.level instanceof ServerWorld && this.tickCount % 3 == 0) {
            // A short server trail remains visible if client interpolation
            // packets arrive sparsely, but does not overwhelm boss telegraphs.
            ((ServerWorld)this.level).sendParticles(
                    this.getStyle() == WIND_SLICE ? ParticleTypes.CLOUD : ParticleTypes.SMOKE,
                    this.getX(), this.getY(), this.getZ(),
                    2, 0.09D, 0.09D, 0.09D, 0.002D);
        }
        if (!this.level.isClientSide && this.tickCount > 80) this.remove();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3 && this.level.isClientSide) {
            // Never invoke ProjectileItemEntity's vanilla shard item event
            // for this impact: only smoke/wind mesh fragments are intended.
            for (int i = 0; i < 12; i++) {
                this.level.addParticle(
                        this.getStyle() == WIND_SLICE
                                ? ParticleTypes.CLOUD : ParticleTypes.CRIT,
                        this.getX(), this.getY(), this.getZ(),
                        (this.random.nextDouble() - 0.5D) * 0.26D,
                        (this.random.nextDouble() - 0.5D) * 0.26D,
                        (this.random.nextDouble() - 0.5D) * 0.26D);
            }
        } else if (id != 3) {
            super.handleEntityEvent(id);
        }
    }
}
