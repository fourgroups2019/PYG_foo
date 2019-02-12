package cn.itcast.core.service.brand;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {
    @Resource
    private BrandDao brandDao;

    @Override
    public List<Brand> findAll() {
        List<Brand> brands = brandDao.selectByExample(null);
        return brands;
    }
//   品牌列表分页查询
    @Override
    public PageResult findPage(Integer PageNo, Integer PageSize) {
        // 1、设置分页条件-分页插件
        PageHelper.startPage(PageNo,PageSize);
        // 2、根据条件查询
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(null);
        // 3、创建PageResult对象并且封装结果
        return new PageResult(page.getResult(),page.getTotal());
    }

    @Override
    public PageResult search(Integer pageNo, Integer pageSize, Brand brand) {
        // 1、设置分页条件-分页插件
        PageHelper.startPage(pageNo, pageSize);
        // 2、设置查询条件
        BrandQuery brandQuery = new BrandQuery();
        // 封装查询条件：
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if(brand.getName() != null && !"".equals(brand.getName().trim())){
            criteria.andNameLike("%" + brand.getName().trim() + "%");
        }
        if(brand.getFirstChar() != null && !"".equals(brand.getFirstChar().trim())){
            criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
        }
        // 设置根据字段排序
        brandQuery.setOrderByClause("id desc");
        // 3、根据条件查询
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);
        // 4、创建PageResult对象并且封装结果
        return new PageResult(page.getResult(), page.getTotal());
    }

    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }

    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    @Transactional
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    @Transactional
    @Override
    public void delete(Long[] ids) {
        if (ids!=null&&ids.length>0){
            for (Long id : ids) {
                brandDao.deleteByPrimaryKey(id);
            }
        }
    }
    //新增规格时初始化品牌列表
    @Override
    public List<Map<String, String>> selectOptionList() {
        return brandDao.selectOptionList();
    }


}
