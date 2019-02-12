package cn.itcast.core.controller.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {
//    显示当前登录人
    @RequestMapping("/name.do")
    public Map<String,String>name(){
        HashMap<String, String> map = new HashMap<>();
        String loginname = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName",loginname);
        return map;
    }
}
