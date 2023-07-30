package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Resource
    private ShoppingCartMapper shoppingCartMapper;

    @Resource
    private DishMapper dishMapper;

    @Resource
    private SetmealMapper setmealMapper;

    @Override
    @Transactional
    public void updateShoppingCart(ShoppingCartDTO shoppingCartDTO, boolean isAdd) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtil.copyProperties(shoppingCartDTO, shoppingCart);

        shoppingCart.setUserId(BaseContext.getThreadLocal(Long.class));
        shoppingCart.setCreateTime(LocalDateTime.now());
        ShoppingCart shoppingCarts = shoppingCartMapper.list(shoppingCart);

        if (shoppingCarts != null) {
            String sql = isAdd ? "number = number + 1" : "number = number - 1";
            update().setSql(sql).eq("id", shoppingCarts.getId()).update();
            if (shoppingCartMapper.selectById(shoppingCarts.getId()).getNumber() == 0) {
                shoppingCartMapper.deleteById(shoppingCarts.getId());
            }
            return;
        }

        if (shoppingCart.getDishId() != null) {
            //名称name、价格amount
            Dish dish = dishMapper.selectById(shoppingCart.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setImage(dish.getImage());

        } else {
            Setmeal setmeal = setmealMapper.selectById(shoppingCart.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setAmount(setmeal.getPrice());
            shoppingCart.setImage(setmeal.getImage());
        }
        shoppingCart.setNumber(1);
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getThreadLocal(Long.class));
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.selectList(lqw);
        return shoppingCarts;
    }

    @Override
    public void clean() {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getThreadLocal(Long.class));
        shoppingCartMapper.delete(lqw);
    }
}
