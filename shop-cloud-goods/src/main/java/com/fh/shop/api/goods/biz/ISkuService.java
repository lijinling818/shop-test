package com.fh.shop.api.goods.biz;

import com.fh.shop.common.ServerResponse;

public interface ISkuService {
    ServerResponse findSkuList();

    ServerResponse findSku(Long id);
}
