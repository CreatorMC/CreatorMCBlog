package com.creator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.creator.constants.SystemConstants;
import com.creator.dao.MenuDao;
import com.creator.dao.RoleMenuDao;
import com.creator.dao.UserRoleDao;
import com.creator.domain.entity.Menu;
import com.creator.domain.entity.Role;
import com.creator.domain.entity.RoleMenu;
import com.creator.domain.entity.UserRole;
import com.creator.service.MenuService;
import com.creator.utils.SecurityUtils;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 菜单权限表(Menu)表服务实现类
 *
 * @author makejava
 * @since 2023-08-07 08:43:17
 */
@Service("menuService")
public class MenuServiceImpl extends ServiceImpl<MenuDao, Menu> implements MenuService {

    @Autowired
    private UserRoleDao userRoleDao;

    @Override
    public List<String> selectMenuPermsByUserId(Long userId) {
        if(SecurityUtils.isAdmin()) {
            //是超级管理员，返回所有权限
            List<Menu> menuList = list(new LambdaQueryWrapper<Menu>()
                    .in(Menu::getMenuType, SystemConstants.MENU_TYPE_MENU, SystemConstants.MENU_TYPE_BUTTON)
                    .eq(Menu::getStatus, SystemConstants.MENU_STATUS_NORMAL)
            );
            return menuList.stream().map(Menu::getPerms).collect(Collectors.toList());
        }
        return userRoleDao.selectJoinList(String.class, new MPJLambdaWrapper<UserRole>()
                .select(Menu::getPerms)
                .innerJoin(Role.class, Role::getId, UserRole::getRoleId)
                .innerJoin(RoleMenu.class, RoleMenu::getRoleId, Role::getId)
                .innerJoin(Menu.class, Menu::getId, RoleMenu::getMenuId)
                .eq(UserRole::getUserId, userId)
                //角色状态为正常
                .eq(Role::getStatus, SystemConstants.ROLE_STATUS_NORMAL)
                //只查找C和F类型的菜单
                .in(Menu::getMenuType, SystemConstants.MENU_TYPE_MENU, SystemConstants.MENU_TYPE_BUTTON)
                //菜单状态为正常
                .eq(Menu::getStatus, SystemConstants.MENU_STATUS_NORMAL)
        );
    }
}

