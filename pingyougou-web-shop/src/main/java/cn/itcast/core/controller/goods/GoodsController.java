package cn.itcast.core.controller.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.goods.GoodsService;
import cn.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    //测试
    @Reference
    private GoodsService goodsService;
    @RequestMapping("/add.do")
    public Result add(@RequestBody GoodsVo goodsVo){
        try {
            // 设置当前商家的id
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goodsVo.getGoods().setSellerId(sellerId);

            goodsService.add(goodsVo);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }

    //    	商品管理列表查询
    @RequestMapping("/search.do")
    public PageResult searchForShop(Integer page, Integer rows,@RequestBody Goods goods){
        // 设置当前的商家的id
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(sellerId);
        return goodsService.searchForShop(page,rows,goods);
    }

//    修改回显
    @RequestMapping("/findOne.do")
    public GoodsVo findOne(Long id){
        return goodsService.findOne(id);
    }

//    修改更新
    @RequestMapping("/update.do")
    public Result update(@RequestBody GoodsVo goodsVo){
        try {
            goodsService.update(goodsVo);
            return new Result(true,"更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新失败");
        }
    }

    //删除商品 下架
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            goodsService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    //商品的上下架 更新索引库
    @RequestMapping("/marketable.do")
    public Result update_is_marketable(Long[] ids,String marketable){
        try {
            goodsService.update_is_marketable(ids,marketable);
            return new Result(true,"更新索引库成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新索引库失败");

        }
    }
}
