package com.webank.webase.front.util.pageutils;

import java.util.List;

public class PageData<T> {
    //当前页
    private int pageIndex;
    //每页的数量
    private int pageSize;
    //当前页的数量
    private int thisSize;
    // 总记录数
    private int totalSize;
    //总页数
    private int pageNum;
    // 数据
    private List<T> list;

    public List<T> getList() {
        return this.list;
    }

    public PageData() {
        this.pageIndex = 0;
        this.pageSize = 0;
        this.thisSize = 0;
        this.totalSize = 0;
        this.pageNum = 0;
        this.list = null;
    }

    public PageData(List<T> list, int pageIndex, int pageSize, int thisSize, int totalSize, int pageNum) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.thisSize = thisSize;
        this.totalSize = totalSize;
        this.pageNum = pageNum;
        this.list = list;
    }
}
