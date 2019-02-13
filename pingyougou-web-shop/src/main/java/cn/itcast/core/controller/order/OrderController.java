package cn.itcast.core.controller.order;

import cn.itcast.core.entity.Result;
import cn.itcast.core.service.order.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Reference
    private OrderService orderService;

    /**
     * 订单发货
     * @param orderId
     * @return
     */
    @RequestMapping("/updateConsignStatus.do")
    public Result updateConsignStatus(Long orderId) {
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            orderService.updateConsignStatus(orderId,name);
            return new Result(true,"操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"操作失败");
        }
    }

}
