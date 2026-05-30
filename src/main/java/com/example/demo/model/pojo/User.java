package com.example.demo.model.pojo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 用户
 */
@Data
@ToString

public class User implements Serializable {

    /**
     * user(36) => 1436499(10)
     */
    private static final long serialVersionUID = 1436499L;


    private Integer id;

    private String name;

    private String password;

    private String mail;

    private Integer state;

    private String avatar;

    private List<Role> roles;

    private Login login;


}