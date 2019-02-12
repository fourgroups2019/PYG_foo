package cn.itcast.core.controller.specification;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.specification.SpecificationService;
import cn.itcast.core.vo.SpecificationVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {
    @Reference
    private SpecificationService specificationService;
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification){
        return specificationService.search(page,rows,specification);
    }

    @RequestMapping("/add.do")
    public Result add( @RequestBody SpecificationVo specificationVo){
        try {
            specificationService.add(specificationVo);
            return new Result(true,"保存成功");
        }catch (Exception e){
            return new Result(false,"保存失败");
        }
    }

    @RequestMapping("/findOne.do")
    public SpecificationVo findOne(Long id){
        return specificationService.findOne(id);
    }

    @RequestMapping("/update.do")
    public Result update(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.update(specificationVo);
            return new Result(true,"更新成功");
        }catch (Exception e){
            return new Result(false,"更新失败");
        }
    }

    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    //新增规格时初始化品牌列表
    @RequestMapping("/selectOptionList.do")
    public List<Map<String,String>> selectOptionList(){
        return specificationService.selectOptionList();
    }

}
