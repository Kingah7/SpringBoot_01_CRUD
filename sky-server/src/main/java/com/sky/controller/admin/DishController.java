package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.sky.constant.RedisKeyConstant.DISH_PREFIX;

@Slf4j
@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {

    @Resource
    private DishService dishService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result saveWithFlavor(@RequestBody DishDTO dishDTO) {
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> queryPage(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.queryPage(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除菜品
     *
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result deleteDish(@RequestParam List<Long> ids) throws InterruptedException {
        dishService.deleteBatch(ids);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            list.add(DISH_PREFIX + ids.get(i).toString());
        }
        stringRedisTemplate.delete(list);
        return Result.success();
    }

    /**
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("更新菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        dishService.updateWithFlavor(dishDTO);
        stringRedisTemplate.delete(DISH_PREFIX + dishDTO.getId());
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getDishByCategoryId(Long categoryId) {
        List<Dish> list = dishService.selectList(categoryId);
        return Result.success(list);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("起售停售")
    public Result changeStatus(@PathVariable Long status, Long id) {
        dishService.changeStatus(status, id);
        stringRedisTemplate.delete(DISH_PREFIX + id);
        return Result.success();
    }

}
