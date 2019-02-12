package cn.itcast.core.service.pay;

import java.util.Map;

//
public interface PayService {
    //生成二维码
    public Map<String,String> createNative(String username)throws Exception;

    //查询支付结果
    public Map<String,String> queryPayStatus(String out_trade_no,String username)throws Exception;
}
