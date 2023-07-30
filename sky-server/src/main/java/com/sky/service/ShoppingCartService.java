package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    void updateShoppingCart(ShoppingCartDTO shoppingCartDTO, boolean isAdd);

    List<ShoppingCart> showShoppingCart();

    void clean();
}
