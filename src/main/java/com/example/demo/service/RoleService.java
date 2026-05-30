package com.example.demo.service;

import com.example.demo.model.pojo.Role;

public interface RoleService {
    public Role findRoleByName(String user);

    public void saveRole(Integer roleId,Integer userId);
}
