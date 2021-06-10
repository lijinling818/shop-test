package com.fh.shop.api.biz;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fh.shop.api.biz.po.Member;
import com.fh.shop.api.mapper.IMamberMapper;
import com.fh.shop.api.member.vo.MemberVO;
import com.fh.shop.common.Contants;
import com.fh.shop.common.KeyUtil;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.Md5Util;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

@Service("memberService")
@Transactional(rollbackFor = Exception.class)
public class IMemberServiceImpl implements IMemberService{
    @Autowired
    private IMamberMapper mamberMapper;

    @Autowired
    private HttpServletRequest request;



    @Override
    public ServerResponse login(String memberName, String password) {
        //非空验证
        if (StringUtils.isEmpty(memberName)|| StringUtils.isEmpty(password)){
            return ServerResponse.error(ResponseEnum.MEMBER_LOGIN_IS_NULL);
        }
        //验证用户是否存在
        QueryWrapper<Member> wrapper = new QueryWrapper<>();
       wrapper.eq("memberName", memberName);
        Member member = mamberMapper.selectOne(wrapper);
        if (member==null){
            return ServerResponse.error(ResponseEnum.MEMBER_LOGIN_NAME_IS_NULL);
        }
        //验证密码是否正确
        if ( !Md5Util.md5(password).equals(member.getPassword())){
            return ServerResponse.error(ResponseEnum.MEMBER_LOGIN_PASSWORD_IS_ERROR);
        }

        //生成签名
        MemberVO memberVO = new MemberVO();
        Long id= member.getId();
        memberVO.setId(id);
        memberVO.setMemberName(member.getMemberName());
        memberVO.setNickName(member.getNickName());
        //因为md5需要的是String类型，所以需要把java对象转为String类型
        String memberVoJson = JSON.toJSONString(memberVO);
        //生产Base64编码格式
        String BaseVo = Base64.getEncoder().encodeToString(memberVoJson.getBytes());
        String sign = Md5Util.sign(memberVoJson , Contants.SECRET);
        String BaseSign = Base64.getEncoder().encodeToString(sign.getBytes());
        //
        RedisUtil.setex(KeyUtil.buileMemberKey(id),"",Contants.TOKEN_EXPIRE);


        //返回前台  用户信息+签名 不包括密码
        //将生产的编码格式的签名 返回给前台
        return ServerResponse.success(BaseVo+"."+BaseSign);


    };









}
