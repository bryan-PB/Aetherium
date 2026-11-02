package com.aetherium.aetherium.content.boss;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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

    @Override
    protected Item getDefaultItem() {
        return RegistryHandler.ASTRAL_FRAGMENT.get();
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    protected void onHitEntity(EntityRayTraceResult result) {
        super.onHitEntity(result);
        if (!this.level.isClientSide && result.getEntity() instanceof LivingEntity
                && result.getEntity() != this.getOwner()) {
            LivingEntity victim = (LivingEntity) result.getEntity();
            LivingEntity owner = this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null;
            DamageSource source = owner == null ? DamageSource.MAGIC : DamageSource.indirectMagic(this, owner);
            victim.hurt(source, 8.0F);
            victim.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 50, 0, true, true));
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
            for (int i = 0; i < 3; i++) {
                double spreadX = (this.random.nextDouble() - 0.5D) * 0.09D;
                double spreadY = (this.random.nextDouble() - 0.5D) * 0.09D;
                double spreadZ = (this.random.nextDouble() - 0.5D) * 0.09D;
                this.level.addParticle(ParticleTypes.END_ROD,
                        this.getX() + spreadX, this.getY() + spreadY, this.getZ() + spreadZ,
                        -this.getDeltaMovement().x * 0.045D,
                        -this.getDeltaMovement().y * 0.045D,
                        -this.getDeltaMovement().z * 0.045D);
            }
            if (this.tickCount % 2 == 0) {
                this.level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY(), this.getZ(),
                        0.0D, 0.015D, 0.0D);
                this.level.addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(),
                        0.0D, 0.0D, 0.0D);
            }
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
