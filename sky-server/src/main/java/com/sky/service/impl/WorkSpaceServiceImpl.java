package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private ReportService reportService;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 查询今日运营数据
     *
     * @return
     */
    @Override
    public BusinessDataVO queryBusinessData(LocalDateTime begin, LocalDateTime end) {
        LocalDate beginTime = begin.toLocalDate();
        LocalDate endTime = end.toLocalDate();

        TurnoverReportVO turnoverStatistics = reportService.getTurnoverReport(beginTime, endTime);
        UserReportVO userStatistics = reportService.getUserStatistics(beginTime, endTime);
        OrderReportVO ordersStatistics = reportService.getOrdersStatistics(beginTime, endTime);


        //获取当日新增用户数量
        String[] split = userStatistics.getNewUserList().split(",");
        Integer newUsers = 0;
        for (String s : split) {
            newUsers += Integer.valueOf(s);
        }
        //获取当日订单完成率
        Double orderCompletionRate = ordersStatistics.getOrderCompletionRate();
        //获取当日营业额
        Double turnover = 0.0;
        for (String s : turnoverStatistics.getTurnoverList().split(",")) {
            turnover += Double.valueOf(s);
        }
        //获取有效订单数
        Integer validOrderCount = ordersStatistics.getValidOrderCount();
        //获取平均客单价
        Double unitPrice;
        if (validOrderCount == 0) unitPrice = 0.0;
        else unitPrice = turnover / validOrderCount;


        return BusinessDataVO.builder()
                .newUsers(newUsers)
                .orderCompletionRate(orderCompletionRate)
                .turnover(turnover)
                .unitPrice(unitPrice)
                .validOrderCount(validOrderCount)
                .build();
    }

    /**
     * 查询套餐停售状态
     *
     * @return
     */
    @Override
    public SetmealOverViewVO queryOverviewSetmeals() {
        QueryWrapper<Setmeal> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);

        Integer sold = setmealMapper.selectCount(null);
        Integer discontinued = setmealMapper.selectCount(wrapper);
        sold -= discontinued;

        return SetmealOverViewVO
                .builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询菜品停售状态
     *
     * @return
     */
    @Override
    public DishOverViewVO queryOverviewDish() {
        QueryWrapper<Dish> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0);

        Integer sold = dishMapper.selectCount(null);
        Integer discontinued = dishMapper.selectCount(wrapper);
        sold -= discontinued;

        return DishOverViewVO
                .builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询订单详情
     *
     * @return
     */
    @Override
    public OrderOverViewVO queryOverviewOrders() {
        Integer allOrders = orderMapper.selectCount(null);

        Integer cancelledOrders = orderMapper.selectCount(new QueryWrapper<Orders>().eq("status", 6));
        Integer completedOrders = orderMapper.selectCount(new QueryWrapper<Orders>().eq("status", 5));
        Integer deliveredOrders = orderMapper.selectCount(new QueryWrapper<Orders>().eq("status", 3));
        Integer waitingOrders = orderMapper.selectCount(new QueryWrapper<Orders>().eq("status", 2));

        return OrderOverViewVO.builder()
                .allOrders(allOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .waitingOrders(waitingOrders)
                .build();
    }
}




