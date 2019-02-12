package cn.itcast.core.service.seller;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class SellerServiceImpl implements SellerService {
    @Resource
    private SellerDao sellerDao;

    @Transactional
    @Override
    public void add(Seller seller) {
        seller.setStatus("0");//未审核
        seller.setCreateTime(new Date());//提交日期
//        密码加盐加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String password = bCryptPasswordEncoder.encode(seller.getPassword());
        seller.setPassword(password);

        sellerDao.insertSelective(seller);
    }

//    商家入驻审核
    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
//        设置分页条件
        PageHelper.startPage(page,rows);
//        设置分页查询条件
        SellerQuery sellerQuery = new SellerQuery();
        if (seller.getStatus()!=null&&!"".equals(seller.getStatus().trim())){
            sellerQuery.createCriteria().andStatusEqualTo(seller.getStatus().trim());
        }
        //查询
        Page<Seller> p = (Page<Seller>) sellerDao.selectByExample(sellerQuery);
//        封装数据
        return new PageResult(p.getResult(),p.getTotal());
    }

    @Override
    public Seller findOne(String sellerId) {
        return sellerDao.selectByPrimaryKey(sellerId);
    }

    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
