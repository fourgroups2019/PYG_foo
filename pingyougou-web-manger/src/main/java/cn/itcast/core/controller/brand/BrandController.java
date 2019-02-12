package cn.itcast.core.controller.brand;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.brand.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll.do")
    public List<Brand> findAll(){
        return brandService.findAll();
    }

//    品牌管理的分页查询
    @RequestMapping("/findPage.do")
    public PageResult findPage(Integer pageNo,Integer pageSize){
        return brandService.findPage(pageNo,pageSize);
    }
//    根据条件查询
    @RequestMapping("/search.do")
    public PageResult search(Integer pageNo,Integer pageSize,@RequestBody Brand brand){
        return brandService.search(pageNo,pageSize,brand);
    }
//    添加品牌
    @RequestMapping("/add.do")
    public Result add(@RequestBody Brand brand){
        try {
            brandService.add(brand);
            return new Result(true,"保存成功!");

        }catch (Exception e){
            return new Result(false,"保存失败!");
        }
    }
//    修改数据回显
    @RequestMapping("/findOne.do")
    public Brand findOne(Long id){
       return brandService.findOne(id);
    }
//修改更新数据
    @RequestMapping("/update.do")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true,"更新成功");
        }catch (Exception e){
            return new Result(false,"更新失败");
        }
    }
//    删除操作
    @RequestMapping("/delete.do")
    public Result delete( Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        }catch (Exception e){
            return new Result(false,"删除失败");
        }
    }
    //新增规格时初始化品牌列表
    @RequestMapping("/selectOptionList.do")
    public List<Map<String,String>> selectOptionList(){
        return brandService.selectOptionList();
    }

}
