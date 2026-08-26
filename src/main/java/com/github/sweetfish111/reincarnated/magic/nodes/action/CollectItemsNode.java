package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class CollectItemsNode extends AbstractMagicNode {

    public CollectItemsNode(UUID id) {
        super(id);
    }

    private float baseCost() {
        return BalanceConfig.COLLECT_ITEMS_BASE_COST.get().floatValue();
    }

    @Override
    public void execute(MagicContext context) {
        Vec3 center = pullVector3(1, context);
        double investedMaso = pullMaso(2, context).amount();
        float availableMaso = (float) (context.getMasoTank().getBalance() + context.getCaster().getMasoAmount());

        MasoInvestmentScaling.EffectResult effectResult =
                MasoInvestmentScaling.computeEffect(baseCost(), (float) investedMaso, availableMaso);
        double radius = Math.max(0.5, effectResult.effectAmount()); // 極端に小さい値の暴発防止
        masoCost = effectResult.masoCost();
        super.execute(context);

        int collectedCount = 0;

        if (center != null && context.getCaster().getCasterEntity() instanceof ServerPlayer player) {
            ServerLevel level = context.getLevel();

            AABB searchBox = new AABB(
                    center.x - radius, center.y - radius, center.z - radius,
                    center.x + radius, center.y + radius, center.z + radius
            );

            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, searchBox, ItemEntity::isAlive);

            for (ItemEntity itemEntity : items) {
                ItemStack stack = itemEntity.getItem().copy();
                if (stack.isEmpty()) continue;

                int beforeCount = stack.getCount();
                player.getInventory().add(stack); // stackは内部で拾えた分だけ減算される
                int afterCount = stack.getCount();

                if (afterCount <= 0) {
                    // 全量回収できた
                    itemEntity.discard();
                    collectedCount += beforeCount;
                } else if (afterCount < beforeCount) {
                    // 一部だけ回収できた（インベントリがほぼ満杯）
                    itemEntity.setItem(stack);
                    collectedCount += (beforeCount - afterCount);
                }
                // afterCount == beforeCount の場合は満杯で何も拾えず→そのまま放置
            }
        }

        // 複数個体設置時の混線を避けるため、ノードID単位でキーをスコープする
        context.setMagicValue("collected_count_" + this.id, (double) collectedCount);
        pushExecute(context);
    }

}