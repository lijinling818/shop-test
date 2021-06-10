package com.fh.shop.api.file;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.member.vo.MemberVO;
import com.fh.shop.common.Contants;
import com.fh.shop.common.KeyUtil;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.Md5Util;
import com.fh.shop.util.RedisUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
public class JwtFilter extends ZuulFilter {
    @Value("${fh.shop.checkUrls}")
    private List<String> checkUrls;
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @SneakyThrows
    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String requestMethod = request.getMethod();
        if (requestMethod.equalsIgnoreCase("options")){
            //不进行处理，拦截
            currentContext.setSendZuulResponse(false);
            return null;
        }

        StringBuffer requestURL = request.getRequestURL();
        boolean isCheck =false;
        //如果requesturl包含 checkurl,就说明是需要检查的，就中断跳出；
        for (String checkUrl : checkUrls) {
            if (requestURL.indexOf(checkUrl)>0){
                isCheck =true;
                break;
            }
        }
        //如果不需要进行检查，就放行
         if (!isCheck){
             return null;
         }
         //需要进行验证的
        //判断是否有头信息
        //验证 【weofnsdsedgd.weetsdsgdsdg】
        String header = request.getHeader("x-auth");
        if (StringUtils.isEmpty(header)){
            return Result(ResponseEnum.TOKEN_IS_MISS);
        }
        //判断头信息是否完整
        String[] headerArr = header.split("\\.");
        if (headerArr.length!=2){
            return Result(ResponseEnum.TOKEN_IS_NOT_FULL);

        }
        //先分别获取用户的base64内容和签名的base64内容
        //验签【核心】
        String memberJsonBase64 = headerArr[0];
        String signBase64 = headerArr[1];
        //先把base64编码解码
        String memberJson= new String(Base64.getDecoder().decode(memberJsonBase64),"utf-8");
        String sign=new String(Base64.getDecoder().decode(signBase64),"utf-8");
        //将用户信息重新进行md5加密和携带的信息进行对比，如果一样，就通过
        String  newSign = Md5Util.sign(memberJson, Contants.SECRET);
        if (!newSign.equals(sign)){
            return Result(ResponseEnum.TOKEN_IS_FALL);
        }
        //下面要用到id,所有得将memberJson 转成对象从而获取id
        MemberVO memberVO = JSON.parseObject(memberJson,MemberVO.class);
        Long id = memberVO.getId();
        //判断是否过期
        if(!RedisUtil.exist(KeyUtil.buileMemberKey(id))){
            return Result(ResponseEnum.TOKEN_IS_TIME_OUT);
        }
        //将用户信息放到request对象中
        currentContext.addZuulRequestHeader(Contants.CURR_MEMBER,memberJson);
        //request.setAttribute(Contants.CURR_MEMBER,memberVO);
        //续命
        RedisUtil.expire(KeyUtil.buileMemberKey(id),Contants.TOKEN_EXPIRE);
        return null;
    }
    private Object Result(ResponseEnum responseEnum) {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletResponse response = currentContext.getResponse();
        response.setContentType("application/json:charset=utf-8");
        //不仅拦截了，还能给前台提示  【核心代码】
        currentContext.setSendZuulResponse(false);
        //提示json信息
        ServerResponse error = ServerResponse.error(responseEnum.TOKEN_IS_MISS);
        String res = JSON.toJSONString(error);
        currentContext.setResponseBody(res);
        //return  null  默认放行
        return null;
    }
}
