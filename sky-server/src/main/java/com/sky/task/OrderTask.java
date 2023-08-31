package com.sky.task;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sky.entity.Orders.CANCELLED;
import static com.sky.entity.Orders.PENDING_PAYMENT;
import static com.sky.task.CronConstant.*;

@Component
@Slf4j
public class OrderTask {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = EVERY_ONE_MINUTE)
    @Transactional
    public void processTimeOutOrder() {
        log.info("开始删除超时订单:{}", LocalDateTime.now());
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<Orders>();
        lqw.eq(Orders::getStatus, PENDING_PAYMENT).le(Orders::getOrderTime, LocalDateTime.now().plusMinutes(15));

        for (Orders order : orderMapper.selectList(lqw)) {
            order.setStatus(CANCELLED);
            order.setCancelReason("订单超时，自动取消");
            order.setCancelTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }
    }

    /**
     * 处理取消订单
     */

    @Transactional
    public void processCancelOrder() {
        log.info("开始删除取消订单:{}", LocalDateTime.now());
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, CANCELLED);
        List<Orders> ordersList = orderMapper.selectList(lqw);
        List<Long> ids = new ArrayList<>();
        if (ordersList != null && !ordersList.isEmpty()) {
            for (int i = 0; i < ordersList.size(); i++) {
                ids.add(ordersList.get(i).getId());
            }
            orderMapper.delete(lqw);
            orderDetailMapper.delete(new LambdaQueryWrapper<OrderDetail>().in(OrderDetail::getOrderId, ids));
        }
    }

}
