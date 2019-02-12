package cn.itcast.core.service.itemcat;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {
    public List<ItemCat> findByParentId(Long parentId);
    //三级联动
    public ItemCat findOne(Long id);

//    6.3.1	列表初始化查询所有分类

    public List<ItemCat> findAll();

    /**
     * 新增分类
     * @param itemCat
     */
    public void add(ItemCat itemCat);

}
