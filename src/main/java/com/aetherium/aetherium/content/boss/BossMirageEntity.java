package com.aetherium.aetherium.content.boss;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Server-owned, pickable decoy with its OWN renderer and tracking position.
 *
 * Winged Executioner: 3 harmless mirror images around the target, following
 * the real boss's movements like the expert-mode Brain of Cthulhu illusions.
 * Guardian: keeps its original single orbiting trap and hit consequence.
 */
public class BossMirageEntity extends Entity {
    private static final DataParameter<Integer> OWNER_ID =
            EntityDataManager.defineId(BossMirageEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> COPY_SLOT =
            EntityDataManager.defineId(BossMirageEntity.class, DataSerializers.INT);
    private boolean expiring;

    public BossMirageEntity(EntityType<? extends BossMirageEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public void setOwner(Entity boss) {
        this.entityData.set(OWNER_ID, boss.getId());
    }

    public void setCopySlot(int slot) {
        this.entityData.set(COPY_SLOT, Math.max(0, Math.min(2, slot)));
    }

    public int getCopySlot() {
        return this.entityData.get(COPY_SLOT);
    }

    public Entity getBoss() {
        return this.level.getEntity(this.entityData.get(OWNER_ID));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, -1);
        this.entityData.define(COPY_SLOT, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        this.entityData.set(OWNER_ID, nbt.getInt("MirageBossId"));
        this.entityData.set(COPY_SLOT, nbt.getInt("CopySlot"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("MirageBossId", this.entityData.get(OWNER_ID));
        nbt.putInt("CopySlot", this.getCopySlot());
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) return;

        Entity owner = getBoss();
        int lifetime = owner instanceof AstralDevourerEntity ? 170 : 120;
        if (owner == null || !owner.isAlive() || this.tickCount >= lifetime) {
            this.expire();
            return;
        }

        if (owner instanceof AstralDevourerEntity) {
            LivingEntity target = ((AstralDevourerEntity)owner).getTarget();
            if (target == null || !target.isAlive()) {
                this.expire();
                return;
            }
            // Mirror positions around the PLAYER, not an orbit around the boss.
            // As the real boss approaches and dashes, all copies approach in
            // corresponding quadrants, retaining one plausible real target.
            double dx = owner.getX() - target.getX();
            double dz = owner.getZ() - target.getZ();
            double radius = Math.max(4.3D, Math.min(10.0D,
                    Math.sqrt(dx * dx + dz * dz)));
            double theta = Math.atan2(dz, dx)
                    + (this.getCopySlot() + 1) * (Math.PI * 0.5D);
            double bob = Math.sin(this.tickCount * 0.13D + this.getCopySlot()) * 0.20D;
            this.setPos(target.getX() + Math.cos(theta) * radius,
                    owner.getY() + bob,
                    target.getZ() + Math.sin(theta) * radius);
            // Same apparent facing as the boss, including locked dash direction.
            this.yRot = owner.yRot;
            this.xRot = owner.xRot;
        } else {
            // Keep the Guardian's original single decoy movement intact.
            double angle = this.tickCount * 0.04D + owner.getId() * 0.63D;
            this.setPos(owner.getX() + Math.cos(angle) * 3.3D, owner.getY(),
                    owner.getZ() + Math.sin(angle) * 3.3D);
            this.yRot = owner.yRot;
        }
        this.hasImpulse = true;
        // Sparse entrance sparkle only; constant portal clouds gave the real
        // creature away and made the copies look like a separate attack.
        if (this.level instanceof ServerWorld && this.tickCount <= 12
                && this.tickCount % 3 == 0) {
            ((ServerWorld)this.level).sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + this.getBbHeight() * 0.52D,
                    this.getZ(), 3, 0.18D, 0.28D, 0.18D, 0.012D);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level.isClientSide) return true;
        Entity owner = this.getBoss();

        // Executioner copies are visual feints, NOT damage traps. Hitting one
        // merely reveals it with a brief dissolving smoke puff.
        if (owner instanceof RiftGuardianEntity) {
            Entity attacker = source.getEntity();
            if (attacker instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)attacker;
                player.hurt(DamageSource.MAGIC, 2.0F);
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 24, 0));
            }
        }

        if (this.level instanceof ServerWorld) {
            ((ServerWorld)this.level).sendParticles(
                    owner instanceof AstralDevourerEntity
                            ? ParticleTypes.SMOKE : ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    22, 0.60D, 0.8D, 0.60D, 0.055D);
        }
        if (owner instanceof AstralDevourerEntity) {
            this.playSound(RegistryHandler.ASTRAL_MIRAGE.get(), 0.4F, 1.32F);
        } else if (owner instanceof RiftGuardianEntity) {
            this.playSound(RegistryHandler.GUARDIAN_MIRAGE.get(), 0.65F, 1.21F);
        }
        this.expire();
        return true;
    }

    public void expire() {
        if (this.level.isClientSide || this.expiring) return;
        this.expiring = true;
        Entity owner = this.getBoss();
        if (owner instanceof AstralDevourerEntity) {
            ((AstralDevourerEntity)owner).onMirageDispelled(this.getId());
        } else if (owner instanceof RiftGuardianEntity) {
            ((RiftGuardianEntity)owner).onMirageDispelled(this.getId());
        }
        this.remove();
    }
}
