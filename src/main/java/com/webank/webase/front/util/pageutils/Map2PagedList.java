package com.webank.webase.front.util.pageutils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Map2PagedList<T> {
    private List<MapHandle> data;
    private Integer pageSize;
    private Integer pageIndex;

    // map constructor
    public Map2PagedList(Map<T,T> maps, Integer pageSize, Integer pageIndex) {
        List<MapHandle> mapList = new ArrayList<>();
        Iterator it = maps.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            MapHandle handle = new MapHandle(key, maps.get(key));
            mapList.add(handle);
        }
        this.data = mapList;
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
    }

    public List<MapHandle> getPagedList() {
        List<MapHandle> resList = null; // 结果记录列表
        Integer size = data.size(); // 总记录数
        Integer pages = size / pageSize; // 总页数
        if (size - pages * pageSize > 0)
            ++pages;
        if (pageIndex < pages)
            resList = data.subList((pageIndex - 1) * pageSize, pageSize * pageIndex);
        else if (pageIndex == pages)
            resList = data.subList((pageIndex - 1) * pageSize, size);
        else
            resList = new ArrayList<>();
        PageData<MapHandle> list = new PageData<>(resList, pageIndex, pageSize, resList.size(), size, pages);
        return list.getList();
    }
}
