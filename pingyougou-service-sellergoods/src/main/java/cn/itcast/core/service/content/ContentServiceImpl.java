package cn.itcast.core.service.content;

import java.util.List;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.ad.ContentQuery;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@Service
public class ContentServiceImpl implements ContentService {

	@Resource
	private ContentDao contentDao;
	@Resource
	private RedisTemplate redisTemplate;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getResult(),page.getTotal());
	}

	@Override
	public void add(Content content) {
		//添加之前清除缓存
		clearCache(content.getCategoryId());
		contentDao.insertSelective(content);
	}



	@Override
	public void edit(Content content) {
		//判断分类id是否一致
		Long newCategoryId = content.getCategoryId();
		Long oldCategoryId = contentDao.selectByPrimaryKey(content.getId()).getCategoryId();
		if (newCategoryId!=oldCategoryId){
			clearCache(newCategoryId);
			clearCache(oldCategoryId);
		}else {
			clearCache(newCategoryId);
		}
		contentDao.updateByPrimaryKeySelective(content);
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				Content content = contentDao.selectByPrimaryKey(id);
//				删除之前更新缓存
				clearCache(content.getCategoryId());
				contentDao.deleteByPrimaryKey(id);
			}
		}
	}

	/**
	 * 首页大广告的轮播图
	 * @param categoryId
	 * @return
	 */
	@Override
	public List<Content> findByCategoryId(Long categoryId) {
//		判断缓存中是否有数据
		List<Content> list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
		synchronized (this){
			list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
			if (list==null){
//				缓存中没有数据从数据库中查询
				ContentQuery contentQuery = new ContentQuery();
				contentQuery.createCriteria().andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");//状态为 1 的广告 审核通过
				contentQuery.setOrderByClause("sort_order desc");
				list=contentDao.selectByExample(contentQuery);
				// 3、将数据放入缓存中
				redisTemplate.boundHashOps("content").put(categoryId,list);

			}

		}
          return list;
	}

	private void clearCache(Long categoryId) {
		redisTemplate.boundHashOps("content").delete(categoryId);
	}


}
