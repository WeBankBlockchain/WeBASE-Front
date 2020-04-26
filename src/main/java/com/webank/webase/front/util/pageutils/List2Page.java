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

import java.util.ArrayList;
import java.util.List;

/**
 * use PageData to transfer list to paged list
 * @param <T>
 */
public class List2Page<T> {
    private List<T> data;
    private Integer pageSize;
    private Integer pageIndex;

    public List2Page(List<T> data, Integer pageSize, Integer pageIndex) {
        this.data = data;
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
    }

    public PageData<T> getResult() {
        List<T> resList = null; // 结果记录列表
        Integer size = data.size(); // 总记录数
        Integer pages = size / pageSize; // 总页数
        if (size - pages * pageSize > 0) {
            ++pages;
        }
        if (pageIndex < pages) {
            resList = data.subList((pageIndex - 1) * pageSize, pageSize * pageIndex);
        } else if (pageIndex == pages) {
            resList = data.subList((pageIndex - 1) * pageSize, size);
        } else {
            resList = new ArrayList<T>();
        }
        return new PageData<T>(resList, pageIndex, pageSize, resList.size(), size, pages);
    }

    public List<T> getPagedList() {
        List<T> resList = null; // 结果记录列表
        Integer size = data.size(); // 总记录数
        Integer pages = size / pageSize; // 总页数
        if (size - pages * pageSize > 0) {
            ++pages;
        }
        if (pageIndex < pages) {
            resList = data.subList((pageIndex - 1) * pageSize, pageSize * pageIndex);
        } else if (pageIndex.equals(pages)) {
            resList = data.subList((pageIndex - 1) * pageSize, size);
        } else {
            resList = new ArrayList<T>();
        }
        PageData<T> list = new PageData<T>(resList, pageIndex, pageSize, resList.size(), size, pages);
        return list.getList();
    }
}
