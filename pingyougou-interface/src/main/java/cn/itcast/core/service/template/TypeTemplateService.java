package cn.itcast.core.service.template;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TypeTemplateService {
//    分页查询
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);

//    添加
    public void add(TypeTemplate typeTemplate);

//模板管理回显
    public TypeTemplate findOne(Long id);

    //修改
    public void update(TypeTemplate typeTemplate);

    //删除
    public void delete(Long[] ids);

//    通过模板id获取商品的规格列表
    public List<Map> findBySpecList(Long id);

}
