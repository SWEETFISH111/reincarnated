package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

public class SummonLightningNode extends AbstractMagicNode {

    @Override
    public void execute(MagicContext context) {
        System.out.println("[kaminari] zikkou no nami ga toutatu! hidari no pin kara zahyou wo pull");

        Vec3 targetPos = pullVector3(1, context);
        System.out.println("[kaminari] shutoku sita zahyou:" + targetPos);

        // 2. 引っ張ってきた座標に雷を落とす！
        if (targetPos != null && context.getCaster().level() instanceof ServerLevel serverLevel) {
            System.out.println("[kaminari] kaminari shoukann");
            Identifier lightningId = Identifier.parse("lightning_bolt");
            var optionalHolder = BuiltInRegistries.ENTITY_TYPE.get(lightningId);

            if(optionalHolder.isPresent()){
                EntityType<?> entityType = optionalHolder.get().value();
                Entity entity = entityType.create(serverLevel, EntitySpawnReason.COMMAND);

                if(entity instanceof LightningBolt lightning){
                    lightning.setPos(targetPos);
                    serverLevel.addFreshEntity(lightning);
                }
            }
            System.out.println("[kaminari] dokan");
        }else {
            System.out.println("[kaminari] zahyou ga null mosikuha sekai ga serverlevel janai");
        }

        // 3. もし右側にさらに別の魔法（爆発など）が繋がっていれば、処理を続ける（Push）
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return null; // 雷ノード自体はデータを出力しない
    }
}