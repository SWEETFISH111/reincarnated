package com.github.sweetfish111.reincarnated.magic.caster;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;

import java.util.UUID;

public class PlayerCasterAdapter implements IMagicCaster{
    private final ServerPlayer player;

    @Override
    public Entity getCasterEntity() {
        return player;
    }

    public PlayerCasterAdapter(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public MagiculeCircuit getCircuit() {
        return player.getData(ModAttachments.PLAYER_MAGIC_DATA).getCircuit(EditorTab.MAGIC);
    }

    @Override
    public UUID getCasterId() {
        return player.getUUID();
    }

    @Override
    public Vec3 getCasterPosition() {
        return player.position();
    }

    @Override
    public ServerLevel getCasterLevel() {
        return (ServerLevel) player.level();
    }

    @Override
    public Vec3 getEyePosition() {
        return player.getEyePosition();
    }

    @Override
    public Vec3 getLookVector() {
        return player.getLookAngle();
    }

    @Override
    public float getMasoAmount() {
        PlayerMagicData data = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
        return data.currentMaso;
    }

    @Override
    public void addMaso(float amount) {
        PlayerMagicData data = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
        data.currentMaso += amount;
    }

    @Override
    public void consumeMaso(float amount) {
        PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
        magicData.currentMaso -= amount;
        magicData.totalConsumedMaso += amount;
    }

    @Override
    public void addTotalRegeneratedMaso(float amount) {
        PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
        magicData.totalRegeneratedMaso += amount;
    }

    @Override
    public boolean ownsCircuit(MagiculeCircuit circuit) {
        PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
        for(EditorTab tab : EditorTab.values()){
            if(tab == EditorTab.MAGIC){
                for(int i = 0; i < PlayerMagicData.MAGIC_SLOT_COUNT; i++){
                    if(magicData.getMagicSlot(i) == circuit){
                        return true;
                    }
                }
            }else {
                if(magicData.getCircuit(tab) == circuit){
                    return true;
                }
            }
        }
        return false;
    }
}
