package com.github.sweetfish111.reincarnated.magic;

import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class CustomMagicProjectileEntity extends ThrowableProjectile implements net.minecraft.world.entity.projectile.ItemSupplier{
    private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(CustomMagicProjectileEntity.class, EntityDataSerializers.FLOAT);

    public CustomMagicProjectileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
        System.out.println("objectile spwan");
    }

    private MagicNode nextNode;
    private MagicContext context;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SIZE, 0.5F);
    }

    public void setMagicExecutionData(MagicNode nextNode, MagicContext context){
        this.nextNode = nextNode;
        this.context = context;
    }

    public void setSize(float size){
        this.entityData.set(DATA_SIZE, size);
        this.refreshDimensions();
    }

    public float getSize(){
        return this.entityData.get(DATA_SIZE);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float size = getSize();
        return EntityDimensions.scalable(size, size);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if(DATA_SIZE.equals(key)){
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if(!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel){
            if(this.context != null && this.nextNode != null){
                Vec3 hitPos = hitResult.getLocation();
                this.context.setMagicValue("hit_pos", hitPos);

                if(hitResult instanceof EntityHitResult entityHitResult){
                    this.context.setMagicValue("hit_entity", entityHitResult.getEntity());
                }

                if(nextNode != null){
                    nextNode.execute(this.context);
                }

            }
            this.discard();
        }
    }

    @Override
    public net.minecraft.world.item.ItemStack getItem(){
        return new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.MAGMA_CREAM);
    }
}
