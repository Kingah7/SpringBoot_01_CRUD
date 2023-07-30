package com.sky.controller.user;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.sky.constant.RedisKeyConstant;
import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static com.sky.constant.RedisKeyConstant.*;
import static com.sky.constant.StatusConstant.*;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> list(Long categoryId) {
        String key = CATEGORY + categoryId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return Result.success(JSONUtil.toList(json, Setmeal.class));
        }


        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(ENABLE);

        List<Setmeal> list = setmealService.list(setmeal);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(list));
        return Result.success(list);
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品列表")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id) {
        String key = SETMEAL + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return Result.success(JSONUtil.toList(json, DishItemVO.class));
        }


        List<DishItemVO> list = setmealService.getDishItemById(id);
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(list));
        return Result.success(list);
    }
}
