package com.fh.shop.api.cart.vo;

import lombok.Data;

@Data
public class CartItemVo {
    private Long skuId;
    private String skuName;
    //在前台展示price用String类型，往数据库存 价格要用bigdecimal类型
    private String price;

    private String skuImage;

    private  Long count;

    private String  subPrice;
}
