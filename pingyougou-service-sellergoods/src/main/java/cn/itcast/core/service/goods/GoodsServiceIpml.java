package cn.itcast.core.service.goods;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.staticpage.StaticPageService;
import cn.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoodsServiceIpml implements GoodsService {
    @Resource
    private GoodsDao goodsDao;

    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemDao itemDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private BrandDao brandDao;

    @Resource
    private SellerDao sellerDao;
//    solr 索引库
    @Resource
    private SolrTemplate solrTemplate;


    @Resource
    private JmsTemplate jmsTemplate;

    @Resource
    private Destination topicPageAndSolrDestination;
    @Resource
    private Destination queueSolrDeleteDestination;

    @Transactional
    @Override
    public void add(GoodsVo goodsVo) {
//        保存商品
        Goods goods = goodsVo.getGoods();
        goodsDao.insertSelective(goods);

//        保存商品描述
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDesc.setGoodsId(goods.getId());
        goodsDescDao.insertSelective(goodsDesc);

//        保存库存...

        //是否启用规格
        if ("1".equals(goods.getIsEnableSpec())){
            // 启用规格：一个spu对应多个sku
            List<Item> itemList = goodsVo.getItemList();
            if (itemList!=null&&itemList.size()>0){
                for (Item item : itemList) {

                    // 商品的标题：spu的名称+spu副标题+规格名称
                    String title = goods.getGoodsName()+" "+goods.getCaption();
//                设置规格名称
                    String spec =item.getSpec();
                    Map<String,String> specMap = JSON.parseObject(spec, Map.class);
                    Set<Map.Entry<String, String>> entries = specMap.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        title +=" "+entry.getValue();
                    }
                    item.setTitle(title);
                    setItemAttribute(goods, goodsDesc, item);
                    // 保存
                    itemDao.insertSelective(item);

                }
            }
        }else {
            // 不启用规格：一个spu对应一个sku
            Item item = new Item();
            item.setTitle(goods.getGoodsName() + " " + goods.getCaption()); // 标题
            item.setPrice(goods.getPrice());    // 商品价格
            item.setStatus("1");    // 是否启用该商品
            item.setNum(9999);      // 库存量
            item.setIsDefault("1"); // 是否默认
            item.setSpec("{}");
            setItemAttribute(goods, goodsDesc, item);
            itemDao.insertSelective(item);
        }


    }

    //    	商品管理列表查询
    @Override
    public PageResult searchForShop(Integer page, Integer rows, Goods goods) {
        //设置分页条件
        PageHelper.startPage(page,rows);
        //设置查询条件
        GoodsQuery goodsQuery = new GoodsQuery();
         goodsQuery.setOrderByClause("id desc");
         if (goods.getSellerId()!=null&&!"".equals(goods.getSellerId().trim())){
             goodsQuery.createCriteria().andSellerIdEqualTo(goods.getSellerId().trim());
         }
//         查询
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);

        return new PageResult(p.getResult(),p.getTotal());
    }
//修改回显
    @Override
    public GoodsVo findOne(Long id) {
//        商品信息
        GoodsVo goodsVo = new GoodsVo();
        Goods goods = goodsDao.selectByPrimaryKey(id);
        goodsVo.setGoods(goods);
//        商品明细信息
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        goodsVo.setGoodsDesc(goodsDesc);
//        商品库存表信息
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        goodsVo.setItemList(itemList);

        return goodsVo;
    }
