package com.aetherium.aetherium.content.boss;

import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Server-owned, attackable illusion hitbox. The owner's boss renderer draws a
 * matching second model at this entity's networked position. No boss AI, loot or bar.
 */
public class BossMirageEntity extends Entity {
    private static final DataParameter<Integer> OWNER_ID =
            EntityDataManager.defineId(BossMirageEntity.class, DataSerializers.INT);

    public BossMirageEntity(EntityType<? extends BossMirageEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public void setOwner(Entity boss) {
        this.entityData.set(OWNER_ID, boss.getId());
    }

    public Entity getBoss() {
        return this.level.getEntity(this.entityData.get(OWNER_ID));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, -1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT nbt) {
        this.entityData.set(OWNER_ID, nbt.getInt("MirageBossId"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT nbt) {
        nbt.putInt("MirageBossId", this.entityData.get(OWNER_ID));
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
        if (owner == null || !owner.isAlive() || this.tickCount >= 120) {
            this.expire();
            return;
        }

        // Hover in a readable mirrored orbit of the real boss instead of staying still.
        double angle = this.tickCount * 0.04D + owner.getId() * 0.63D;
        double radius = owner instanceof AstralDevourerEntity ? 4.2D : 3.3D;
        double height = owner instanceof AstralDevourerEntity
                ? Math.sin(this.tickCount * 0.11D) * 0.35D : 0.0D;
        this.setPos(owner.getX() + Math.cos(angle) * radius,
                owner.getY() + height,
                owner.getZ() + Math.sin(angle) * radius);
        this.hasImpulse = true;

        if (this.level instanceof ServerWorld && this.tickCount % 10 == 0) {
            ((ServerWorld)this.level).sendParticles(ParticleTypes.PORTAL,
                    this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                    6, 0.42D, 0.7D, 0.42D, 0.03D);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level.isClientSide) return true;
        Entity attacker = source.getEntity();
        if (attacker instanceof PlayerEntity) {
            // Fake target: a small penalty, never lethal by design under normal armor.
            PlayerEntity player = (PlayerEntity)attacker;
            player.hurt(DamageSource.MAGIC, 2.0F);
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 24, 0));
        }
        if (this.level instanceof ServerWorld) {
            ((ServerWorld)this.level).sendParticles(ParticleTypes.PORTAL,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    28, 0.65D, 0.8D, 0.65D, 0.09D);
        }
        Entity owner = this.getBoss();
        if (owner instanceof AstralDevourerEntity) {
            this.playSound(RegistryHandler.ASTRAL_MIRAGE.get(), 0.65F, 1.27F);
        } else if (owner instanceof RiftGuardianEntity) {
            this.playSound(RegistryHandler.GUARDIAN_MIRAGE.get(), 0.65F, 1.21F);
        }
        this.expire();
        return true;
    }

    public void expire() {
        if (this.level.isClientSide) return;
        Entity owner = getBoss();
        if (owner instanceof AstralDevourerEntity) {
            ((AstralDevourerEntity) owner).onMirageDispelled(this.getId());
        } else if (owner instanceof RiftGuardianEntity) {
            ((RiftGuardianEntity) owner).onMirageDispelled(this.getId());
        }
        this.remove();
    }
}
