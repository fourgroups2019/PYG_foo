package cn.itcast.core.service.user;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.utils.md5.MD5Util;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class UserServiceImpl implements UserService {
//    发送数据  手机号
    @Resource
    private JmsTemplate jmsTemplate;
    @Resource
    private Destination smsDestination;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UserDao userDao;
    @Override
    public void sendCode(final String phone) {
        //随机生成6位验证码
        final String code = RandomStringUtils.randomNumeric(6);
        System.out.println("code:"+code);
        // 将验证码存储
        redisTemplate.boundValueOps(phone).set(code);
        // 设置该验证码的过期时间
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);

        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("phoneNumbers",phone);//设置手机号到消息体
                mapMessage.setString("signName","阮文");//设置短息签名
                mapMessage.setString("templateCode","SMS_140720901");//设置短信模板
                mapMessage.setString("templateParam","{\"code\":\""+code+"\"}");//设置验证码
                return mapMessage;
            }
        });
    }

    @Override
    public void add(String smscode, User user) {

        String code = (String) redisTemplate.boundValueOps(user.getPhone()).get();
        if(code != null && smscode != null && !"".equals(smscode) && code.equals(smscode)){
            // 用户密码需要加密：md5加密
            String password = MD5Util.MD5Encode(user.getPassword(), "");
            user.setPassword(password);
            user.setCreated(new Date());
            user.setUpdated(new Date());
            userDao.insertSelective(user);
        }else{
            throw new RuntimeException("验证码不正确");
        }
    }
}
