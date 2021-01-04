/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.front.util.pageutils;

import java.util.List;

/**
 * data unit of page in List2Page tool
 * @param <T>
 */
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
