package cn.itcast.core.service.staticpage;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticPageServiceImpl implements StaticPageService, ServletContextAware {

    @Resource
    private GoodsDao goodsDao;

    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private ItemDao itemDao;


    private Configuration configuration;
    // 注入FreeMarkerConfigurer：获取到Configuration并且可以指定模板的位置
    public void setFreeMarkerConfigurer(FreeMarkerConfigurer freeMarkerConfigurer) {
        this.configuration = freeMarkerConfigurer.getConfiguration();
    }

    // 注入ServletContext
    private ServletContext servletContext;
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * 生成商品详情的静态页
     * @param id
     */
    @Override
    public void getHtml(Long id) {
        try {
            // 1、创建Configuration并且指定模板的位置
            //  new Configuration... 指定模板的位置：硬编码了
            // 联想：springmvc中指定视图的位置在配置文件中配置 --- 希望模板在配置文件中配置
            // 不能new Configuration，此路不通了。通过spring注入Configuration。springmvc支持Freemarker的。
            // 注入FreeMarkerConfigurer好处：获取到Configuration并且可以指定模板的位置
            // 2、获取该位置下的模板
            Template template = configuration.getTemplate("item.ftl");
            // 3、准备数据（真实的业务数据）
            Map<String, Object> dataModel = getDataModel(id);
            // 创建file：生成的静态页的位置。
            // 注意：生成的静态页是可以直接访问的---静态页在项目发布的真实路径下
            // request.getServletContext.getRealpath("/")：此路不通，在服务里获取不到request
            String pathname = "/" + id + ".html";
            String realPath = servletContext.getRealPath(pathname);
            File file = new File(realPath);
            // 4、模板 + 数据 = 输出（生成静态页面）
            Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            template.process(dataModel, out);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 获取业务数据
    private Map<String,Object> getDataModel(Long id) {
        Map<String,Object> dataModel = new HashMap<>();
        // 1、spu的副标题
        Goods goods = goodsDao.selectByPrimaryKey(id);
        dataModel.put("goods", goods);
        // 2、商品图片、描述、售后
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        dataModel.put("goodsDesc", goodsDesc);
        // 3、商品的分类
        ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
        ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
        ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
        dataModel.put("itemCat1", itemCat1);
        dataModel.put("itemCat2", itemCat2);
        dataModel.put("itemCat3", itemCat3);
        // 4、该商品对应的库存信息
        ItemQuery itemQuery = new ItemQuery();
        // 查询有库存的商品信息
        itemQuery.createCriteria().andGoodsIdEqualTo(id).andStatusEqualTo("1").andNumGreaterThan(0);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        dataModel.put("itemList", itemList);
        return dataModel;
    }


}
