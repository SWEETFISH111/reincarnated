package com.github.sweetfish111.reincarnated.circuit;

public enum MagiculeNodeType {
    EVENT_KEY_ONE("event_key_1", "実行キー１",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.EXEC}
    ),
    LIGHTNING("lightning", "雷ノード",
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE},
            new PortDataType[]{PortDataType.EXEC}
    ),
    EXPLOSION("explosion","爆発ノード",
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE},
            new PortDataType[]{PortDataType.EXEC}
    ),
    GET_LOOK_TARGET("get_look_target","見た相手",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.ENTITY}
    ),
    CASTER_POS("caster_pos", "発動者の位置",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.VECTORE}
    ),
    OFFSET("offset", "相対座標",
            new PortDataType[]{PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.VECTORE}
    ),
    GET_LOOK_FORWARD("get_look_forward", "視線の方向",
            new PortDataType[]{PortDataType.NUMBER},
            new PortDataType[]{PortDataType.VECTORE},
            ContentWidgetType.SWITCH
    ),
    NUMBER("number", "数字",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.NUMBER},
            ContentWidgetType.NUMBER_INPUT
    ),
    COMBERS_TARGET_POS("combers_target_pos", "座標変換",
            new PortDataType[]{PortDataType.ENTITY},
            new PortDataType[]{PortDataType.VECTORE}
    );

    private final String id;
    public final String displayName;
    private ContentWidgetType content;
    public final PortDataType[] inputs;
    public final PortDataType[] outputs;

    MagiculeNodeType(String id, String displayName, PortDataType[] inputs, PortDataType[] outputs){
        this.id = id;
        this.displayName = displayName;
        this.inputs = inputs;
        this.outputs = outputs;
        this.content = ContentWidgetType.NONE;
    }
    MagiculeNodeType(String id, String displayName, PortDataType[] inputs, PortDataType[] outputs, ContentWidgetType content){
        this(id, displayName, inputs, outputs);
        this.content = content;
    }

    public String getId(){
        return this.id;
    }
    public ContentWidgetType getContent(){return this.content;}

    public static MagiculeNodeType fromId(String id){
        for (MagiculeNodeType type : values()){
            if(type.id.equals(id)){
                return type;
            }
        }
        return null;
    }
}
