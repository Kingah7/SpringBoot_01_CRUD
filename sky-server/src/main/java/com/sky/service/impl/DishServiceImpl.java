package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Resource
    private DishMapper dishMapper;

    @Resource
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和口味
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        log.info("新增菜品和口味:{}", dishDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getThreadLocal(Long.class));
        dish.setCreateUser(BaseContext.getThreadLocal(Long.class));
        dish.setUpdateTime(LocalDateTime.now());

        dishMapper.insert(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            for (int i = 0; i < flavors.size(); i++) {
                flavors.get(i).setDishId(dish.getId());
                dishFlavorMapper.insert(flavors.get(i));
            }
        }
    }
}
