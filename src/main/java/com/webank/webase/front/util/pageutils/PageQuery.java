package com.webank.webase.front.util.pageutils;


public class PageQuery {
    private int pageSize;
    private int pageIndex;

    public PageQuery(int pageSize, int pageIndex) {
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
    }

    public PageQuery() {
        this.pageSize = 0;
        this.pageIndex = 0;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }
}
