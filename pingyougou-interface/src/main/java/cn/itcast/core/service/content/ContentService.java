package cn.itcast.core.service.content;

import java.util.List;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.ad.Content;

public interface ContentService {

	public List<Content> findAll();

	public PageResult findPage(Content content, Integer pageNum, Integer pageSize);

	public void add(Content content);

	public void edit(Content content);

	public Content findOne(Long id);

	public void delAll(Long[] ids);
	//轮播图功能
	public List<Content> findByCategoryId(Long categoryId);

}
