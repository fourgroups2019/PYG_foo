package cn.itcast.core.controller.specification;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.specification.SpecificationService;
import cn.itcast.core.vo.SpecificationVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/specification")
public class SpecificationController {
    @Reference
    private SpecificationService specificationService;

    /**
     * 条件查询
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification){
        return specificationService.search(page,rows,specification);
    }

    /**
     * 规格申请
     * @param specificationVo
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.add(specificationVo);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"保存失败");
        }
    }
}
