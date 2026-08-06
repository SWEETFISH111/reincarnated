package com.github.sweetfish111.reincarnated.magic;

import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
    private double gravity = 0.03;
    private int livingtickTime = 10 * 20;

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

    public void setLivingTime(double time){
        double tickTime = time * 20;
        this.livingtickTime = (int)tickTime;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if(DATA_SIZE.equals(key)){
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void tick() {
        super.tick();
        AABB searchBox = this.getBoundingBox();
        Entity hitEntity = null;

        for (Entity entity : this.level().getEntities(this, searchBox)) {
            // 術者自身や、アイテムなどの当たりたくないものを除外する条件分岐
            hitEntity = entity;
            break; // 最初に見つかったヒット対象を確定
        }

        // 3. 接触するエンティティがいたら、着弾処理（爆発・ダメージ・消滅）を走らせる
        if (hitEntity != null) {
            this.onHit(new EntityHitResult(hitEntity)); // 独自のヒット処理
            this.discard(); // エンティティを消滅させる（despawn）
        }

        if(livingtickTime > 0){
            livingtickTime--;
        }else{
            this.onHit(new HitResult(this.position()) {
                @Override
                public Type getType() {
                    return null;
                }
            });
            this.discard();
        }

    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        if(!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel){
            if(this.context != null && this.nextNode != null){
                Vec3 hitPos = hitResult.getLocation();
                MagicContext newContext = new MagicContext(context.getCircuit(), context.getRuntimeCircuit());
                newContext.setMagicValue("hit_pos", hitPos);

                if(hitResult instanceof EntityHitResult entityHitResult){
                    newContext.setMagicValue("hit_entity", entityHitResult.getEntity());
                }

                if(nextNode != null){
                    try{
                        nextNode.execute(newContext);
                    }catch (CalculationCapacityOverException c){
                        IMagicCaster iMagicCaster = context.getCaster();
                        if(iMagicCaster.getCasterEntity() instanceof ServerPlayer caster){
                            caster.sendSystemMessage(Component.literal("《告》発射体が制御不能に陥りました"));
                        }
                        context.getCaster().getCasterLevel().explode(null, hitPos.x, hitPos.y, hitPos.z, 10.0f, Level.ExplosionInteraction.TNT);
                    }catch (MasoShortageException m){
                        IMagicCaster iMagicCaster = context.getCaster();
                        if(iMagicCaster.getCasterEntity() instanceof ServerPlayer caster){
                            caster.sendSystemMessage(Component.literal("《告》個体名" + caster.getName().getString() + "の魔素残量が低下。発射体の術式を維持できません"));
                        }
                    }
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
