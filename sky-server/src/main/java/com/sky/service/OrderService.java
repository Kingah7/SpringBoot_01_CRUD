package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;


public interface OrderService extends IService<Orders> {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    PageResult historyOrders(Integer page, Integer pageSize, Long status);

    PageResult queryPage(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO orderStatisticsVOResult();

    void orderCancel(OrdersCancelDTO ordersCancelDTO);

    void reminder(Long id);
}
