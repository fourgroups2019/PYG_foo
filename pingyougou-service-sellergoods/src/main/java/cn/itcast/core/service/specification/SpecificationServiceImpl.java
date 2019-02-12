package cn.itcast.core.service.specification;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.vo.SpecificationVo;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {
    @Resource
    private SpecificationDao specificationDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {
        //设置分页条件
        PageHelper.startPage(page,rows);
        //设置查询条件
        SpecificationQuery specificationQuery = new SpecificationQuery();
        if (specification.getSpecName()!=null&&!"".equals(specification.getSpecName().trim())){
            specificationQuery.createCriteria().andSpecNameLike("%"+specification.getSpecName().trim()+"%");
        }
        specificationQuery.setOrderByClause("id desc");
        //根据条件查询
       Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(specificationQuery);
        //将结果封装到PageResult中
        return new PageResult(p.getResult(),p.getTotal());
    }

//    //保存商品规格
    @Transactional
    @Override
    public void add(SpecificationVo specificationVo) {
//        设置规格名称
        Specification specification = specificationVo.getSpecification();
        specificationDao.insertSelective(specification);//设置自增获取自增主键
//        获取规格选项
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        if (specificationOptionList!=null&&specificationOptionList.size()>0){
            for (SpecificationOption specificationOption : specificationOptionList) {
                //获取主键id
                specificationOption.setSpecId(specification.getId());
                //specificationOptionDao.insertSelective(specificationOption);
//                自定义sql批量插入
            }
            specificationOptionDao.insertSelectives(specificationOptionList);
        }

    }

    @Override
    public SpecificationVo findOne(Long id) {
        SpecificationVo specificationVo = new SpecificationVo();
        Specification specification = specificationDao.selectByPrimaryKey(id);
        specificationVo.setSpecification(specification);
//查询规格选项
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        specificationOptionQuery.createCriteria().andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(specificationOptionQuery);
//        封装数据;
        specificationVo.setSpecificationOptionList(specificationOptionList);
        return specificationVo;
    }

    @Transactional
    @Override
    public void update(SpecificationVo specificationVo) {
//        更新规格
        Specification specification = specificationVo.getSpecification();
        specificationDao.updateByPrimaryKeySelective(specification);
//        更新规格选项 先删除
        SpecificationOptionQuery optionQuery = new SpecificationOptionQuery();
        optionQuery.createCriteria().andSpecIdEqualTo(specification.getId());
        specificationOptionDao.deleteByExample(optionQuery);
//        在插入  添加
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        if (specificationOptionList!=null&&specificationOptionList.size()>0){
            for (SpecificationOption specificationOption : specificationOptionList) {
                //获取主键id
                specificationOption.setSpecId(specification.getId());
                //specificationOptionDao.insertSelective(specificationOption);
//                自定义sql批量插入
            }
            specificationOptionDao.insertSelectives(specificationOptionList);
        }

    }
//删除操作
    @Override
    public void delete(Long[] ids) {
        if (ids!=null&&ids.length>0){
            for (Long id : ids) {
//                先删规格选项
                SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
                specificationOptionQuery.createCriteria().andIdEqualTo(id);
                specificationOptionDao.deleteByExample(specificationOptionQuery);
//                再删除规格
                specificationDao.deleteByPrimaryKey(id);
            }

        }
    }
    //新增规格时初始化品牌列表
    @Override
    public List<Map<String, String>> selectOptionList() {
        return specificationDao.selectOptionList();
    }

}