//修改 更新商品
    @Override
    public void update(GoodsVo goodsVo) {
//        更新商品
        Goods goods = goodsVo.getGoods();
        goodsDao.updateByPrimaryKeySelective(goods);
//        更新商品明细信息
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDescDao.updateByPrimaryKeySelective(goodsDesc);
//        更新商品对应的库存 先删除
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemDao.selectByExample(itemQuery);
//        再插入  保存
        //是否启用规格
        if ("1".equals(goods.getIsEnableSpec())){
            // 启用规格：一个spu对应多个sku
            List<Item> itemList = goodsVo.getItemList();
            if (itemList!=null&&itemList.size()>0){
                for (Item item : itemList) {

                    // 商品的标题：spu的名称+spu副标题+规格名称
                    String title = goods.getGoodsName()+" "+goods.getCaption();
//                设置规格名称
                    String spec =item.getSpec();
                    Map<String,String> specMap = JSON.parseObject(spec, Map.class);
                    Set<Map.Entry<String, String>> entries = specMap.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        title +=" "+entry.getValue();
                    }
                    item.setTitle(title);
                    setItemAttribute(goods, goodsDesc, item);
                    // 保存
                    itemDao.insertSelective(item);

                }
            }
        }else {
            // 不启用规格：一个spu对应一个sku
            Item item = new Item();
            item.setTitle(goods.getGoodsName() + " " + goods.getCaption()); // 标题
            item.setPrice(goods.getPrice());    // 商品价格
            item.setStatus("1");    // 是否启用该商品
            item.setNum(9999);      // 库存量
            item.setIsDefault("1"); // 是否默认
            item.setSpec("{}");
            setItemAttribute(goods, goodsDesc, item);
            itemDao.insertSelective(item);
        }


    }
    //* 运营系统查询待审核的商品列表
    @Override
    public PageResult searchForManager(Integer page, Integer rows, Goods goods) {
        // 设置分页条件
        PageHelper.startPage(page, rows);
        // 设置查询条件：待审核并且是未删除
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        if(goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus().trim())){
            criteria.andAuditStatusEqualTo(goods.getAuditStatus().trim());
        }
        criteria.andIsDeleteIsNull();   // null：未删除   1：已删除  MySQL查询：建议is null
        goodsQuery.setOrderByClause("id desc");
        // 根据条件查询
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        // 封装结果
        return new PageResult(p.getResult(), p.getTotal());
    }

    @Transactional
    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids!=null&&ids.length>0){
            Goods goods = new Goods();
            goods.setAuditStatus(status);
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                if ("1".equals(status)){ //审核通过后
                    // 说明：今天将所有的库存信息保存到索引库中(目的：为了索引库中有很多的数据，可以搜索操作)
                    //dataImportToSolrForItem();
                    //  正真的实现：将审核通过后的商品对应的库存保存到索引库中
                    //updateItemToSolr(id);
                    // 生成静态页面
                    //staticPageService.getHtml(id);
                   // 需要将商品id发送到mq中
                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            // 将商品的id封装成消息体进行发送
                            TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                            return textMessage;
                        }
                    });
                }

            }
        }
    }

    private void updateItemToSolr(Long id) {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id).andIsDefaultEqualTo("1").andStatusEqualTo("1");
        List<Item> items = itemDao.selectByExample(itemQuery);;
        if(items != null && items.size() > 0){
            // 处理动态字段
            for (Item item : items) {
                // 栗子：{"机身内存":"16G","网络":"联通3G"}
                String spec = item.getSpec();
                // 拼接的动态字段：item_spec_机身内存 、 item_spec_网络
                Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(items);
            solrTemplate.commit();
        }

    }

    /**
     * 将全部库存的数据保存到索引库中
     */
    private void dataImportToSolrForItem() {
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andStatusEqualTo("1"); // 将正常的库存信息保存到索引库中
        List<Item> items = itemDao.selectByExample(itemQuery);
        if(items != null && items.size() > 0){
            // 处理动态字段
            for (Item item : items) {
                // 栗子：{"机身内存":"16G","网络":"联通3G"}
                String spec = item.getSpec();
                // 拼接的动态字段：item_spec_机身内存 、 item_spec_网络
                Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(items);
            solrTemplate.commit();
        }


    }

    //    逻辑删除商品
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if (ids!=null&&ids.length>0){
            Goods goods = new Goods();
            goods.setIsDelete("1");
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                // 更新索引库
//                SimpleQuery query = new SimpleQuery("item_goodsid:"+id);
//                solrTemplate.delete(query);
//                solrTemplate.commit();
                // 删除静态页面(不删除静态页)
                //删除索引库中的数据  1,发送消息到消息队列
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                        return textMessage;
                    }
                });

            }
        }
    }


    private void setItemAttribute(Goods goods, GoodsDesc goodsDesc, Item item) {
        // 商品图片
        // 例子：
        // [{"color":"粉色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXq2AFIs5AAgawLS1G5Y004.jpg"},
        // {"color":"黑色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXrWAcIsOAAETwD7A1Is874.jpg"}]
        String itemImages = goodsDesc.getItemImages();
        List<Map> images = JSON.parseArray(itemImages, Map.class);
        if(images != null && images.size() > 0){
            String image = images.get(0).get("url").toString();
            item.setImage(image);
        }
        // 商品三级分类id
        item.setCategoryid(goods.getCategory3Id());
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        item.setGoodsId(goods.getId()); // 商品id
        item.setSellerId(goods.getSellerId());  // 商家id
        item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName()); // 分类名称
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());    // 品牌名称
        item.setSeller(sellerDao.selectByPrimaryKey(goods.getSellerId()).getNickName());   // 商家店铺名称

    }
}
