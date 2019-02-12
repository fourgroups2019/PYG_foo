package cn.itcast.core.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

/**
        * 自定义认证类
        */
public class UserDetailServiceImpl implements UserDetailsService {
           /**
            * 登录成功后：需要对该用户进行授权
            * @param username
            * @return
            * @throws UsernameNotFoundException
            */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //授权
        Set<GrantedAuthority> authorities = new HashSet<>();
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        authorities.add(simpleGrantedAuthority);
        User user = new User(username, "", authorities);
        return user;
    }
}
