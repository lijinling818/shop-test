package com.fh.shop.api.controller;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.biz.IMemberService;
import com.fh.shop.api.member.vo.MemberVO;
import com.fh.shop.common.Contants;
import com.fh.shop.common.KeyUtil;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Api(tags = "会员接口")
public class MemberController {
    @Autowired
    private HttpServletRequest request;
    @Resource(name = "memberService")
    private IMemberService memberService;


    //会员登录

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ApiOperation("登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberName",value = "会员名称",required = true,dataType ="java.long.String" ),
            @ApiImplicitParam(name = "password",value = "密码",required = true,dataType ="java.long.String" ),
    })
     public ServerResponse Login(String memberName, String password){

        return memberService.login(memberName,password);
     };

    @RequestMapping(value = "/findMember",method = RequestMethod.GET)
   @ApiImplicitParam(name = "x-auth",value = "头信息",dataType = "java.long.String",paramType = "header",required = true)
    public ServerResponse findMember(){
        //MemberVO memberVO = (MemberVO) request.getAttribute(Contants.CURR_MEMBER);
        String header = request.getHeader(Contants.CURR_MEMBER);
        MemberVO memberVO = JSON.parseObject(header, MemberVO.class);
        return ServerResponse.success(memberVO);
    };
    @RequestMapping(value = "/logOut",method = RequestMethod.GET)

    public ServerResponse logOut(){
        //MemberVO memberVO = (MemberVO) request.getAttribute(Contants.CURR_MEMBER);
        String header = request.getHeader(Contants.CURR_MEMBER);
        MemberVO memberVO = JSON.parseObject(header, MemberVO.class);
        Long id = memberVO.getId();
        RedisUtil.del(KeyUtil.buileMemberKey(id));
        return ServerResponse.success();
    };











}



