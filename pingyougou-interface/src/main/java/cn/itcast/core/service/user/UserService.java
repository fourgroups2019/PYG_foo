package cn.itcast.core.service.user;

import cn.itcast.core.pojo.user.User;

public interface UserService {
//    接收手机号
    public void sendCode(String phone);
//    用户注册
    public void add(String smscode,User user);
}
