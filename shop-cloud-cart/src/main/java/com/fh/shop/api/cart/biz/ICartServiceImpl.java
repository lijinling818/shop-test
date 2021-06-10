package com.fh.shop.api.cart.biz;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.cart.vo.CartItemVo;
import com.fh.shop.api.cart.vo.CartVo;
import com.fh.shop.api.goods.IGoodsFeignService;
import com.fh.shop.api.goods.po.Sku;
import com.fh.shop.common.Contants;
import com.fh.shop.common.KeyUtil;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.BigdecimalUtil;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service("CartService")
@Transactional(rollbackFor = Exception.class)
public class ICartServiceImpl implements ICartService {

      @Autowired
      private IGoodsFeignService iGoodsFeignService;

      @Value("${cart.limit}")
      private int cartLimit;

      //商品加入购物车
    @Override
    public ServerResponse addCart(Long memberId, Long skuId, Long count) {
        if (count>cartLimit){
            return ServerResponse.error(ResponseEnum.CATR_COUNT_IS_FULL);
        }
        //商品是否存在
        ServerResponse<Sku> skuResponse = iGoodsFeignService.findSku(skuId);

        Sku sku = skuResponse.getData();   //skuMapper.selectById(skuId);
        if (sku==null){
            return ServerResponse.error(ResponseEnum.CART_SKU_IS_NULL);
        }
        //商品是否上架
        if (sku.getStatus()!= Contants.UP){
            return ServerResponse.error(ResponseEnum.CART_SKU_IS_DOWN);
        }
        //商品库存数量不得小于购买数量
        Integer stock = sku.getStock();
        if (stock.intValue()<count){
            return ServerResponse.error(ResponseEnum.CART_SKU_STOCK_IS_ERROR);
        }

        //会员是否有购物车
       String key= KeyUtil.buildCartKey(memberId);
        Boolean exist = RedisUtil.exist(key);
        //如果商品不存在
        if (!exist){
            if (count<=0){
                return ServerResponse.error(ResponseEnum.CART_IS_ERROR);
            }
            //没有购物车建个购物车，直接把商品加入购物车
            CartVo cartVo = new CartVo();
            CartItemVo cartItemVo = new CartItemVo();
            cartItemVo.setCount(count);
            String price = sku.getPrice().toString();
            cartItemVo.setPrice(price);
            cartItemVo.setSkuId(skuId);
            cartItemVo.setSkuImage(sku.getImage());
            cartItemVo.setSkuName(sku.getSkuName());
            BigDecimal subPrice = BigdecimalUtil.mul(price, count + "");
            cartItemVo.setSubPrice(subPrice.toString());
            cartVo.getCartItemVoList().add(cartItemVo);
            cartVo.setTotalCount(count);
            cartVo.setTotalPrice(cartItemVo.getSubPrice());
            //更新购物车，redis中的购物车
           // RedisUtil.set(key, JSON.toJSONString(cartVo));
            RedisUtil.hset(key, Contants.CART_JSON_FILED,JSON.toJSONString(cartVo));
            RedisUtil.hset(key, Contants.CART_COUNT_FILED,cartVo.getTotalCount()+"");
        }else {
            //有购物车
            String cartJson = RedisUtil.hget(key, Contants.CART_JSON_FILED);
            CartVo cartVo = JSON.parseObject(cartJson, CartVo.class);
            List<CartItemVo> cartItemVoList = cartVo.getCartItemVoList();
            Optional<CartItemVo> item = cartItemVoList.stream().filter(x -> x.getSkuId().longValue() == skuId.longValue()).findFirst();
            if (item.isPresent()){
                //有这款商品，找到这款商品，更新数量，小计
                CartItemVo cartItemVo = item.get();
            long itemCount= cartItemVo.getCount()+count;

            //限制购物10件
                if (itemCount>cartLimit){
                    return  ServerResponse.error(ResponseEnum.CATR_COUNT_IS_FULL);
                }
                if (itemCount<=0){
                    cartItemVoList.removeIf(x->x.getSkuId().longValue()==cartItemVo.getSkuId().longValue());
                    if (cartItemVoList.size()==0){
                        //把整个购物车干掉
                        RedisUtil.del(key);
                        return ServerResponse.success();
                    }
                    //更新购物车
                    updateCart(key, cartVo);
                    return ServerResponse.success();
                }
                    cartItemVo.setCount(itemCount);
                    BigDecimal subPrice= new BigDecimal(cartItemVo.getSubPrice());
                BigDecimal subPriceAtr = subPrice.add(BigdecimalUtil.mul(cartItemVo.getPrice(), count + ""));
                cartItemVo.setSubPrice(subPriceAtr.toString());
                //更新购物车
                updateCart(key, cartVo);

            }else{
                if (count<=0){
                    return ServerResponse.error(ResponseEnum.CART_IS_ERROR);
                }
                //没有这款商品，直接将商品加入购物车
                CartItemVo cartItemVo = new CartItemVo();
                cartItemVo.setCount(count);
                String price = sku.getPrice().toString();
                cartItemVo.setPrice(price);
                cartItemVo.setSkuId(skuId);
                cartItemVo.setSkuImage(sku.getImage());
                cartItemVo.setSkuName(sku.getSkuName());
                BigDecimal subPrice = BigdecimalUtil.mul(price, count + "");
                cartItemVo.setSubPrice(subPrice.toString());
                cartVo.getCartItemVoList().add(cartItemVo);
                //更新购物车
                updateCart(key, cartVo);
            }
        }
        return ServerResponse.success();
    }

