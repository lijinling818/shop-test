package com.fh.shop.api.goods.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SkuVo implements Serializable {

    private Long id;
    private String skuName;

    private String price;

    private String image;

}
