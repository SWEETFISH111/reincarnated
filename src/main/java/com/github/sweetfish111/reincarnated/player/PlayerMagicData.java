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
    public float maxMaso = 20f;
    public float currentMaso = 20f;
    public float masoRegenRate = 0.1f;

    public PlayerMagicData(){
        for(EditorTab tab : EditorTab.values()){
            circuits.put(tab, new MagiculeCircuit());
        }
    }

    public MagiculeCircuit getCircuit(EditorTab tab){
        return circuits.computeIfAbsent(tab, k -> new MagiculeCircuit());
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
        masoTag.putFloat("maxMaso", maxMaso);
        masoTag.putFloat("currentMaso", currentMaso);
        masoTag.putFloat("masoRegenRate", masoRegenRate);
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
            maxMaso = masoTag.getFloat("maxMaso").orElse(20f);
            currentMaso = masoTag.getFloat("currentMaso").orElse(20f);
            masoRegenRate = masoTag.getFloat("masoRegenRate").orElse(0.1f);

        }
    }
}
