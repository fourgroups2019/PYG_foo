package cn.itcast.core.service.template;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service

public class TypeTemplateServiceImpl implements TypeTemplateService{
    @Resource
   private TypeTemplateDao typeTemplateDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        //点击列表查询时将数据放入缓存中
        List<TypeTemplate> list = typeTemplateDao.selectByExample(null);
        if (list!=null&&list.size()>0){
            for (TypeTemplate template : list) {
                //1,将品牌结果集放入缓存
                String brandIds = template.getBrandIds();
                List<Map> brandList = JSON.parseArray(brandIds, Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(),brandList);
                //2,将规格结果集放入缓存
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps("specList").put(template.getId(),specList);

            }
        }
//        设置分页条件
        PageHelper.startPage(page,rows);
       // 2、设置查询条件
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        if (typeTemplate.getName()!=null&&!"".equals(typeTemplate.getName().trim())){
            typeTemplateQuery.createCriteria().andBrandIdsLike("%"+typeTemplate.getName().trim()+"%");
        }
        /* 根据条件查询 */
        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(typeTemplateQuery);
//        封装结果集
        return new PageResult(p.getResult(),p.getTotal());
    }

//     添加
    @Transactional
    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }
//回显
    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }
//修改
    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids!=null&&ids.length>0){
            for (Long id : ids) {
                typeTemplateDao.deleteByPrimaryKey(id);
            }
        }
    }

    @Override
    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        // 栗子：[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        String specIds = typeTemplate.getSpecIds();
        //将json串转换成对象
        List<Map> specList = JSON.parseArray(specIds, Map.class);
        // 通过规格获取到规格选项
        if (specList!=null&&specList.size()>0){
            for (Map map : specList) {
                Long specId = Long.parseLong(map.get("id").toString());
                SpecificationOptionQuery optionQuery = new SpecificationOptionQuery();
                optionQuery.createCriteria().andSpecIdEqualTo(specId);
                List<SpecificationOption> options =specificationOptionDao.selectByExample(optionQuery);
                map.put("options",options);
            }

        }
        // 最终specList：[{"id":27,"text":"网络","options":options},{"id":32,"text":"机身内存"}]

        return specList;
    }
}
