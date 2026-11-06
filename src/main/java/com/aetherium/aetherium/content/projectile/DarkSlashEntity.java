package com.aetherium.aetherium.content.projectile;

import com.aetherium.aetherium.content.boss.BossMirageEntity;
import com.aetherium.aetherium.registry.RegistryHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import javax.annotation.Nullable;
import net.minecraft.network.IPacket;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A visible violet diagonal energy cut emitted by each valid sword swing.
 * Travels only ~4.3 blocks beyond its initial hand offset, lasts eight ticks,
 * respects walls and damages each nearby enemy at most once.
 */
public final class DarkSlashEntity extends Entity {
    public static final double SPEED = 0.54D;
    private static final int MAX_AGE = 8;
    private final Set<UUID> alreadyHit = new HashSet<>();
    private float slashDamage = 5.0F;
    @Nullable private PlayerEntity shooter;
    @Nullable private UUID shooterId;

    public DarkSlashEntity(EntityType<? extends DarkSlashEntity> type, World world) {
        super(type, world);
        this.noPhysics = true;
    }

    public DarkSlashEntity(World world, PlayerEntity player, float damage) {
        this(RegistryHandler.DARK_SLASH.get(), world);
        this.shooter = player;
        this.shooterId = player.getUUID();
        this.slashDamage = damage;
    }

    @Nullable
    private PlayerEntity getOwnerPlayer() {
        if (this.shooter != null && this.shooter.isAlive()) return this.shooter;
        if (this.shooterId != null && this.level instanceof ServerWorld) {
            Entity loaded = ((ServerWorld) this.level).getEntity(this.shooterId);
            if (loaded instanceof PlayerEntity) {
                this.shooter = (PlayerEntity) loaded;
                return this.shooter;
            }
        }
        return null;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        // Orientation travels in Forge's spawn packet. The server alone owns
        // damage, position, team checks and the list of entities already hit.
    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT tag) {
        // Eight-tick projectiles deliberately do not persist across world saves.
    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT tag) {
        this.remove();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > MAX_AGE) {
            if (!this.level.isClientSide) this.remove();
            return;
        }
        Vector3d motion = this.getDeltaMovement();
        if (motion.lengthSqr() < 0.00001D) return;

        Vector3d from = this.position();
        Vector3d to = from.add(motion);
        BlockRayTraceResult wall = this.level.clip(new RayTraceContext(from, to,
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
        boolean blocked = wall.getType() == RayTraceResult.Type.BLOCK;
        if (blocked) to = wall.getLocation();

        // Both sides move the projectile for fluid client interpolation.
        this.setPos(to.x, to.y, to.z);
        if (this.level.isClientSide) {
            for (int i = 0; i < 3; i++) {
                this.level.addParticle(ParticleTypes.DRAGON_BREATH,
                        this.getX() + (random.nextDouble() - 0.5D) * 0.75D,
                        this.getY() + (random.nextDouble() - 0.5D) * 0.65D,
                        this.getZ() + (random.nextDouble() - 0.5D) * 0.75D,
                        0.0D, 0.01D, 0.0D);
            }
        } else {
            hitNearbyEnemies();
            if (this.level instanceof ServerWorld && this.tickCount % 2 == 0) {
                ((ServerWorld)this.level).sendParticles(ParticleTypes.DRAGON_BREATH,
                        this.getX(), this.getY(), this.getZ(), 2,
                        0.22D, 0.22D, 0.22D, 0.02D);
            }
        }
        if (blocked && !this.level.isClientSide) this.remove();
    }

    private void hitNearbyEnemies() {
        PlayerEntity shooter = this.getOwnerPlayer();
        if (shooter == null) return;
        PlayerEntity player = shooter;

        // One slash is a 1.9-block diagonal blade, so it has a tight AOE
        // around its forward-moving center instead of raycasting only 1 pixel.
        AxisAlignedBB area = this.getBoundingBox().inflate(0.78D, 0.85D, 0.78D);
        for (LivingEntity victim : this.level.getEntitiesOfClass(LivingEntity.class,
                area, target -> target.isAlive() && target != shooter
                        && !target.isAlliedTo(shooter))) {
            if (!alreadyHit.add(victim.getUUID())) continue;

            // Permit bonus damage on the same enemy the player just hit with
            // the sword. Otherwise vanilla's invulnerability frames swallow
            // the weaker slash entirely, contradicting its purpose.
            if (victim.invulnerableTime > 0 && victim.getLastHurtByMob() == player) {
                victim.invulnerableTime = 0;
            }

            DamageSource source = DamageSource.indirectMagic(this, player);
            if (victim.hurt(source, this.slashDamage) && this.level instanceof ServerWorld) {
                ((ServerWorld)this.level).sendParticles(ParticleTypes.DRAGON_BREATH,
                        victim.getX(), victim.getY(0.5D), victim.getZ(), 10,
                        0.26D, 0.29D, 0.26D, 0.02D);
            }
        }
    }
}
