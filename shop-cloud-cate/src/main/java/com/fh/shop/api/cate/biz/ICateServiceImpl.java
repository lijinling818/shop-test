package com.fh.shop.api.cate.biz;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.cate.mapper.ICateMappers;
import com.fh.shop.api.cate.po.Cate;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("cateServices")
public class ICateServiceImpl implements ICateService {

    @Autowired
    private ICateMappers cateMapper;
    public ServerResponse findList(){
        String cateList1 = RedisUtil.get("cateList");
        if (StringUtils.isNotBlank(cateList1)){
            //如果缓存里的数据不为空，将json字符串转为java对象
            List<Cate> cates = JSON.parseArray(cateList1, Cate.class);
            //然后直接返回
            return ServerResponse.success(cates);
        }
         //从数据库中找
        List<Cate> cateList = cateMapper.selectList(


                null);
        //将java对象转为字符串，往缓存中放
        String cateList2 = JSON.toJSONString(cateList);
        //往缓存中放一份
        RedisUtil.set("cateList",cateList2);
        //返回
        return ServerResponse.success(cateList);
    }
}
