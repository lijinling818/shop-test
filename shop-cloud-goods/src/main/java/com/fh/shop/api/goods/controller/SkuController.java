package com.fh.shop.api.goods.controller;

import com.fh.shop.api.goods.biz.ISkuService;
import com.fh.shop.common.ServerResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
public class SkuController {
    @Resource(name = "skuService")
    private ISkuService skuService;


  @GetMapping("/skus")
    public ServerResponse findSkuList(){
        return skuService.findSkuList();
    }

    @GetMapping("/findSku")
    public ServerResponse findSku(@RequestParam("id") Long id){
      return skuService.findSku(id);
    }
}
