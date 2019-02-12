package cn.itcast.core.controller.itemcat;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.itemcat.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {
    @Reference
    private ItemCatService itemCatService;
    @RequestMapping("/findOne.do")
    public ItemCat findOne(Long id){
        return itemCatService.findOne(id);
    }

    @RequestMapping("/findByParentId.do")
    public List<ItemCat> findByParentId(Long parentId){
       return itemCatService.findByParentId(parentId);
    }

    @RequestMapping("/findAll.do")
    public List<ItemCat> findAll(){
        return itemCatService.findAll();
    }


}
