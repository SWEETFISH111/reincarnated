package com.github.sweetfish111.reincarnated.blockentity;

import com.github.sweetfish111.reincarnated.circuit.CircuitCompileCache;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.item.ReincarnatedItems;
import com.github.sweetfish111.reincarnated.magic.caster.MagicCircleCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.tank.MasoTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import static com.github.sweetfish111.reincarnated.blockentity.ReincarnatedBlockEntities.MAGIC_CIRCLE_ENTITY;
import static com.github.sweetfish111.reincarnated.item.ReincarnatedItems.*;

public class MagicCircleEntity extends RandomizableContainerBlockEntity {
    private final MasoTank masoTank = new MasoTank(10000);
    private MagiculeCircuit circuit = new MagiculeCircuit();
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY); // ★唯一のアイテム入れ物

    public MagicCircleEntity(BlockPos pos, BlockState state){
        super(MAGIC_CIRCLE_ENTITY.get(), pos, state);
    }

    public MasoTank getMasoTank(){return this.masoTank;}
    public MagiculeCircuit getCircuit(){return this.circuit;}
    // ★getBlockInventory()は削除。今後「インベントリ」が必要な場所には、MagicCircleEntity自身(this)を渡す

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("maso_tank", CompoundTag.CODEC, masoTank.saveToNBT());
        output.store("circuit", CompoundTag.CODEC, circuit.saveToNBT());
        if (!this.trySaveLootTable(output)) {
            ContainerHelper.saveAllItems(output, this.items);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("maso_tank", CompoundTag.CODEC)
                .ifPresent(this.masoTank::loadFromNBT);
        input.read("circuit", CompoundTag.CODEC)
                .ifPresent(this.circuit::loadFromNBT);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(input)) {
            ContainerHelper.loadAllItems(input, this.items);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        loadCircuitFromBook();
    }

    private void loadCircuitFromBook() {
        ItemStack bookStack = this.getItem(0); // スロット0＝本
        if (bookStack.isEmpty()) {
            this.circuit = new MagiculeCircuit();
            return;
        }

        CustomData customData = bookStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            this.circuit.loadFromNBT(tag);
            CircuitCompileCache.invalidate(this.circuit);
        }
    }

    @Override
    public int getContainerSize(){ return 5; }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("gui.reincarnated.magic_circle");
    }

    @Override
    protected NonNullList<ItemStack> getItems() { return this.items; }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) { this.items = nonNullList; }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new MagicCircleMenu(i, inventory, this); // ★MagicCircleEntity自身(this)を渡す
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MagicCircleEntity entity) {
        if (level.isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) level;

        entity.consumeMasoStoneIfPossible();

        MagicCircleCasterAdapter caster = new MagicCircleCasterAdapter(entity);
        RuntimeMagicCircuit runtimeCircuit = MagicCompiler.compileCircuit(caster, entity.getCircuit());
        if (runtimeCircuit == null) return;

        for (AbstractMagicNode startNode : runtimeCircuit.getInstancedNodes().values()) {
            if (startNode.isTrigger() && "on_tick".equals(startNode.getTriggerType())) {
                MagicContext context = new MagicContext(entity.getCircuit(), runtimeCircuit);
                RuntimeMagicCircuit.executeNode(caster, startNode.getId(), context);
            }
        }
    }

    private void consumeMasoStoneIfPossible() {
        ItemStack stoneStack = this.getItem(1); // スロット1＝魔石
        if (stoneStack.isEmpty() || !stoneStack.is(MASO_STONE.get())) return;

        double value = BalanceConfig.MASO_STONE_VALUE.get();
        double overflow = this.masoTank.deposit(value);

        if (overflow == 0) {
            stoneStack.shrink(1);
        }
    }
}