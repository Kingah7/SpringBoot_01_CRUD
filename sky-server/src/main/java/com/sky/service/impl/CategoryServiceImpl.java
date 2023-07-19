package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.sky.constant.MessageConstant.CATEGORY_BE_RELATED_BY_DISH;
import static com.sky.constant.MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL;
import static com.sky.constant.StatusConstant.ENABLE;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private DishMapper dishMapper;

    @Resource
    private SetmealMapper setmealMapper;

    /**
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(categoryPageQueryDTO.getType() != null, Category::getType, categoryPageQueryDTO.getType());
        lqw.orderByAsc(Category::getSort);
        IPage page = new Page(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        categoryMapper.selectPage(page, lqw);

        PageResult pageResult = new PageResult();
        pageResult.setRecords(page.getRecords());
        pageResult.setTotal(page.getTotal());

        return pageResult;

    }

    @Override
    public List<Category> getCtgByType(Integer type) {
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Category::getType, type);
        List<Category> list = categoryMapper.selectList(lqw);
        return list;
    }

    /**
     * 新增分类
     *
     * @param categoryDTO
     */
    @Transactional
    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(ENABLE);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getThreadLocal(Long.class));
        category.setCreateUser(BaseContext.getThreadLocal(Long.class));
        category.setUpdateTime(LocalDateTime.now());

        categoryMapper.insert(category);
    }

    /**
     * 修改分类
     *
     * @param categoryDTO
     */
    @Override
    @Transactional
    public void updateCategory(CategoryDTO categoryDTO) {
        Category category = categoryMapper.selectById(categoryDTO.getId());
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getThreadLocal(Long.class));
        categoryMapper.updateById(category);
    }

    @Transactional
    @Override
    public Result deleteById(Integer id) {
        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        List<Setmeal> setmeals = setmealMapper.selectList(new LambdaQueryWrapper<Setmeal>().eq(Setmeal::getCategoryId, id));
        if (dishes != null) {
            throw new DeletionNotAllowedException(CATEGORY_BE_RELATED_BY_DISH);
        }
        if (setmeals != null) {
            throw new DeletionNotAllowedException(CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        categoryMapper.deleteById(id);
        return Result.success();
    }

    @Override
    public void changeStatus(Integer status, Long id) {
        categoryMapper.changeStatus(status,id);
    }
}
