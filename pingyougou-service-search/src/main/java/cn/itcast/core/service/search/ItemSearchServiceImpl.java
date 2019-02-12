package cn.itcast.core.service.search;


import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.aspectj.weaver.ast.Var;
import org.jasig.cas.client.authentication.SimpleGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.annotation.Resource;
import java.util.*;

//前台系统的搜索
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Resource
    private SolrTemplate solrTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ItemDao itemDao;
    //前台系统的搜索
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
//        搜索关键字处理
        String keywords = searchMap.get("keywords");
        if (keywords!=null&&!"".equals(keywords)){
            keywords  = keywords.replace(" ", "");
            searchMap.put("keywords",keywords);
        }
        //1,根据关键字检索并且进行分页
        Map<String, Object> resultMap = new HashMap<>();

       // Map<String ,Object> map = searchForPage(searchMap);
        // 1、根据关键字检索并且进行分页 并且检索的内容高亮
        Map<String, Object> map = searchForHighlightForPage(searchMap);
        resultMap.putAll(map);
        //2,获取商品的分类列表
        // 2、获取商品的分类列表
        List<String> categoryList = searchForGroupPage(searchMap);
        if(categoryList != null && categoryList.size() > 0){
            resultMap.put("categoryList", categoryList);

            // 3、获取商品的品牌、规格列表
            Map<String, Object> brandsAndSpecsMap = searchBrandsAndSpecsByCategoryName(categoryList.get(0));//传递分类名称
            resultMap.putAll(brandsAndSpecsMap);
        }

        return resultMap;
    }

   // 将审核通过后的商品对应的库存保存到索引库中
    @Override
    public void updateItemToSolr(Long id) {
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

    @Override
    public void deleteItemFromSolr(Long id) {
        SimpleQuery query = new SimpleQuery("item_goodsid:"+id);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    // 获取品牌结果集以及规格结果集
    private Map<String, Object> searchBrandsAndSpecsByCategoryName(String categoryName) {
        Map<String,Object> map = new HashMap<>();
        // 通过分类获取模板的id
        Object typeId = redisTemplate.boundHashOps("itemCat").get(categoryName);
        // 通过模板id获取品牌结果集、规格结果集
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        map.put("brandList", brandList);
        // 通过模板id获取品牌结果集、规格结果集
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        map.put("specList",specList);
        return map;
    }

    private List<String> searchForGroupPage(Map<String, String> searchMap) {
        // 1、设置检索条件
        Criteria criteria = new Criteria("item_keywords"); // 根据哪个字段检索
        String keywords = searchMap.get("keywords");
        if(keywords != null && !"".equals(keywords)){
            criteria.is(keywords);
        }
        SimpleQuery query = new SimpleQuery(criteria);
        // 2、封装分组条件
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category");  // 设置根据哪个字段进行分组
        query.setGroupOptions(groupOptions);
        // 3、根据条件检索
        List<String> categoryList = new ArrayList<>();  // 封装分组结果
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);
        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");//要拿到那个字段下的分组结果
        Page<GroupEntry<Item>> groupEntries = groupResult.getGroupEntries();
        for (GroupEntry<Item> groupEntry : groupEntries) {
            String value = groupEntry.getGroupValue();
            categoryList.add(value);
        }
        return categoryList;
    }

    // 1、根据关键字检索并且进行分页 并且检索的内容高亮
    private Map<String,Object> searchForHighlightForPage(Map<String, String> searchMap) {
        // 1、封装检索的关键字
        Criteria criteria = new Criteria("item_keywords"); // 根据哪个字段检索
        String keywords = searchMap.get("keywords");
        if(keywords != null && !"".equals(keywords)){
            criteria.is(keywords);
        }
        SimpleHighlightQuery query = new SimpleHighlightQuery(criteria);
        // 2、封装分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        Integer offset = (pageNo - 1) * pageSize;
        query.setOffset(offset);  // 其始行
        query.setRows(pageSize);    // 每页显示的条数
        //3,封装高亮条件
        HighlightOptions highlightOptions  = new HighlightOptions();
        highlightOptions.setSimplePrefix("<font color='red'>");
        highlightOptions.setSimplePostfix("</font>");
        highlightOptions.addField("item_title");                // 对该字段中包含的关键字高亮
        query.setHighlightOptions(highlightOptions);

        // 4、继续封装过滤的条件 select * from table where item_keywrods like ? and cate
        // 根据商品分类过滤
        String category = searchMap.get("category");
        if(category != null && !"".equals(category)){
            Criteria cri = new Criteria("item_category");
            cri.is(category);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        // 根据商品的品牌过滤
        String brand = searchMap.get("brand");
        if(brand != null && !"".equals(brand)){
            Criteria cri = new Criteria("item_brand");
            cri.is(brand);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        // 根据商品的价格过滤
        String price = searchMap.get("price");
        if(price != null && !"".equals(price)){
            // 页面传递的价格区间段：min-max   xxx以上 xxx-*
            String[] prices = price.split("-");
            if(price.contains("*")){ // xxx以上
                Criteria cri = new Criteria("item_price");
                cri.greaterThanEqual(prices[0]);
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
                query.addFilterQuery(filterQuery);
            }else{ // 区间段
                Criteria cri = new Criteria("item_price");
                cri.between(prices[0], prices[1], true, true);
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
                query.addFilterQuery(filterQuery);
            }
        }
        // 根据商品的规格过滤
        // {"内存大小":"16G","网络":"联通4G"}
        String spec = searchMap.get("spec");
        if(spec != null && !"".equals(spec)){
            // item_spec_*：需要拼接动态字段
            Map<String, String> specMap = JSON.parseObject(spec, Map.class);
            Set<Map.Entry<String, String>> entries = specMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                Criteria cri = new Criteria("item_spec_" + entry.getKey());
                cri.is(entry.getValue());
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
                query.addFilterQuery(filterQuery);
            }
        }
        // 5、根据商品价格以及新品进行排序
        // sortField：排序字段 ,sort：排序规则
        String sort = searchMap.get("sort");
        if (sort!=null&&!"".equals(sort)){
            if ("DESC".equals(sort)){
                // 降序
             Sort s = new Sort(Sort.Direction.DESC,"item_" + searchMap.get("sortField"));
             query.addSort(s);
            }else{
                Sort s = new Sort(Sort.Direction.ASC, "item_" + searchMap.get("sortField"));
                query.addSort(s);
            }
        }

        // 4、根据条件检索
        HighlightPage<Item> highlightPage = solrTemplate.queryForHighlightPage(query, Item.class);
         // 获取高亮的结果重新赋值给item的title属性
        List<HighlightEntry<Item>> highlighted = highlightPage.getHighlighted();
        if (highlighted!=null&&highlighted.size()>0){
            for (HighlightEntry<Item> highlightEntry : highlighted) {
                Item item = highlightEntry.getEntity();
                List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();
                if(highlights != null && highlights.size() > 0){
                    String title = highlights.get(0).getSnipplets().get(0); // 高亮结果
                    item.setTitle(title);
                }
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("totalPages", highlightPage.getTotalPages());  // 总页数
        map.put("total", highlightPage.getTotalElements());    // 总条数
        map.put("rows", highlightPage.getContent());           // 结果集
        return map;
    }

    //1,根据关键字检索并且进行分页
    private Map<String, Object> searchForPage(Map<String, String> searchMap) {
        // 1、封装检索的关键字
        Criteria criteria = new Criteria("item_keywords"); // 根据哪个字段检索
        String keywords = searchMap.get("keywords");
        if(keywords != null && !"".equals(keywords)){
            criteria.is(keywords);
        }
        SimpleQuery query = new SimpleQuery(criteria);
        // 2、封装分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        Integer offset = (pageNo - 1) * pageSize;
        query.setOffset(offset);  // 其始行
        query.setRows(pageSize);    // 每页显示的条数
//        // 3、根据条件检索
       ScoredPage<Item> scoredPage = solrTemplate.queryForPage(query, Item.class);
        Map<String,Object> map = new HashMap<>();
        map.put("totalPages", scoredPage.getTotalPages());  // 总页数
        map.put("total", scoredPage.getTotalElements());    // 总条数
        map.put("rows", scoredPage.getContent());           // 结果集
        return map;

    }
}
