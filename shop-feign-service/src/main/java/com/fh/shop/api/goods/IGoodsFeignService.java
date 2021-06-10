package com.fh.shop.api.goods;

import com.fh.shop.api.goods.po.Sku;
import com.fh.shop.common.ServerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name ="shop-cloud-goods")
public interface IGoodsFeignService {

    @GetMapping("/api/findSku")
    public ServerResponse<Sku> findSku(@RequestParam("id") Long id);

}
