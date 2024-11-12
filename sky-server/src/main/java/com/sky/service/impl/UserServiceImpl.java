package com.sky.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    WeChatProperties weChatProperties;

    @Autowired
    UserMapper userMapper;

    public User wxlogin(UserLoginDTO userLoginDTO) {
        // 获取openid
        String openid = getOpenId(userLoginDTO);

        // 如果没有获取到openid，抛出异常
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 判断给定openid的用户是否存在， 如果不存在则创建
        User user = userMapper.getByOpenId(openid);

        if (user == null) {
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }

        return user;
    }


    // 向微信接口发送http请求，以获取用户openid
    private String getOpenId(UserLoginDTO userLoginDTO) {
        Map<String, String> params = new HashMap();
        params.put("appid", weChatProperties.getAppid());
        params.put("secret", weChatProperties.getSecret());
        params.put("js_code", userLoginDTO.getCode());
        params.put("grant_type", "authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN, params);
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");

        return openid;
    }
}
