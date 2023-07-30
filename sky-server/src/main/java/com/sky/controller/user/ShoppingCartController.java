package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端购物车相关接口")
public class ShoppingCartController {

    @Resource
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车:{}", shoppingCartDTO);
        shoppingCartService.updateShoppingCart(shoppingCartDTO, true);
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("减少购物车")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("减少购物车:{}", shoppingCartDTO);
        shoppingCartService.updateShoppingCart(shoppingCartDTO, false);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> queryCart() {
        List<ShoppingCart> shoppingCartList = shoppingCartService.showShoppingCart();
        return Result.success(shoppingCartList);
    }

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result cleanShoppingCart() {
        shoppingCartService.clean();
        return Result.success();
    }
}
