package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dataList = new ArrayList<>();
        List<BigDecimal> turnoverList = new ArrayList<>();


        while (!begin.equals(end.plusDays(1))) {
            dataList.add(begin);

            LocalDateTime st = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime ed = LocalDateTime.of(begin, LocalTime.MAX);

            QueryWrapper<Orders> wrapper = new QueryWrapper<>();
            wrapper.select("SUM(amount)").between("order_time", st, ed).eq("status", Orders.COMPLETED);

            BigDecimal amount = (BigDecimal) orderMapper.selectObjs(wrapper).get(0);
            if (amount == null) amount = new BigDecimal(0);
            turnoverList.add(amount);
            begin = begin.plusDays(1);
        }


//        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
//        lqw.between(Orders::getOrderTime, st, ed).eq(Orders::getStatus,Orders.COMPLETED);
        return TurnoverReportVO.builder()
                .turnoverList(StringUtils.join(turnoverList, ','))
                .dateList(StringUtils.join(dataList, ','))
                .build();
    }
}
