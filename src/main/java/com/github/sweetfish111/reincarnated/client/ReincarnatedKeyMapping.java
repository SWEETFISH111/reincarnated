package com.github.sweetfish111.reincarnated.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "reincarnated", value = Dist.CLIENT)
public class ReincarnatedKeyMapping {
    public static final Lazy<KeyMapping> MAGIC_KEY_1 = Lazy.of(() -> new KeyMapping(
            "key.reincarnated.MAGIC_KEY_1",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            KeyMapping.Category.MISC
    ));
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("reincarnated","category"));

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event){
        event.registerCategory(CATEGORY);
        event.register(MAGIC_KEY_1.get());
    }
}
