package com.example.demo.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 返回结果实体类
 */


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public  class  Result<T> {

    private Integer code;// 返回码

    private String message;//返回信息

    private T data;// 返回数据

    public static<T> Result success(String msg,T data){
        return new Result(StatusCode.OK,msg,data);
    }
    public static Result success(String msg){
        return new Result(StatusCode.OK,msg,null);
    }
    public static Result error(Integer code,String message){
        return new Result(code,message,null);
    }
    public static Result error(String message){
        return new Result(StatusCode.SERVICEERROR,message,null);
    }





}
