package com.fh.shop.api.cart.Controller;

import com.fh.shop.api.BaseController;
import com.fh.shop.api.cart.biz.ICartService;
import com.fh.shop.api.member.vo.MemberVO;
import com.fh.shop.common.ServerResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Api(tags = "购物车接口")
public class CartController extends BaseController {

    @Resource(name = "CartService")
    private ICartService cartService;
    @Autowired
    private HttpServletRequest request;

@PostMapping("/addCart")
@ApiOperation("购物车添加商品接口")
@ApiImplicitParams({
        @ApiImplicitParam(name = "skuId",value = "商品id",dataType = "java.lang.Long",required = true),
        @ApiImplicitParam(name = "count",value = "商品数量",dataType = "java.lang.Long",required = true),
        @ApiImplicitParam(name = "x-auth",value = "请求头信息",dataType = "java.lang.String",paramType = "header",required = true)
})
    public ServerResponse addCart(Long skuId, Long count){
    MemberVO memberVO = buildMemberVO(request);
    Long memberId=memberVO.getId();
    return cartService.addCart(memberId,skuId,count);
}
@GetMapping("/findCart")
@ApiOperation("购物车展示接口")
@ApiImplicitParams({
        @ApiImplicitParam(name = "x-auth",value = "请求头信息",dataType = "java.lang.String",paramType = "header",required = true)
})
public ServerResponse findCart(){
    MemberVO memberVO = buildMemberVO(request);
    Long memberId=memberVO.getId();
    return cartService.findCart(memberId);
}


@GetMapping("/findCartCount")
    @ApiOperation("查询购物车商品数量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "请求头信息",dataType = "java.lang.String",paramType = "header",required = true)
    })
    public ServerResponse findCartCount(){
    MemberVO memberVO = buildMemberVO(request);
        Long memberId=memberVO.getId();
        return cartService.findCartCount(memberId);
    }





@DeleteMapping("/deleteCartItem")
@ApiOperation("删除购物车")
@ApiImplicitParams({
        @ApiImplicitParam(name = "x-auth",value = "请求头信息",dataType = "java.lang.String",paramType = "header",required = true)
})
    //删除购物车中的内容
    public ServerResponse deleteCartItem(Long skuId){
    MemberVO memberVO = buildMemberVO(request);
    Long memberId=memberVO.getId();
    return cartService.deleteCartItem(memberId,skuId);
    }

    @DeleteMapping("/deleteBatch")
    @ApiOperation("批量删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth",value = "请求头信息",required = true,paramType = "header",dataType = "java.lang.String")
    })
    public ServerResponse deleteBatch(String ids){
        MemberVO memberVO = buildMemberVO(request);
        Long memberId=memberVO.getId();
        return cartService.deleteBatch(memberId,ids);
    }






}