    private void updateCart(String key, CartVo cartVo) {
        List<CartItemVo> cartItemVos = cartVo.getCartItemVoList();
        long totalCount = 0L;
        BigDecimal totalPrice = new BigDecimal(0);
        for (CartItemVo itemVo : cartItemVos) {
            totalCount += itemVo.getCount();
            totalPrice = totalPrice.add(new BigDecimal(itemVo.getSubPrice()));
        }
        cartVo.setTotalCount(totalCount);
        cartVo.setTotalPrice(totalPrice.toString());
        //更新购物车，redis中的购物车
       // RedisUtil.set(key, JSON.toJSONString(cartVo));
        RedisUtil.hset(key, Contants.CART_JSON_FILED,JSON.toJSONString(cartVo));
        RedisUtil.hset(key, Contants.CART_COUNT_FILED,cartVo.getTotalCount()+"");
    }



    @Override
    public ServerResponse findCart(Long memberId) {
        String cartJSON = RedisUtil.hget(KeyUtil.buildCartKey(memberId), Contants.CART_JSON_FILED);
        CartVo cartVo = JSON.parseObject(cartJSON, CartVo.class);
        return ServerResponse.success(cartVo);
    };

    @Override
    public ServerResponse findCartCount(Long memberId) {
        String count = RedisUtil.hget(KeyUtil.buildCartKey(memberId), Contants.CART_COUNT_FILED);
        return ServerResponse.success(count);
    }

    @Override
    public ServerResponse deleteCartItem(Long memberId, Long skuId) {
        //获取当前会员的购物车
        String cartJson = RedisUtil.hget(KeyUtil.buildCartKey(memberId), Contants.CART_JSON_FILED);
        CartVo cartVo = JSON.parseObject(cartJson, CartVo.class);
        List<CartItemVo> cartItemVoList = cartVo.getCartItemVoList();
        Optional<CartItemVo> itemVo = cartItemVoList.stream().filter(x -> x.getSkuId().longValue() == skuId.longValue()).findFirst();
        if (!itemVo.isPresent()){
            return ServerResponse.error(ResponseEnum.CART_SKU_IS_NULL);
        }
        cartItemVoList.removeIf(x->x.getSkuId().longValue()==skuId.longValue());
        if (cartItemVoList.size()==0){
            RedisUtil.del(KeyUtil.buildCartKey(memberId));
            return ServerResponse.success();
        }
        //更新购物车
        updateCart(KeyUtil.buildCartKey(memberId),cartVo);
        return ServerResponse.success();
    }

    @Override
    public ServerResponse deleteBatch(Long memberId, String ids) {
        if (StringUtils.isEmpty(ids)){
            return ServerResponse.error(ResponseEnum.CART_IS_NO_SELETE);
        }
        //获取会员对应的购物车
        String key= KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.hget(key, Contants.CART_JSON_FILED);
        CartVo cartVo = JSON.parseObject(cartJson, CartVo.class);
        List<CartItemVo> cartItemVoList = cartVo.getCartItemVoList();
        Arrays.stream(ids.split(",")).forEach(x->cartItemVoList.removeIf(y->y.getSkuId().longValue()==Long.parseLong(x)));
        if (cartItemVoList.size()==0){
            RedisUtil.del(key);
            return ServerResponse.success();
        }
        //更新购物车
        updateCart(key,cartVo);
        return ServerResponse.success();


    };
















}
