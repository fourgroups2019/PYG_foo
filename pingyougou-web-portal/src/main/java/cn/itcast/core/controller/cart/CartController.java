package cn.itcast.core.controller.cart;

import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.cart.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;


    // 需要服务器端也支持CORS，设置响应的头信息
//            response.setHeader("Access-Control-Allow-Origin", "http://localhost:9003");
//            response.setHeader("Access-Control-Allow-Credentials", "true"); // 服务器端支持携带cookie

    /**
     * 将商品加入购物车中
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList.do")
//    @CrossOrigin(origins = {"http://localhost:9003"}, allowCredentials = "true")
    @CrossOrigin(origins = {"http://localhost:9003"})
    public Result addGoodsToCartList(Long itemId, Integer num,
                                     HttpServletRequest request, HttpServletResponse response){

        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            // 1、定义一个空车集合
            List<Cart> cartList = null;
            // 在实际的开发中：定义开关
            boolean flag = false;
            // 2、判断本地是否有购物车
            Cookie[] cookies = request.getCookies();
            if(cookies != null && cookies.length > 0){
                for (Cookie cookie : cookies) {
                    if("BUYER_CART".equals(cookie.getName())){
                        // 3、本地有购物车，需要取出来
                        //String text = cookie.getValue(); // 购物车的json串
                        cartList= JSON.parseArray(URLDecoder.decode(cookie.getValue(),"utf-8"),Cart.class);//解码
                        //cartList = JSON.parseArray(text, Cart.class);
                        flag = true;
                        break;  // 一旦找到购物车，直接跳出循环
                    }
                }
            }
            // 4、判断cartList是否为null，如果为null说明是第一次将商品加入购物车
            // 购物车：需要自己去创建
            if(cartList == null){
                cartList = new ArrayList<>();
            }
            // 经过上面的步骤，一定有车了
            // 将请求的数据封装到cart中
            Cart cart = new Cart();
            Item item = cartService.findOne(itemId);
            cart.setSellerId(item.getSellerId());   // 存储的商家id
            List<OrderItem> orderItemList = new ArrayList<>();
            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(itemId);    // 库存id
            orderItem.setNum(num);          // 购买数量
            orderItemList.add(orderItem);

            cart.setOrderItemList(orderItemList);
            // 5、将商品进行装车
            // 5-1、判断商品是否属于同一个商家：只需要判断商家id是否相同即可
//            cartList.contains() // 没问题，但是这个方法找不到具体的位置
            int sellerIndexOf = cartList.indexOf(cart);// 说明：判断商品是否属于同一个商家
            if(sellerIndexOf != -1){
                // 同一个商家：继续判断，判断是否属于同款商品
                Cart oldCart = cartList.get(sellerIndexOf);
                List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                int itemIndexOf = oldOrderItemList.indexOf(orderItem); // 判断是否是同款商品
                if(itemIndexOf != -1){
                    // 同款商品，合并购买数量
                    OrderItem oldOrderItem = oldOrderItemList.get(itemIndexOf);
                    oldOrderItem.setNum(oldOrderItem.getNum() + num);
                }else{
                    // 同商家但不同款
                    oldOrderItemList.add(orderItem);
                }
            }else{
                // 不是同一个商家
                cartList.add(cart);
            }
            // 判断用户是否登录：未登录：保存到客户端  已登录：保存到服务器端
            if (!"anonymousUser".equals(username)){
                // 6-1、已登录：保存到服务器端
                cartService.saveCartToRedis(username,cartList);
                //清空购物车
                if (flag){
                    //String s = JSON.toJSONString(cartList);
                    Cookie cookie = new Cookie("BUYER_CART", null);
                    cookie.setPath("/"); // cookie共享
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);

                }
            }else {
                // 6-2、未登录：保存到客户端
                // 6、将购物车保存到本地
                //Cookie cookie = new Cookie("BUYER_CART", JSON.toJSONString(cartList));
                // 6、将购物车保存到cookie
                String s = JSON.toJSONString(cartList);
                Cookie cookie = new Cookie("BUYER_CART", URLEncoder.encode(s, "utf-8") );
                cookie.setPath("/"); // cookie共享
                cookie.setMaxAge(60 * 60);
                response.addCookie(cookie);
            }
            return new Result(true, "商品成功加入购物车");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, "加入购物车失败");
        }
    }

    /**
     * 回显购物车中的列表数据
     * @return
     */
    @RequestMapping("/findCartList.do")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response) throws UnsupportedEncodingException {
        // 未登录：从cookie中获取
        List<Cart> cartList = null;
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0){
            for (Cookie cookie : cookies) {
                if("BUYER_CART".equals(cookie.getName())){
                    //String text = cookie.getValue(); // 购物车的json串
                    cartList= JSON.parseArray(URLDecoder.decode(cookie.getValue(),"utf-8"),Cart.class);
                    //cartList = JSON.parseArray(text, Cart.class);
                    break;
                }
            }
        }
        //  已登录：从redis中获取
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"anonymousUser".equals(username)){
            // 思考一个场景：登录成功后，在前台系统中，【我的购物车】，将本地的购物车同步到redis中
            if (cartList!=null){
                cartService.saveCartToRedis(username, cartList);
                //清空购物车
                Cookie cookie = new Cookie("BUYER_CART", null );
                cookie.setPath("/"); // cookie共享
                cookie.setMaxAge(0);
                response.addCookie(cookie);

            }
            // 从redis中获取
            cartList = cartService.findCartListFromRedis(username);
        }

        // 对购物车中的数据进行填充
        if(cartList != null && cartList.size() > 0){
            cartList = cartService.autoDataToCart(cartList);
        }

        return cartList;
    }
}
