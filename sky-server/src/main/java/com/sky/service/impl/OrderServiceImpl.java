package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Order;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.LoginFailedException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.properties.WeChatProperties;
import com.sky.service.OrderService;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.OrderSubmitVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sky.constant.MessageConstant.*;
import static com.sky.entity.Orders.PENDING_PAYMENT;
import static com.sky.entity.Orders.UN_PAID;


@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private AddressBookMapper addressBookMapper;

    @Resource
    private ShoppingCartMapper shoppingCartMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //异常排除
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.selectById(addressBookId);
        if (BeanUtil.isEmpty(addressBook)) {
            throw new AddressBookBusinessException(ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getThreadLocal(Long.class);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectList(
                new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));
        if (shoppingCartList.isEmpty() || shoppingCartList == null) {
            throw new ShoppingCartBusinessException(SHOPPING_CART_IS_NULL);
        }

        //订单表插入
        Orders orders = new Orders();
        BeanUtil.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(UN_PAID);
        orders.setStatus(PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());//收货人
        orders.setUserId(userId);

        orderMapper.insert(orders);
        //订单详情表插入
        Long ordersId = orders.getId();
        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtil.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(ordersId);
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertList(orderDetailList);

        //清空购物车
        shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>().eq(ShoppingCart::getUserId, userId));

        //封装vo
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }
}
