package cn.itcast.core.controller.template;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.template.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {
    @Reference
    private TypeTemplateService typeTemplateService;
    @RequestMapping("/findOne.do")
    public TypeTemplate findOne(Long id){
        return typeTemplateService.findOne(id);
    }

    @RequestMapping("/findBySpecList.do")
    public List<Map> findBySpecList(Long id){
       return typeTemplateService.findBySpecList(id);
    }

    /**
     * 条件查询
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody TypeTemplate typeTemplate){
        return typeTemplateService.search(page,rows,typeTemplate);
    }

    /**
     * 模板申请
     * @param typeTemplate
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody TypeTemplate typeTemplate){
        try {
            typeTemplateService.add(typeTemplate);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }
}
