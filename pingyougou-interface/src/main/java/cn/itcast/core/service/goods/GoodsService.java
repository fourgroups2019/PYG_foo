package cn.itcast.core.service.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.vo.GoodsVo;

public interface GoodsService {
//    保存商品
    public void add(GoodsVo goodsVo);

//    	商品管理列表查询
    public PageResult searchForShop(Integer page, Integer rows, Goods goods);

//    回显数据
    public GoodsVo findOne(Long id);
//    修改
    public void update(GoodsVo goodsVo);
//    运营商系统商品审核

public PageResult searchForManager(Integer page, Integer rows, Goods goods);
//商品审核
    public void updateStatus(Long[] ids,String status);
//    删除商品
    public void delete(Long[] ids);
}
