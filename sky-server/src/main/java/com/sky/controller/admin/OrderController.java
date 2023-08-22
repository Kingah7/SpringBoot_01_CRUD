package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.sky.entity.Orders.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单管理接口")
public class OrderController {
    @Resource
    private OrderService orderService;

    @Resource
    private AddressBookService addressBookService;


    @GetMapping("/conditionSearch")
    @ApiOperation("分页查询")
    public Result<PageResult> queryPage(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("开始分页查询:{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.queryPage(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> orderStatisticsVOResult() {
        log.info("各个状态的订单数量统计...");
        OrderStatisticsVO orderStatisticsVO = orderService.orderStatisticsVOResult();
        return Result.success(orderStatisticsVO);
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result orderCancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("取消订单:{}", ordersCancelDTO);
        orderService.orderCancel(ordersCancelDTO);
        return Result.success();
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result queryOrderDetail(@PathVariable Long id) {
        log.info("查询订单详情:{}", id);
        Orders orders = orderService.query().eq("id", id).one();
        return Result.success(orders);
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejectOrder(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        Long orderId = ordersRejectionDTO.getId();

        orderService.update()
                .set("rejection_reason", ordersRejectionDTO.getRejectionReason())
                .set("status", CANCELLED)
                .set("pay_status", REFUND)
                .eq("id", orderId).update();
        return Result.success();
    }

    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result orderConfirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单:{}", ordersConfirmDTO);
        orderService.update().set("status", CONFIRMED).eq("id", ordersConfirmDTO.getId()).update();
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result completeOrder(@PathVariable Long id) {
        log.info("完成订单:{}", id);
        orderService.update().set("status", Orders.COMPLETED).update();
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repeatOrder(@PathVariable Long id) {
        /**
         * 再来一单业务
         */
        return Result.success();
    }
}
