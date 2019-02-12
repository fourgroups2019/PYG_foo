package cn.itcast.core.service.brand;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

/**
 * 查询所有品牌
 */
public interface BrandService {
    /**
     * 查询所有品牌
     */
    public List<Brand> findAll();
   //品牌列表分页查询
    public PageResult findPage(Integer PageNo,Integer PageSize);

//    根据条件查询对象
    public PageResult search(Integer pageNo, Integer pageSize, Brand brand);

//    添加品牌
    public void add(Brand brand);
//    品牌修改数据回显
    public Brand findOne(Long id);
//    修改更新操作
    public void update(Brand brand);
//    删除操作
    public void delete(Long[] ids);

    //新增规格时初始化品牌列表
    public List<Map<String,String>> selectOptionList();
}
