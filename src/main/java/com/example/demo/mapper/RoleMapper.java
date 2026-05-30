package com.example.demo.mapper;

import com.example.demo.model.pojo.Role;
import com.example.demo.model.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper {
    Role findRoleByName(String rolename);

    void saveRole(Integer roleId,Integer userId);

    List<Role> selectRoleById(int i);
}
