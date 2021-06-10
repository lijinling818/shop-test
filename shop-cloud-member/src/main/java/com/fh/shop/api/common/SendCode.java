package com.fh.shop.api.common;

import com.alibaba.fastjson.JSON;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import com.fh.shop.util.SMSutil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("api")
public class SendCode {

    @PostMapping("/sms/sendCode")
    public ServerResponse sendCode(String phone){
        //验证手机号非空
        if (StringUtils.isEmpty(phone)){
            return ServerResponse.error(ResponseEnum.MEMBER_PHONE_IS_NULL);
        }
        //验证手机号是否合法


        //调用工具类发送短信
        String result = SMSutil.send(phone);
        //将json格式的字符串转为java对象
        Map resultMap = JSON.parseObject(result, Map.class);
        int code = (int) resultMap.get("code");
        if (code!=200){
            return ServerResponse.error(ResponseEnum.SMS_IS_ERROR);
        }
        String obj = (String) resultMap.get("obj");
        //获取验证码存入redis中
        RedisUtil.setex(phone,obj,2*60);
        return ServerResponse.success();
    };


}
