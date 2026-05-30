package com.example.demo.service.impl;

import com.example.demo.mapper.RoleMapper;
import com.example.demo.model.pojo.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl {
    @Autowired
    private RoleMapper roleMapper;
    public Role findRoleByName(String rolename) {
         return  roleMapper.findRoleByName(rolename);
    }

    public void saveRole(Integer roleId,Integer userId) {
        roleMapper.saveRole(roleId,userId);
    }
}
