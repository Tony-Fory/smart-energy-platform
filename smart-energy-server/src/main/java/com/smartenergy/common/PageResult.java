package com.smartenergy.common;

import lombok.Data;

import java.util.List;

/**
 * 分页查询结果
 *
 * @param <T> 数据类型
 * @author smart-energy
 */
@Data
public class PageResult<T> {

    private List<T> records;

    private long total;

    private PageResult() {
    }

    public static <T> PageResult<T> of(List<T> records, long total) {
        PageResult<T> result = new PageResult<>();
        result.records = records;
        result.total = total;
        return result;
    }
}
