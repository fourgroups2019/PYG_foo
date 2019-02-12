package cn.itcast.core.service.specification;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.vo.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
    public PageResult search(Integer page, Integer rows, Specification specification);

   public void add(SpecificationVo specificationVo);

//   商品规格的回显
    public SpecificationVo findOne(Long id);

//    新增
    public void update(SpecificationVo specificationVo);

//    删除操作
    public void delete(Long[] ids);

    //新增规格时初始化品牌列表

    public List<Map<String,String>> selectOptionList();
}
