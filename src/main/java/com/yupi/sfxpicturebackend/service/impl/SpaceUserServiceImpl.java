package com.yupi.sfxpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.sfxpicturebackend.exception.ErrorCode;
import com.yupi.sfxpicturebackend.exception.ThrowUtils;
import com.yupi.sfxpicturebackend.model.dto.space.space_user.SpaceUserAddRequest;
import com.yupi.sfxpicturebackend.model.dto.space.space_user.SpaceUserQueryRequest;
import com.yupi.sfxpicturebackend.model.entity.Space;
import com.yupi.sfxpicturebackend.model.entity.SpaceUser;
import com.yupi.sfxpicturebackend.model.entity.User;
import com.yupi.sfxpicturebackend.model.enums.SpaceRoleEnum;
import com.yupi.sfxpicturebackend.model.vo.SpaceUserVO;
import com.yupi.sfxpicturebackend.model.vo.SpaceVO;
import com.yupi.sfxpicturebackend.model.vo.UserVO;
import com.yupi.sfxpicturebackend.service.SpaceService;
import com.yupi.sfxpicturebackend.service.SpaceUserService;
import com.yupi.sfxpicturebackend.mapper.SpaceUserMapper;
import com.yupi.sfxpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 飞翔不会飞
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2026-05-15 10:07:38
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    private final SpaceService spaceService;
    private final UserService userService;

    public SpaceUserServiceImpl(SpaceService spaceService, UserService userService) {
        this.spaceService = spaceService;
        this.userService = userService;
    }

    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 1. 填充参数默认值
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        // 2. 校验参数
        this.validSpaceUser(spaceUser, true);
        // 3. 插入数据
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "添加空间用户关联失败");
        return spaceUser.getId();
    }

    /**
     * 校验参数，如果是add，则判断空间和用户是否存在，如果是编辑，则判断角色是否合法
     *
     * @param spaceUser
     * @param add
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        String spaceRole = spaceUser.getSpaceRole();
        if (add) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(ObjectUtil.isEmpty(space), ErrorCode.PARAMS_ERROR, "空间 id 错误");
            User user = userService.getById(userId);
            ThrowUtils.throwIf(ObjectUtil.isEmpty(user), ErrorCode.PARAMS_ERROR, "用户 id 错误");
        }
        ThrowUtils.throwIf(spaceRole == null || spaceRole.isEmpty(), ErrorCode.PARAMS_ERROR, "空间角色不能为空");
        SpaceRoleEnum enumByValue = SpaceRoleEnum.getEnumByValue(spaceRole);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "空间角色不合法");
    }

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        // 对象转封装类
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        // 关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        // 关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
            spaceUserVO.setSpace(spaceVO);
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 将所有的spaceuer转为vo
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream()
                .map(SpaceUserVO::objToVo)
                .collect(Collectors.toList());
        // 获取vo里的用户id
        Set<Long> userIdSet = spaceUserVOList.stream().map(SpaceUserVO::getUserId).collect(Collectors.toSet());
        // 获取vo里空间id列表
        Set<Long> spaceIdSet = spaceUserVOList.stream().map(SpaceUserVO::getSpaceId).collect(Collectors.toSet());
        // 获取id列表对应的用户列表,并转为map，为了后续根据id直接获取对应的user，而不需要遍历
        Map<Long, List<User>> userListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 获取空间id列表对应的空间列表
        Map<Long, List<Space>> spaceListMap = spaceService.listByIds(spaceIdSet).stream().collect(Collectors.groupingBy(Space::getId));
        // 填充vo里的用户信息和空间信息
        spaceUserVOList.forEach(spaceUserVo -> {
            Long userId = spaceUserVo.getUserId();
            Long spaceId = spaceUserVo.getSpaceId();
            if(userListMap.containsKey(userId)){
                User user = userListMap.get(userId).get(0);
                UserVO userVO = userService.getUserVO(user);
                spaceUserVo.setUser(userVO);
            }
            if(spaceListMap.containsKey(spaceId)){
                Space space = spaceListMap.get(spaceId).get(0);
                SpaceVO spaceVO = spaceService.getSpaceVO(space, null);
                spaceUserVo.setSpace(spaceVO);
            }
        });
        return spaceUserVOList;
    }


    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }

}




