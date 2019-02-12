package cn.itcast.core.service.cart;

import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;

import java.util.List;

public interface CartService {
    /**
     * 根据库存id获取到商家id
     * @param id
     * @return
     */
    public Item findOne(Long id);

    /**
     * 填充购物车中的数据
     * @param cartList
     * @return
     */
    List<Cart> autoDataToCart(List<Cart> cartList);

    /**
     * 将购物车添加到redis中
     * @param username
     * @param newCartList
     */
    void saveCartToRedis(String username, List<Cart> newCartList);

    //从redis中回显购物车
    List<Cart> findCartListFromRedis(String username);
}
