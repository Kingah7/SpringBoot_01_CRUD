package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
    Page<Orders> queryPage(@Param("status") Long status, @Param("userId") Long userId);

    Page<Orders> queryPage2(OrdersPageQueryDTO ordersPageQueryDTO);

    List<GoodsSalesDTO> queryTop10(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
