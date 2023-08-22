package com.sky.controller.user;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.service.OrderDetailService;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.swing.text.DateFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sky.entity.Orders.PAID;
import static com.sky.entity.Orders.TO_BE_CONFIRMED;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "C端-订单相关接口")
@Slf4j
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderDetailService orderDetailService;

    @Resource
    private AddressBookService addressBookService;

    @Resource
    private WebSocketServer webSocketServer;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("历史订单")
    public Result<PageResult> historyOrders(Integer page, Integer pageSize, @RequestParam(required = false) Long status) {
        log.info("开始查询历史订单:{},{},{}", page, pageSize, status);
        PageResult pageResult = orderService.historyOrders(page, pageSize, status);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<Orders> queryOrderDetail(@PathVariable Long id) {
        log.info("查询订单详情", id);
        Orders orders = orderService.getById(id);

        Long addressBookId = orders.getAddressBookId();
        AddressBook address = addressBookService.getById(addressBookId);
        String addressDetail = address.getProvinceName() +
                address.getCityName() + address.getDistrictName() +
                address.getDetail();

        orders.setAddress(addressDetail);
        orders.setOrderDetailList(orderDetailService.list(new QueryWrapper<OrderDetail>()
                .eq("order_id", id)));

        return Result.success(orders);
    }

    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrdersPaymentDTO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        orderService.update().set("pay_status", Integer.toString(PAID))
                .set("status", TO_BE_CONFIRMED)
                .set("checkout_time", LocalDateTime.now().format(formatter))
                .eq("number", ordersPaymentDTO.getOrderNumber())
                .update();

        log.info("订单支付成功:{}", ordersPaymentDTO);

        //向管理端推送消息
        String number = ordersPaymentDTO.getOrderNumber();
        Map map = new HashMap();
        map.put("type", 1);
        Orders orders = orderService.getBaseMapper().selectOne(new QueryWrapper<Orders>().eq("number", number));
        map.put("orderId", orders.getId());
        map.put("content", "订单号:" + number);


        String jsonStr = JSONUtil.toJsonStr(map);
        webSocketServer.sendToAllClient(jsonStr);
        return Result.success(ordersPaymentDTO);
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("客户催单")
    public Result reminderOrder(@PathVariable Long id) {
        orderService.reminder(id);
        return Result.success();
    }
}
