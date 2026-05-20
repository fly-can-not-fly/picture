package com.yupi.sfxpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.sfxpicturebackend.model.dto.space.space_user.SpaceUserAddRequest;
import com.yupi.sfxpicturebackend.model.dto.space.space_user.SpaceUserQueryRequest;
import com.yupi.sfxpicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.sfxpicturebackend.model.enums.SpaceRoleEnum;
import com.yupi.sfxpicturebackend.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 飞翔不会飞
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2026-05-15 10:07:38
*/
public interface SpaceUserService extends IService<SpaceUser> {

    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    void validSpaceUser(SpaceUser spaceUser, boolean add);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    SpaceRoleEnum getSpaceUserRole(long spaceId, long userId);

    boolean isSpaceAdmin(long spaceId, long userId);

    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);
}
