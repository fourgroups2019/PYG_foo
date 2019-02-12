package cn.itcast.core.controller.address;

import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.service.address.AddressService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {
    @Reference
    private AddressService addressService;

    //加载当前收货人的地址列表
    @RequestMapping("/findListByLoginUser.do")
    public List<Address> findListByLoginUser(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.findListByLoginUser(userId);
    }
}
