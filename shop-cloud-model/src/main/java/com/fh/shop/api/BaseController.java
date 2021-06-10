package com.fh.shop.api;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.member.vo.MemberVO;
import com.fh.shop.common.Contants;

import javax.servlet.http.HttpServletRequest;

public class BaseController {

    public MemberVO  buildMemberVO(HttpServletRequest request){
        String header = request.getHeader(Contants.CURR_MEMBER);
        MemberVO memberVO = JSON.parseObject(header, MemberVO.class);
        return memberVO;
    }
}
