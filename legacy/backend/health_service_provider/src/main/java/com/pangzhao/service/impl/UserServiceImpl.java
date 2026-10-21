package com.pangzhao.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pangzhao.mapper.*;
import com.pangzhao.pojo.*;
import com.pangzhao.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
@Service(interfaceClass = UserService.class)
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserAndRoleMapper userAndRoleMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RoleAndPermissionMapper roleAndPermissionMapper;
    @Autowired
    private PermissionMapper permissionMapper;

    //根据用户名查找用户 包括用户对应的角色集合 以及角色对应的权限集合
    @Override
    public User findByName(String s) {
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username",s);
        User user = userMapper.selectOneByExample(example);

        List<Role> roles = findRoleByRoleId(findRoleIdByUserId(user.getId()));
        for (Role role : roles) {
            List<Permission> permissions = findPermissionByPermissionId(findPermissionIdByRole(role));
            Set<Permission> permissions1 = new HashSet<>();
            for (Permission permission : permissions) {
                permissions1.add(permission);
            }
            role.setPermissions(permissions1);
        }

        Set<Role> roles1 = new HashSet<>();
        for (Role role : roles) {
            roles1.add(role);
        }
        user.setRoles(roles1);
        return user;
    }


    //根据user的id查询对应的role的id集合
    public List<Integer> findRoleIdByUserId(Integer userId){
        ArrayList<Integer> list = new ArrayList<>();
        Example example = new Example(UserAndRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",userId);
        List<UserAndRole> userAndRoles = userAndRoleMapper.selectByExample(example);
        for (UserAndRole userAndRole : userAndRoles) {
            list.add(userAndRole.getRoleId());
        }
        return list;
    }

    //根据role的id集合查找role的集合
    public List<Role> findRoleByRoleId(List<Integer> roleIds){
        Example example = new Example(Role.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",roleIds);
        List<Role> roles = roleMapper.selectByExample(example);
        return roles;
    }

    //根据role查找对应的权限id的集合
    public List<Integer> findPermissionIdByRole(Role role){
        ArrayList<Integer> list = new ArrayList<>();
        Example example = new Example(RoleAndPermission.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("roleId",role.getId());
        List<RoleAndPermission> roleAndPermissions = roleAndPermissionMapper.selectByExample(example);
        for (RoleAndPermission roleAndPermission : roleAndPermissions) {
            list.add(roleAndPermission.getPermissionId());
        }
        return list;
    }

    //根据permission的id查找权限的集合
    public List<Permission> findPermissionByPermissionId(List<Integer> permissionIds){
        Example example = new Example(Permission.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",permissionIds);
        List<Permission> permissions = permissionMapper.selectByExample(example);
        return permissions;
    }
}
