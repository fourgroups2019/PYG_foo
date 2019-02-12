package cn.itcast.core.service.search;

import java.util.Map;

public interface ItemSearchService {
    //前台系统的搜索
    public Map<String,Object> search(Map<String,String> searchMap);
    //将审核通过后的商品对应的库存保存到索引库中
    public void updateItemToSolr(Long id);
   // 从索引库中删除商品
    public void deleteItemFromSolr(Long id);
}
