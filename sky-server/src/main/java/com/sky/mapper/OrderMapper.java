package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
    Page<Orders> queryPage(@Param("status") Long status, @Param("userId") Long userId);

    Page<Orders> queryPage2(OrdersPageQueryDTO ordersPageQueryDTO);
}
