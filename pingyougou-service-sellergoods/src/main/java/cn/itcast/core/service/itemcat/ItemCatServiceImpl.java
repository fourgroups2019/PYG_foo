package cn.itcast.core.service.itemcat;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;
@Service
public class ItemCatServiceImpl implements ItemCatService {
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        //点击列表查询时将数据放入缓存中
        List<ItemCat> itemCats = itemCatDao.selectByExample(null);
        if (itemCats!=null&&itemCats.size()>0){
            for (ItemCat itemCat : itemCats) {
                //将分类名称 模板id存储到redis中
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
            }
        }
//        根据条件查询
        ItemCatQuery itemCatQuery = new ItemCatQuery();
        itemCatQuery.createCriteria().andParentIdEqualTo(parentId);
        return itemCatDao.selectByExample(itemCatQuery);

    }

    @Override
    public ItemCat findOne(Long id) {
        return itemCatDao.selectByPrimaryKey(id);
    }

    @Override
    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }

    /**
     * 新增分类
     * @param itemCat
     */
    @Override
    public void add(ItemCat itemCat) {
        itemCatDao.insertSelective(itemCat);
    }
}
