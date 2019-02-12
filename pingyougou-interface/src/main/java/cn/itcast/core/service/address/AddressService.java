package cn.itcast.core.service.address;

import cn.itcast.core.pojo.address.Address;

import java.util.List;

public interface AddressService {
    //加载当前收货人的地址列表
    public List<Address> findListByLoginUser(String userId);
}
