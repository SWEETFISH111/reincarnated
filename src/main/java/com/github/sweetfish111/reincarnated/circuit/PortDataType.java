package com.github.sweetfish111.reincarnated.circuit;

public enum PortDataType {
    EXEC(0XFFFFFFFF),
    NUMBER(0XFF00AAFF),
    VECTORE(0XFF55FF55),
    ENTITY(0XFFFF5555),
    ENTITYSNAPSHOT(0xFFFF2222),
    BOOLEAN(0xCFF77EB5),
    MASO(0xFF6F51A1),
    EXP(0xFFD3E173),
    DAMAGE(0xFFFF8C00),
    SATIETY(0xFFFFEFD5),
    POWERGAP(0xFF000080),
    KILLSCORE(0xFFFF0000),
    ANY(0xFFF4E511);

    public final int color;

    PortDataType(int color){
        this.color = color;
    }
}

