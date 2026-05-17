package com.yupi.sfxpicturebackend.model.enums;

import lombok.Getter;

/**
 * @author 孙飞翔
 */
@Getter
public enum SpaceTypeEnum {
    /**
     * 个人空间
     */
    PERSONAL(0, "个人空间"),
    /**
     * 团队空间
     */
    TEAM(1, "团队空间");

    private final int value;
    private final String description;

    SpaceTypeEnum(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值获取枚举
     *
     * @param value
     * @return
     */
    public static SpaceTypeEnum getEnumByValue(int value) {
        if (value < 0) {
            return null;
        }
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()) {
            if (spaceTypeEnum.getValue() == value) {
                return spaceTypeEnum;
            }
        }
        return null;
    }
}
