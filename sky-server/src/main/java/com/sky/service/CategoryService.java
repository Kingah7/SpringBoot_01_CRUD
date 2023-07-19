package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;

import java.util.List;

public interface CategoryService extends IService<Category> {

    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    List<Category> getCtgByType(Integer type);

    void save(CategoryDTO categoryDTO);

     void updateCategory(CategoryDTO categoryDTO);

    Result deleteById(Integer id);


    void changeStatus(Integer status, Long id);
}
