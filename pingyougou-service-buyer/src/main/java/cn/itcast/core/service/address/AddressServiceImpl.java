package cn.itcast.core.service.address;

import cn.itcast.core.dao.address.AddressDao;
import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.pojo.address.AddressQuery;
import com.alibaba.dubbo.config.annotation.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
public class AddressServiceImpl implements AddressService {
    @Resource
    private AddressDao addressDao;
    //加载当前收货人的地址列表
    @Override
    public List<Address> findListByLoginUser(String userId) {
        AddressQuery addressQuery = new AddressQuery();
        addressQuery.createCriteria().andUserIdEqualTo(userId);
        List<Address> list = addressDao.selectByExample(addressQuery);
        return list;
    }
}
