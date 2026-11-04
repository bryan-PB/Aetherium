package com.aetherium.aetherium.content.boss;

import com.aetherium.aetherium.registry.RegistryHandler;
import com.aetherium.aetherium.content.entity.RiftStalkerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.IPacket;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

/** Small, low-cost magic projectile used by the final boss. */
public class AetherBoltEntity extends ProjectileItemEntity {
    public AetherBoltEntity(EntityType<? extends AetherBoltEntity> type, World world) {
        super(type, world);
    }

    public AetherBoltEntity(World world, LivingEntity shooter) {
        super(RegistryHandler.AETHER_BOLT.get(), shooter, world);
    }

    /** Without this Forge spawn packet, custom item projectiles may never appear client-side. */
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected Item getDefaultItem() {
        // Keep the default constant. Rift shots set their item explicitly so the sprite
        // is synchronized even when the client has not resolved the projectile owner.
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
            if (candidate instanceof RiftStalkerEntity) {
                // Bosses should not have their shots absorbed by their summoned reinforcements.
                return false;
            }
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
            // Player-fired astral bolts can uncover decoys just like a melee hit.
            ((BossMirageEntity)result.getEntity()).hurt(
                    DamageSource.indirectMagic(this, (LivingEntity)this.getOwner()), 1.0F);
        }
        if (!this.level.isClientSide && result.getEntity() instanceof LivingEntity
                && result.getEntity() != this.getOwner()) {
            LivingEntity victim = (LivingEntity) result.getEntity();
            LivingEntity owner = this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null;
            DamageSource source = owner == null ? DamageSource.MAGIC : DamageSource.indirectMagic(this, owner);
            if (victim.hurt(source, this.getOwner() instanceof PlayerEntity ? 4.0F : 8.0F)) {
                // Only successful hits make an impact noise or inflict slow.
                // A dense boss volley therefore does not play eight overlapping impact clips.
                victim.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 50, 0, true, true));
                this.playSound(RegistryHandler.AETHERIUM_BLADE_HIT.get(), 0.52F,
                        this.getOwner() instanceof RiftGuardianEntity ? 0.82F : 1.25F);
                if (this.level instanceof ServerWorld) {
                    ServerWorld server = (ServerWorld) this.level;
                    server.sendParticles(ParticleTypes.END_ROD, victim.getX(), victim.getY(0.55D), victim.getZ(),
                            14, 0.25D, 0.35D, 0.25D, 0.04D);
                    server.sendParticles(ParticleTypes.DRAGON_BREATH, victim.getX(), victim.getY(0.55D), victim.getZ(),
                            10, 0.22D, 0.30D, 0.22D, 0.025D);
                }
            }
        }
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte) 3);
            this.remove();
        }
    }

    @Override
    protected void onHitBlock(BlockRayTraceResult result) {
        super.onHitBlock(result);
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte) 3);
            this.remove();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            // A bright three-layer trail makes the moving bolt readable even at a distance.
            for (int i = 0; i < 4; i++) {
                double spreadX = (this.random.nextDouble() - 0.5D) * 0.09D;
                double spreadY = (this.random.nextDouble() - 0.5D) * 0.09D;
                double spreadZ = (this.random.nextDouble() - 0.5D) * 0.09D;
                this.level.addParticle(ParticleTypes.END_ROD,
                        this.getX() + spreadX, this.getY() + spreadY, this.getZ() + spreadZ,
                        -this.getDeltaMovement().x * 0.035D,
                        -this.getDeltaMovement().y * 0.035D,
                        -this.getDeltaMovement().z * 0.035D);
                this.level.addParticle(ParticleTypes.DRAGON_BREATH,
                        this.getX() + spreadX * 0.5D, this.getY() + spreadY * 0.5D, this.getZ() + spreadZ * 0.5D,
                        0.0D, 0.005D, 0.0D);
            }
            if (this.tickCount % 2 == 0) {
                this.level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(),
                        0.0D, 0.015D, 0.0D);
                this.level.addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(),
                        0.0D, 0.0D, 0.0D);
            }
        } else if (this.level instanceof ServerWorld && this.tickCount % 2 == 0) {
            // Send a compact server-side trail as well: this remains visible to tracking clients
            // even when local entity particle interpolation is sparse or the client misses a tick.
            ServerWorld server = (ServerWorld) this.level;
            double trailY = this.getY() + this.getBbHeight() * 0.5D;
            server.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), trailY, this.getZ(), 2, 0.035D, 0.035D, 0.035D, 0.005D);
            server.sendParticles(ParticleTypes.DRAGON_BREATH,
                    this.getX(), trailY, this.getZ(), 2, 0.045D, 0.045D, 0.045D, 0.002D);
            server.sendParticles(ParticleTypes.PORTAL,
                    this.getX(), trailY, this.getZ(), 1, 0.015D, 0.015D, 0.015D, 0.0D);
        }
        if (!this.level.isClientSide && this.tickCount > 80) {
            this.remove();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 3 && this.level.isClientSide) {
            for (int i = 0; i < 16; i++) {
                double dx = (this.random.nextDouble() - 0.5D) * 0.32D;
                double dy = (this.random.nextDouble() - 0.5D) * 0.32D;
                double dz = (this.random.nextDouble() - 0.5D) * 0.32D;
                this.level.addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(),
                        dx, dy, dz);
            }
        }
    }
}
