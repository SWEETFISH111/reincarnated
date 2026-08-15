package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ノード種類のパレット解放状態（①）。
 * どのCompoundNode回路を編集できるか（②）はSkillAccessControlの責務で、ここでは扱わない。
 */
public class NodeUnlockState implements PersistentComponent {
    private final Map<EditorTab, Set<MagiculeNodeType>> unlockedNodeTypes = new EnumMap<>(EditorTab.class);

    public NodeUnlockState(){
        for (EditorTab tab : EditorTab.values()) {
            unlockedNodeTypes.put(tab, new HashSet<>());
        }
    }

    public boolean isNodeTypeUnlocked(EditorTab tab, MagiculeNodeType nodeType) {
        Set<MagiculeNodeType> types = unlockedNodeTypes.get(tab);
        return types != null && types.contains(nodeType);
    }

    public void unlockNodeType(EditorTab tab, MagiculeNodeType nodeType) {
        unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>()).add(nodeType);
    }

    public void unlockNodeTypes(EditorTab tab, Set<MagiculeNodeType> nodeTypes) {
        unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>()).addAll(nodeTypes);
    }

    public void addDefaultUnlockedNodes(EditorTab tab) {
        if (tab == EditorTab.SKILL) {
            Set<MagiculeNodeType> types = unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>());
            types.add(MagiculeNodeType.ADD);
            types.add(MagiculeNodeType.AND);
            types.add(MagiculeNodeType.DIVIDE);
            types.add(MagiculeNodeType.EQUAL);
            types.add(MagiculeNodeType.GREATER_THAN);
            types.add(MagiculeNodeType.GRATER_OR_EQUAL);
            types.add(MagiculeNodeType.LESS_THAN);
            types.add(MagiculeNodeType.LESS_OR_EQUAL);
            types.add(MagiculeNodeType.MODULO);
            types.add(MagiculeNodeType.MULTIPLY);
            types.add(MagiculeNodeType.NOT);
            types.add(MagiculeNodeType.OR);
            types.add(MagiculeNodeType.SUBTACT);
        } else if (tab == EditorTab.MAGIC) {
            Set<MagiculeNodeType> types = unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>());
            types.add(MagiculeNodeType.DAMAGE);
            types.add(MagiculeNodeType.EXPLOSION);
            types.add(MagiculeNodeType.HEALING);
            types.add(MagiculeNodeType.LIGHTNING);
            types.add(MagiculeNodeType.DIG);
            types.add(MagiculeNodeType.DIG_ALl);
            types.add(MagiculeNodeType.COLLECT_ITEMS);
            types.add(MagiculeNodeType.DELAY);
            types.add(MagiculeNodeType.IF);
            types.add(MagiculeNodeType.REPEAT);
            types.add(MagiculeNodeType.TOGGLE);
            types.add(MagiculeNodeType.WHILE);
            types.add(MagiculeNodeType.COMBERS_LOOK_DIRECTION);
            types.add(MagiculeNodeType.COMBERS_TARGET_POS);
            types.add(MagiculeNodeType.OFFSET);
            types.add(MagiculeNodeType.TO_BLOCK_POS);
            types.add(MagiculeNodeType.GET_LOOK_FORWARD);
            types.add(MagiculeNodeType.GET_LOOK_TARGET);
            types.add(MagiculeNodeType.GET_NEAREST_ENTITY_IN_RADIUS);
            types.add(MagiculeNodeType.RETURN_CASTER);
            types.add(MagiculeNodeType.GET_BLOCK_AT_POS);
            types.add(MagiculeNodeType.GET_CURENT_MASO);
            types.add(MagiculeNodeType.GET_MAX_MASO);
            types.add(MagiculeNodeType.GET_CURRENT_HP);
            types.add(MagiculeNodeType.GET_MAX_HP);
            types.add(MagiculeNodeType.EVENT_KEY_ONE);
            types.add(MagiculeNodeType.ON_SLOT_ENABLE);
            types.add(MagiculeNodeType.NUMBER);
            types.add(MagiculeNodeType.BOOLEAN);
            types.add(MagiculeNodeType.VECTOR);
            types.add(MagiculeNodeType.NULL);
            types.add(MagiculeNodeType.ADD);
            types.add(MagiculeNodeType.AND);
            types.add(MagiculeNodeType.DIVIDE);
            types.add(MagiculeNodeType.EQUAL);
            types.add(MagiculeNodeType.GREATER_THAN);
            types.add(MagiculeNodeType.GRATER_OR_EQUAL);
            types.add(MagiculeNodeType.LESS_THAN);
            types.add(MagiculeNodeType.LESS_OR_EQUAL);
            types.add(MagiculeNodeType.MODULO);
            types.add(MagiculeNodeType.MULTIPLY);
            types.add(MagiculeNodeType.NOT);
            types.add(MagiculeNodeType.OR);
            types.add(MagiculeNodeType.SUBTACT);
            types.add(MagiculeNodeType.SHOOT_PROJECTILE);
            types.add(MagiculeNodeType.SUMMON);
            types.add(MagiculeNodeType.INPUT_PROXY);
            types.add(MagiculeNodeType.OUTPUT_PROXY);
        } else if (tab == EditorTab.ARTS) {
            Set<MagiculeNodeType> types = unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>());
            types.add(MagiculeNodeType.ADD);
            types.add(MagiculeNodeType.AND);
            types.add(MagiculeNodeType.DIVIDE);
            types.add(MagiculeNodeType.EQUAL);
            types.add(MagiculeNodeType.GREATER_THAN);
            types.add(MagiculeNodeType.GRATER_OR_EQUAL);
            types.add(MagiculeNodeType.LESS_THAN);
            types.add(MagiculeNodeType.LESS_OR_EQUAL);
            types.add(MagiculeNodeType.MODULO);
            types.add(MagiculeNodeType.MULTIPLY);
            types.add(MagiculeNodeType.NOT);
            types.add(MagiculeNodeType.OR);
            types.add(MagiculeNodeType.SUBTACT);
        }
    }

    @Override
    public void saveToNBT(CompoundTag tag) {
        for (Map.Entry<EditorTab, Set<MagiculeNodeType>> entry : unlockedNodeTypes.entrySet()) {
            ListTag listTag = new ListTag();
            for (MagiculeNodeType nodeType : entry.getValue()) {
                listTag.add(StringTag.valueOf(nodeType.name()));
            }
            tag.put(entry.getKey().name(), listTag);
        }
    }

    @Override
    public void loadFromNBT(CompoundTag tag) {
        for (EditorTab tab : EditorTab.values()) {
            if (tag.contains(tab.name())) {
                ListTag listTag = tag.getListOrEmpty(tab.name());
                Set<MagiculeNodeType> set = unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>());
                for (int i = 0; i < listTag.size(); i++) {
                    try {
                        String typeName = listTag.getStringOr(i, null);
                        if (typeName != null) {
                            set.add(MagiculeNodeType.valueOf(typeName));
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }
}