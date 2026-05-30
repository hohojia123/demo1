package com.example.demo.model.entity;


import com.github.pagehelper.PageInfo;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 分页结果类
 *
 * @param<T>
 */
@Data
@ToString
public class PageResult<T> {
    private Long total; //数据条数
    private List<T> rows; //数据

    public PageResult(PageInfo pageInfo) {
        super();
        this.total = pageInfo.getTotal();
        this.rows = pageInfo.getList();
    }

}
