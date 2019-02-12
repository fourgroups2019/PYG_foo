package cn.itcast.core.service.order;

import cn.itcast.core.pojo.order.Order;

public interface OrderService {
    //提交订单 保存到数据库操作
   public void add(String username, Order order);
}
