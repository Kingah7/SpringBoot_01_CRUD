package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkSpaceService workSpaceService;

    private List<LocalDate> getdateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dataList = new ArrayList<>();

        while (!begin.equals(end.plusDays(1))) {
            dataList.add(begin);
            begin = begin.plusDays(1);
        }
        return dataList;
    }


    /**
     * 获取指定时间内营业额
     *
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

            System.out.println(amount);
            if (amount == null) amount = new BigDecimal(0);
            turnoverList.add(amount);
            begin = begin.plusDays(1);
        }

        return TurnoverReportVO.builder()
                .turnoverList(StringUtils.join(turnoverList, ','))
                .dateList(StringUtils.join(dataList, ','))
                .build();
    }

    /**
     * 统计指定时间内用户数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> userList = new ArrayList<>();
        List<Integer> addList = new ArrayList<>();


        while (!begin.equals(end.plusDays(1))) {

            dateList.add(begin);

            LocalDateTime st = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime ed = LocalDateTime.of(begin, LocalTime.MAX);

            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.between("create_time", st, ed);

            //新增用户
            addList.add(userMapper.selectCount(wrapper));
            //总用户
            wrapper.clear();
            wrapper.le("create_time", ed);
            userList.add(userMapper.selectCount(wrapper));

            begin = begin.plusDays(1);
        }

        String date = StringUtils.join(dateList, ',');
        String add = StringUtils.join(addList, ',');
        String user = StringUtils.join(userList, ',');

        return UserReportVO.builder()
                .dateList(date)
                .newUserList(add)
                .totalUserList(user)
                .build();
    }

    /**
     * 统计指定时间内的订单数据
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getdateList(begin, end);
        List<Integer> totalList = new ArrayList<>();
        List<Integer> vaildList = new ArrayList<>();
        int totalSum = 0, validSum = 0;
        double orderCompletionRate = 0.0;

        for (LocalDate date : dateList) {
            LocalDateTime st = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime ed = LocalDateTime.of(date, LocalTime.MAX);
            //查询每天的订单
            QueryWrapper<Orders> wrapper = new QueryWrapper<>();
            wrapper.between("order_time", st, ed);

            Integer total = orderMapper.selectCount(wrapper);
            totalSum += total;
            totalList.add(total);
            //查询每天的有效订单
            wrapper.eq("status", Orders.COMPLETED);
            Integer valid = orderMapper.selectCount(wrapper);
            validSum += valid;
            vaildList.add(valid);
        }

        if (totalSum != 0) {
            orderCompletionRate = validSum * 1.0 / totalSum;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ','))
                .orderCountList(StringUtils.join(totalList, ','))
                .validOrderCountList(StringUtils.join(vaildList, ','))
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(totalSum)
                .validOrderCount(validSum)
                .build();
    }

    /**
     * top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10ReportVO(LocalDate begin, LocalDate end) {
        LocalDateTime st = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime ed = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOS = orderMapper.queryTop10(st, ed);
        List<String> nameList = new ArrayList<>();
        List<Integer> valueList = new ArrayList<>();

        for (GoodsSalesDTO good : goodsSalesDTOS) {
            nameList.add(good.getName());
            valueList.add(good.getNumber());
        }

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ','))
                .numberList(StringUtils.join(valueList, ','))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse resp) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();

        BusinessDataVO businessDataVO = workSpaceService.queryBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        InputStream fileInputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(fileInputStream);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workSpaceService.queryBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = resp.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
