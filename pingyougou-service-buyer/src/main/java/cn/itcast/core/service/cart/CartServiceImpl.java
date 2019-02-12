package cn.itcast.core.service.cart;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Resource
    private ItemDao itemDao;

    @Resource
    private SellerDao sellerDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据库存id获取到商家id
     * @param id
     * @return
     */
    @Override
    public Item findOne(Long id) {
        return itemDao.selectByPrimaryKey(id);
    }

    /**
     * 填充购物车中的数据
     * @param cartList
     * @return
     */
    @Override
    public List<Cart> autoDataToCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            Seller seller = sellerDao.selectByPrimaryKey(cart.getSellerId());
            cart.setSellerName(seller.getNickName());   // 商家店铺名称
            List<OrderItem> orderItemList = cart.getOrderItemList();
            // 填充购物项中的数据
            for (OrderItem orderItem : orderItemList) {
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                orderItem.setPicPath(item.getImage());  // 商品图片
                orderItem.setTitle(item.getTitle());    // 商品标题
                orderItem.setPrice(item.getPrice());    // 商品单价
                Double totalFee = item.getPrice().doubleValue() * orderItem.getNum();
                orderItem.setTotalFee(new BigDecimal(totalFee));        // 商品小计
            }
        }
        return cartList;
    }

    /**
     * 将购物车保存到redis中
     * @param username
     * @param newCartList
     */
    @Override
    public void saveCartToRedis(String username, List<Cart> newCartList) {
        // 取出老车
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        // 将新车合并到老车中
        oldCartList = mergeNewCartListToOldCartList(newCartList, oldCartList);
        // 将老车保存到redis中
        redisTemplate.boundHashOps("BUYER_CART").put(username, oldCartList);
    }

    /**
     * 从redis中获取
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart>  cartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        return cartList;
    }

    /**
     * 将新车合并到老车中
     * @param newCartList
     * @param oldCartList
     * @return
     */
    private List<Cart> mergeNewCartListToOldCartList(List<Cart> newCartList, List<Cart> oldCartList) {
        if(newCartList != null){
            if(oldCartList != null){
                // 新车和老车都不为空，进行合并（新车合并到老车中去）
                for (Cart newCart : newCartList) {
                    // 判断是否属于同一个商家
                    int sellerIndexOf = oldCartList.indexOf(newCart);
                    if(sellerIndexOf != -1){
                        // 同一个商家
                        // 继续判断是否是同款商品（判断的是购物项）
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();
                        List<OrderItem> oldOrderItemList = oldCartList.get(sellerIndexOf).getOrderItemList();
                        // 遍历新车的购物项
                        for (OrderItem newOrderItem : newOrderItemList) {
                            int itemIndexOf = oldOrderItemList.indexOf(newOrderItem);
                            if(itemIndexOf != -1){
                                // 同款商品,合并数量
                                Integer newNum = newOrderItem.getNum();
                                OrderItem oldOrderItem = oldOrderItemList.get(itemIndexOf);
                                oldOrderItem.setNum(oldOrderItem.getNum() + newNum);
                            }else{
                                // 不是同款商品
                                oldOrderItemList.add(newOrderItem);
                            }

                        }
                    }else{
                        // 不是同一个商家：直接加入老车
                        oldCartList.add(newCart);
                    }
                }
            }else{
                return newCartList;
            }
        }else{
            return oldCartList;
        }

        return oldCartList;
    }
}
