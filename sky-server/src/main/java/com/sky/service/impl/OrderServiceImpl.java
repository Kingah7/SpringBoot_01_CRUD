package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Order;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.LoginFailedException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.properties.WeChatProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderDetailService;
import com.sky.service.OrderService;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sky.constant.MessageConstant.*;
import static com.sky.entity.Orders.*;


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

    @Resource
    private WebSocketServer webSocketServer;

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
        orders.setAddress(addressBook.getAddressBook());

        orderMapper.insert(orders);
        //订单详情表插入
        Long ordersId = orders.getId();
        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();

            BeanUtil.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(ordersId);
            System.out.println(orderDetail);
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

    @Override
    public PageResult historyOrders(Integer page, Integer pageSize, Long status) {
        Long userId = BaseContext.getThreadLocal(Long.class);
        PageHelper.startPage(page, pageSize);
        Page<Orders> query = orderMapper.queryPage(status, userId);

        for (int i = 0; i < query.size(); i++) {
            Long orderId = query.get(i).getId();
            List<OrderDetail> orderDetailList = orderDetailMapper.selectList
                    (new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));

            query.get(i).setOrderDetailList(orderDetailList);
        }

        return new PageResult(query.getTotal(), query.getResult());
    }

    /**
     * 管理端订单查询
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult queryPage(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.queryPage2(ordersPageQueryDTO);
        for (Orders order : page.getResult()) {
            Long orderId = order.getId();
            List<OrderDetail> list = orderDetailMapper.selectList(new QueryWrapper<OrderDetail>().eq("order_id", orderId));
            order.setOrderDetailList(list);
        }

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public OrderStatisticsVO orderStatisticsVOResult() {
        Integer pendingPayment = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, PENDING_PAYMENT));
        Integer toBeConfirmed = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, TO_BE_CONFIRMED));
        Integer deliveryInProgress = orderMapper.selectCount(new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, DELIVERY_IN_PROGRESS));

        OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmed)
                .deliveryInProgress(deliveryInProgress)
                .confirmed(pendingPayment)
                .build();

        return orderStatisticsVO;
    }

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     */
    @Override
    public void orderCancel(OrdersCancelDTO ordersCancelDTO) {
        Long id = ordersCancelDTO.getId();
        update().set("status", CANCELLED)
                .set("cancel_reason", ordersCancelDTO.getCancelReason())
                .set("cancel_time", LocalDateTime.now())
                .eq("id", id)
                .update();
    }

    /**
     * 客户催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.selectById(id);

        if (orders == null) {
            throw new OrderBusinessException(ORDER_STATUS_ERROR);
        }

        Map map = new HashMap();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号:" + orders.getNumber());

        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }
}
