package com.fh.shop.api.goods.po;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Sku implements Serializable {

  private Long id;

  private String skuName;

  private  Long spuId;

  private BigDecimal price;

  private  int stock;

  private  String specInfo;

  private String image;

  private  Long colorId;
  private Integer status;  //上下架状态

  private Integer newProduct;//是否新品

  private Integer recommend;//是否推荐

  private  Long sale;   //销量


}
