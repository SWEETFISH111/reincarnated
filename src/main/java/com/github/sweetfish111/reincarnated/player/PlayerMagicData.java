package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlayerMagicData {
    private final Map<EditorTab, MagiculeCircuit> circuits = new EnumMap<>(EditorTab.class);
    public float currentMaso = 20f;

    public float totalRegeneratedMaso = 0;
    public float totalConsumedMaso = 0;

    private static final double BASE_MAX_MASO = 20f;
    private static final double BASE_REGEN_RATE = 0.1f;

    // 成長係数（チューニング用）
    private static final double MAX_SCALE_FACTOR = 5.0;
    private static final double REGEN_SCALE_FACTOR = 0.5;

    public PlayerMagicData(){
        for(EditorTab tab : EditorTab.values()){
            circuits.put(tab, new MagiculeCircuit());
        }
    }

    public MagiculeCircuit getCircuit(EditorTab tab){
        return circuits.computeIfAbsent(tab, k -> new MagiculeCircuit());
    }

    public float getMaxMaso(){
        double scaledInput = this.totalConsumedMaso / 100.0;
        return (float) (BASE_MAX_MASO + MAX_SCALE_FACTOR * Math.log(1.0 + scaledInput));
    }

    public float getMasoRegenRate(){
        double scaledInput = this.totalRegeneratedMaso / 100.0;
        return (float)(BASE_REGEN_RATE + REGEN_SCALE_FACTOR * Math.log(1.0 + scaledInput));
    }

    public void setCircuits(EditorTab tab, MagiculeCircuit circuit){
        this.circuits.put(tab,circuit);
    }

    public CompoundTag saveToNBT(){
        CompoundTag rootTag = new CompoundTag();

        for(Map.Entry<EditorTab, MagiculeCircuit> entry : this.circuits.entrySet()){
            EditorTab tab = entry.getKey();
            MagiculeCircuit circuit = entry.getValue();

            rootTag.put(tab.name(), circuit.saveToNBT());
        }
        CompoundTag masoTag = new CompoundTag();
        masoTag.putFloat("currentMaso", currentMaso);
        masoTag.putFloat("totalRegeneratedMaso", totalRegeneratedMaso);
        masoTag.putFloat("totalConsumedMaso", totalConsumedMaso);
        rootTag.put("maso", masoTag);
        return rootTag;
    }

    public void loadFromNBT(CompoundTag rootTag){
        if(rootTag == null || rootTag.isEmpty()) return;

        for(EditorTab tab : EditorTab.values()){
            if(rootTag.contains(tab.name())){
                rootTag.getCompound(tab.name()).ifPresent(tabTag ->{
                    MagiculeCircuit circuit = new MagiculeCircuit();
                    circuit.loadFromNBT(tabTag);
                    this.circuits.put(tab, circuit);
                });
            }
        }
        if(rootTag.contains("maso")){
            CompoundTag masoTag = rootTag.getCompound("maso").orElse(new CompoundTag());
            currentMaso = masoTag.getFloat("currentMaso").orElse(20f);
            totalRegeneratedMaso = masoTag.getFloat("totalRegeneratedMaso").orElse(0f);
            totalConsumedMaso = masoTag.getFloat("totalConsumedMaso").orElse(0f);
        }
    }
}
