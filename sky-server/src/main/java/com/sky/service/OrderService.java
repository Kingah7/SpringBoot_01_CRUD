package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.vo.OrderSubmitVO;


public interface OrderService extends IService<Orders> {
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
}
