package com.example.cunion.config.shiro;

import com.example.cunion.entity.User;
import com.example.cunion.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class OAuth2Realm extends AuthorizingRealm {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /**
     *
     * @param collection
     * @return 授权（验证权限时调用）
     * @throws AuthenticationException
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection collection) {
        User user = (User) collection.getPrimaryPrincipal();
        String userId = user.getId();
        //查询用户的权限列表
        Set<String> permsSet = userService.searchUserRoleByUserId(userId);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        //把权限列表添加到info对象中
        info.setStringPermissions(permsSet);
        return info;
    }

    /**
     *
     * @param token
     * @return 认证（登录时调用）
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String accessToken = (String) token.getPrincipal();
        String userId = jwtUtil.getUserId(accessToken);
        User user = userService.getLoginUser(userId);

        //从令牌中获取userId， 然后检测该账户是否被冻结。
        if (user == null){
            throw new LockedAccountException("账号已被锁定，请联系管理员");
        }
        //往info对象中添加用户信息、Token字符串
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());
        return info;
    }
}
