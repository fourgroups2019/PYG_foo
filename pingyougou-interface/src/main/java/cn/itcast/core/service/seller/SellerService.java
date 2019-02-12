package cn.itcast.core.service.seller;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;

public interface SellerService {
//    商家申请入驻
    public void add(Seller seller);
//    商家入驻待审核
    public PageResult search(Integer page,Integer rows,Seller seller);
//    商家审核详情页面
    public Seller findOne(String sellerId);
//    审核操作
    public void updateStatus(String sellerId,String status);
}
