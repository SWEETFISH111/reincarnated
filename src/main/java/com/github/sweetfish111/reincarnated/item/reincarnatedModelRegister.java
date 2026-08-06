package com.github.sweetfish111.reincarnated.item;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.data.PackOutput;

import java.util.Collections;
import java.util.Optional;

import static com.github.sweetfish111.reincarnated.item.ReincarnatedItems.GRIMOIRE;

public class reincarnatedModelRegister extends ModelProvider {

    public reincarnatedModelRegister(PackOutput output, String modId) {
        super(output, modId);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.itemModelOutput.register(
                GRIMOIRE.get(),
                new ClientItem(
                        new CuboidItemModelWrapper.Unbaked(
                            ModelLocationUtils.getModelLocation(GRIMOIRE.get()),
                            Optional.empty(),
                            Collections.emptyList()
                        ),
                        new ClientItem.Properties(
                                false,
                                false,
                                1.0f
                        )
                )


        );
    }
}
