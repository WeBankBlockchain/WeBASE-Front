package com.webank.webase.front.util.pageutils;

import java.util.ArrayList;
import java.util.List;

public class List2Page<T> {
    private List<T> data;
    private Integer pageSize;
    private Integer pageIndex;

    public List2Page(List<T> data, Integer pageSize, Integer pageIndex) {
        this.data = data;
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
    }

    public List2Page(List<T> data, PageQuery pageQuery) {
        this(data, pageQuery.getPageSize(), pageQuery.getPageIndex());
    }

    public PageData<T> getResult() {
        List<T> resList = null; // 结果记录列表
        Integer size = data.size(); // 总记录数
        Integer pages = size / pageSize; // 总页数
        if (size - pages * pageSize > 0)
            ++pages;
        if (pageIndex < pages)
            resList = data.subList((pageIndex - 1) * pageSize, pageSize * pageIndex);
        else if (pageIndex == pages)
            resList = data.subList((pageIndex - 1) * pageSize, size);
        else
            resList = new ArrayList<T>();
        return new PageData<T>(resList, pageIndex, pageSize, resList.size(), size, pages);
    }

    public List<T> getPagedList() {
        List<T> resList = null; // 结果记录列表
        Integer size = data.size(); // 总记录数
        Integer pages = size / pageSize; // 总页数
        if (size - pages * pageSize > 0)
            ++pages;
        if (pageIndex < pages)
            resList = data.subList((pageIndex - 1) * pageSize, pageSize * pageIndex);
        else if (pageIndex == pages)
            resList = data.subList((pageIndex - 1) * pageSize, size);
        else
            resList = new ArrayList<T>();
        PageData<T> list = new PageData<T>(resList, pageIndex, pageSize, resList.size(), size, pages);
        return list.getList();
    }
}
