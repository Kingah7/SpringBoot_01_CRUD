package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static com.sky.constant.MessageConstant.*;
import static com.sky.constant.StatusConstant.DISABLE;
import static com.sky.constant.StatusConstant.ENABLE;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Resource
    private SetmealMapper setmealMapper;

    @Resource
    private SetmealDishMapper setmealDishMapper;

    @Resource
    private DishMapper dishMapper;

    @Override
    public PageResult queryPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.queryPage(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional

    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmeal.setCreateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getThreadLocal(Long.class));
        setmeal.setCreateUser(BaseContext.getThreadLocal(Long.class));
        setmeal.setUpdateTime(LocalDateTime.now());

        setmealMapper.insert(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes == null) return;
        for (int i = 0; i < setmealDishes.size(); i++) {
            setmealDishes.get(i).setSetmealId(setmeal.getId());
            setmealDishMapper.insert(setmealDishes.get(i));
        }
    }

    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getThreadLocal(Long.class));

        if (setmealDTO.getSetmealDishes() == null) return;
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealMapper.update(setmeal);

        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        for (int i = 0; i < setmealDishes.size(); i++) {
            setmealDishes.get(i).setSetmealId(setmeal.getId());
            setmealDishMapper.insert(setmealDishes.get(i));
        }

    }

    @Override
    public SetmealVO getBySetmealId(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);

        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public void changeStatus(Long id, Long status) {

        List<Long> dishIds = setmealMapper.getDishBySetmealId(id);
        if (status == 1 && dishIds != null) {
            for (int i = 0; i < dishIds.size(); i++) {
                Dish dish = dishMapper.selectById(dishIds.get(i));
                if (dish.getStatus() == DISABLE)
                    throw new DeletionNotAllowedException(SETMEAL_ENABLE_FAILED);
            }
        }


        setmealMapper.updateStatus(id, status);
    }

    @Transactional
    @Override
    public void deleteByIds(List<Long> ids) {
        //1.判断当前套餐是否能够删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.selectById(id);
            if (setmeal.getStatus() == ENABLE) {
                throw new DeletionNotAllowedException(SETMEAL_ON_SALE);
            }
        }

        for (Long id : ids) {
            setmealMapper.deleteById(id);
            LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
            lqw.eq(SetmealDish::getSetmealId, id);
            setmealDishMapper.delete(lqw);
        }
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
